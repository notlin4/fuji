package io.github.sakurawald.module.initializer.pvp;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.config.handler.ConfigHandler;
import io.github.sakurawald.config.handler.ObjectConfigHandler;
import io.github.sakurawald.config.model.PvPModel;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.util.CommandUtil;
import io.github.sakurawald.util.MessageUtil;
import java.util.HashSet;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;


public class PvpModule extends ModuleInitializer {

    public static final ConfigHandler<PvPModel> pvpHandler = new ObjectConfigHandler<>("pvp.json", PvPModel.class);

    @Override
    public void onInitialize() {
        pvpHandler.loadFromDisk();
    }

    @Override
    public void onReload() {
        pvpHandler.loadFromDisk();
    }

    @SuppressWarnings("unused")
    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("pvp")
                        .then(CommandManager.literal("on").executes(this::$on))
                        .then(CommandManager.literal("off").executes(this::$off))
                        .then(CommandManager.literal("list").executes(this::$list))
                        .then(CommandManager.literal("status").executes(this::$status))
        );
    }

    private int $on(CommandContext<ServerCommandSource> ctx) {
        return CommandUtil.playerOnlyCommand(ctx, player -> {
            HashSet<String> whitelist = pvpHandler.model().whitelist;
            String name = player.getGameProfile().getName();
            if (!whitelist.contains(name)) {
                whitelist.add(name);
                pvpHandler.saveToDisk();

                MessageUtil.sendMessage(player, "pvp.on");

                return Command.SINGLE_SUCCESS;
            }

            MessageUtil.sendMessage(player, "pvp.on.already");
            return Command.SINGLE_SUCCESS;
        });
    }

    private int $off(CommandContext<ServerCommandSource> ctx) {
        return CommandUtil.playerOnlyCommand(ctx, player -> {
            HashSet<String> whitelist = pvpHandler.model().whitelist;
            String name = player.getGameProfile().getName();
            if (whitelist.contains(name)) {
                whitelist.remove(name);
                pvpHandler.saveToDisk();

                MessageUtil.sendMessage(player, "pvp.off");
                return Command.SINGLE_SUCCESS;
            }

            MessageUtil.sendMessage(player, "pvp.off.already");
            return 0;
        });
    }

    @SuppressWarnings("SameReturnValue")
    private int $status(CommandContext<ServerCommandSource> ctx) {
        return CommandUtil.playerOnlyCommand(ctx, player -> {
            HashSet<String> whitelist = pvpHandler.model().whitelist;
            player.sendMessage(MessageUtil.ofComponent(player, "pvp.status")
                    .append(whitelist.contains(player.getGameProfile().getName()) ? MessageUtil.ofComponent(player, "on") : MessageUtil.ofComponent(player, "off")));
            return Command.SINGLE_SUCCESS;
        });
    }

    private int $list(CommandContext<ServerCommandSource> ctx) {
        HashSet<String> whitelist = pvpHandler.model().whitelist;
        MessageUtil.sendMessage(ctx.getSource(), "pvp.list", whitelist);
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean contains(String name) {
        return pvpHandler.model().whitelist.contains(name);
    }

}