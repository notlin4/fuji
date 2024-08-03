package io.github.sakurawald.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.command.argument.adapter.interfaces.AbstractArgumentTypeAdapter;
import io.github.sakurawald.command.argument.wrapper.OfflinePlayerName;
import io.github.sakurawald.util.minecraft.CommandHelper;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class OfflinePlayerArgumentTypeAdapter extends AbstractArgumentTypeAdapter {

    @Override
    public boolean match(Type type) {
        return OfflinePlayerName.class.equals(type);
    }

    @Override
    public ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(Parameter parameter) {
        return super.makeRequiredArgumentBuilder(parameter).suggests(CommandHelper.Suggestion.offlinePlayers());
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Parameter parameter) {
        return new OfflinePlayerName(StringArgumentType.getString(context, parameter.getName()));
    }
}