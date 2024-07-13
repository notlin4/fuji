package io.github.sakurawald.module.initializer.world;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.module.common.structure.Position;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.util.CommandUtil;
import io.github.sakurawald.util.MessageUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.server.command.CommandManager.argument;


public class WorldInitializer extends ModuleInitializer {

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("world").then(argument(CommandUtil.ARGUMENT_NAME_DIMENSION, DimensionArgumentType.dimension()).executes(this::$world)));
    }

    private int $world(CommandContext<ServerCommandSource> ctx) {
        return CommandUtil.playerOnlyCommand(ctx, player -> {

            try {
                ServerWorld serverWorld = DimensionArgumentType.getDimensionArgument(ctx, CommandUtil.ARGUMENT_NAME_DIMENSION);
                Position.of(player, serverWorld).teleport(player);
            } catch (CommandSyntaxException e) {
                MessageUtil.sendMessage(player,"dimension.no_found");
            }

            return Command.SINGLE_SUCCESS;
        });
    }
}