package io.github.sakurawald.module.initializer.kit;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.kit.command.argument.wrapper.KitName;
import io.github.sakurawald.module.initializer.kit.gui.KitEditorGui;
import io.github.sakurawald.module.initializer.kit.structure.Kit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@CommandNode("kit")
@CommandRequirement(level = 4)
public class KitInitializer extends ModuleInitializer {

    private static final String INVENTORY = "inventory";

    private static final Path STORAGE_PATH = ReflectionUtil.getModuleConfigPath(KitInitializer.class).resolve("kit-data");

    public static void writeKit(@NotNull Kit kit) {
        Path path = STORAGE_PATH.resolve(kit.getName());

        NbtCompound root = NbtHelper.readOrDefault(path);
        if (root == null) {
            LogUtil.warn("failed to write kit {}", kit);
            return;
        }

        NbtList nbtList = new NbtList();
        NbtHelper.writeSlotsNode(nbtList, kit.getStackList());

        root.put(INVENTORY, nbtList);
        NbtHelper.write(root, path);
    }

    public static @NotNull List<String> getKitNameList() {
        List<String> ret = new ArrayList<>();
        try {
            Files.list(STORAGE_PATH).forEach(p -> ret.add(p.toFile().getName()));
        } catch (IOException e) {
            LogUtil.error("failed to list kits {}", e.toString());
        }
        return ret;
    }

    public static @NotNull List<Kit> readKits() {
        List<Kit> ret = new ArrayList<>();
        for (String name : getKitNameList()) {
            ret.add(readKit(name));
        }
        return ret;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteKit(@NotNull String name) {
        Path path = STORAGE_PATH.resolve(name);
        path.toFile().delete();
    }

    public static @NotNull Kit readKit(@NotNull String name) {
        Path p = STORAGE_PATH.resolve(name);
        NbtCompound root = NbtHelper.readOrDefault(p);

        if (root == null) {
            return new Kit(p.toFile().getName(), new ArrayList<>());
        }

        NbtList nbtList = (NbtList) root.get(INVENTORY);
        List<ItemStack> itemStacks = NbtHelper.readSlotsNode(nbtList);
        return new Kit(p.toFile().getName(), itemStacks);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onInitialize() {
        STORAGE_PATH.toFile().mkdirs();
    }

    @CommandNode("editor")
    private static int $editor(@CommandSource ServerPlayerEntity player) {
        List<Kit> kits = readKits();
        new KitEditorGui(player, kits, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("give")
    private static int $give(@CommandSource CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, KitName kit) {
        /* read kit*/
        Kit $kit = readKit(kit.getValue());
        if ($kit.getStackList().isEmpty()) {
            LocaleHelper.sendMessageByKey(ctx.getSource(), "kit.kit.empty");
            return CommandHelper.Return.FAIL;
        }

        /* try to insert the item in specified slot */
        PlayerInventory playerInventory = player.getInventory();
        List<ItemStack> failedList = new ArrayList<>();
        for (int i = 0; i < $kit.getStackList().size(); i++) {
            ItemStack copy = $kit.getStackList().get(i).copy();

            if (!playerInventory.insertStack(i, copy)) {
                failedList.add(copy);
            }
        }

        /* try to insert the item in any slot */
        failedList.removeIf(playerInventory::insertStack);

        /* the inventory of player is full, just drop the item in the ground */
        failedList.forEach(it -> player.dropItem(it, true));

        return CommandHelper.Return.SUCCESS;
    }

}
