package io.github.sakurawald.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.command.argument.adapter.interfaces.AbstractArgumentTypeAdapter;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class CommandContextArgumentTypeAdapter extends AbstractArgumentTypeAdapter {

    @Override
    public boolean match(Type type) {
        return CommandContext.class.equals(type);
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        throw new UnsupportedOperationException("You should add `@CommandSource` annotation before the CommandContext<ServerCommandSource> !");
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Parameter parameter) {
        return context;
    }
}