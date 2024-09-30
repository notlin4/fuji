package io.github.sakurawald.module.initializer.kit.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.module.initializer.kit.KitInitializer;
import io.github.sakurawald.module.initializer.kit.command.argument.wrapper.KitName;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Type;

public class KitNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public boolean match(Type type) {
        return KitName.class.equals(type);
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new KitName(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests((context, builder) -> {
            KitInitializer.getKitNameList().forEach(builder::suggest);
            return builder.buildFuture();
        });
    }
}
