package io.github.sakurawald.command.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.command.adapter.ArgumentTypeAdapter;
import io.github.sakurawald.util.minecraft.CommandHelper;
import io.github.sakurawald.util.minecraft.MessageHelper;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class PlayerArgumentTypeAdapter extends ArgumentTypeAdapter {

    @Override
    public boolean match(Type type) {
        return ServerPlayerEntity.class.equals(type);
    }

    @Override
    public ArgumentType<?> makeArgumentType() {
        return EntityArgumentType.player();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Parameter parameter) {
        return context.getSource().getPlayer();
    }

    @Override
    public boolean validateCommandSource(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            MessageHelper.sendMessage(context.getSource(), "command.player_only");
            return false;
        }

        return true;
    }
}
