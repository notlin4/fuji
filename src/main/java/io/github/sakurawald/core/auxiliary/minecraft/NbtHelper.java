package io.github.sakurawald.core.auxiliary.minecraft;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.structure.SpatialBlock;
import lombok.experimental.UtilityClass;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class NbtHelper {

    private static final String FUJI_UUID = Fuji.MOD_ID + "$uuid";

    public static void set(@NotNull NbtCompound root, @NotNull String path, NbtElement value) {
        // search the path
        String[] nodes = path.split("\\.");
        for (int i = 0; i < nodes.length - 1; i++) {
            String node = nodes[i];

            if (!root.contains(node)) {
                root.put(node, new NbtCompound());
            }

            root = root.getCompound(node);
        }

        // set the value
        String key = nodes[nodes.length - 1];
        root.put(key, value);
    }

    public static NbtElement getOrDefault(@NotNull NbtCompound root, @NotNull String path, NbtElement defaultValue) {
        if (get(root, path) == null) {
            set(root, path, defaultValue);
        }

        return get(root, path);
    }

    public static @org.jetbrains.annotations.Nullable NbtElement get(@NotNull NbtCompound root, @NotNull String path) {
        // search the path
        String[] nodes = path.split("\\.");
        for (int i = 0; i < nodes.length - 1; i++) {
            String node = nodes[i];

            if (!root.contains(node)) {
                LogUtil.error("nbt {} don't has path {}", root, path);
            }

            root = root.getCompound(node);
        }

        // get the value
        String key = nodes[nodes.length - 1];
        return root.get(key);
    }

    public static @Nullable NbtCompound readOrDefault(@NotNull Path path) {
        try {
            if (!path.toFile().exists()) {
                NbtIo.write(new NbtCompound(), path);
            }
            return Objects.requireNonNull(NbtIo.read(path));
        } catch (IOException e) {
            LogUtil.error("failed to create nbt file in {}", path);
        }

        return null;
    }

    public static void write(@NotNull NbtCompound root, @NotNull Path path) {
        try {
            NbtIo.write(root, path);
        } catch (IOException e) {
            LogUtil.error("failed to write nbt file in {}", path);
        }
    }

    public static NbtList writeSlotsNode(@NotNull NbtList node, @NotNull List<ItemStack> itemStackList) {
        for (ItemStack item : itemStackList) {
            node.add(item.encodeAllowEmpty(RegistryHelper.getDefaultWrapperLookup()));
        }
        return node;
    }

    public static @NotNull List<ItemStack> readSlotsNode(@Nullable NbtList node) {
        if (node == null) return new ArrayList<>();

        List<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            ret.add(ItemStack.fromNbtOrEmpty(RegistryHelper.getDefaultWrapperLookup(), node.getCompound(i)));
        }
        return ret;
    }


    private static NbtCompound addUuidToNbtCompoundIfAbsent(@Nullable NbtCompound root) {
        if (root == null) {
            root = new NbtCompound();
        }
        if (root.contains(FUJI_UUID)) return root;

        root.putString(FUJI_UUID, String.valueOf(UUID.randomUUID()));
        return root;
    }

    public static @NotNull NbtComponent addUuidToNbtComponentIfAbsent(@Nullable NbtComponent nbtComponent) {
        if (nbtComponent == null) {
            return NbtComponent.of(addUuidToNbtCompoundIfAbsent(null));
        }

        NbtCompound ret = addUuidToNbtCompoundIfAbsent(nbtComponent.copyNbt());
        return NbtComponent.of(ret);
    }

    public static @Nullable String computeUuid(@Nullable NbtComponent nbtComponent) {
        if (nbtComponent == null) return null;

        NbtCompound root = nbtComponent.copyNbt();

        if (!root.contains(FUJI_UUID)) return null;

        return root.getString(FUJI_UUID);
    }

    public static String formatString(World world, BlockPos blockPos) {
        String dimension = RegistryHelper.ofString(world);
        String pos = blockPos.getX() + "#" + blockPos.getY() + "#" + blockPos.getZ();
        return dimension + "#" + pos;
    }

    public static String computeUuid(World world, BlockPos blockPos) {
        return UUID.nameUUIDFromBytes(formatString(world, blockPos).getBytes()).toString();
    }

    public static @NotNull String getOrMakeUUIDNbt(ItemStack itemStack) {
        NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (computeUuid(nbtComponent) == null) {
            nbtComponent = addUuidToNbtComponentIfAbsent(nbtComponent);
            itemStack.set(DataComponentTypes.CUSTOM_DATA, nbtComponent);
        }

        //noinspection DataFlowIssue
        return computeUuid(nbtComponent);
    }

    public static String computeUuid(SpatialBlock spatialBlock) {
        return computeUuid(spatialBlock.ofDimension(), spatialBlock.ofBlockPos());
    }
}
