package io.github.sakurawald.module.initializer.command_attachment.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class InteractTypeArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return InteractType.valueOf(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(InteractType.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("interact-type");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(CommandHelper.Suggestion.enums(InteractType.values()));
    }
}
