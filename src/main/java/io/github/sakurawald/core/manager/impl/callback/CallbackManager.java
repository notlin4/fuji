package io.github.sakurawald.core.manager.impl.callback;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.event.impl.CommandEvents;
import io.github.sakurawald.core.manager.abst.BaseManager;
import io.github.sakurawald.core.manager.impl.callback.structure.TTLMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CallbackManager extends BaseManager {
    private static final String COMMAND_CALLBACK = "command-callback";

    private final TTLMap<String, Consumer<ServerPlayerEntity>> uuid2consumer = new TTLMap<>();

    @Override
    public void onInitialize() {
        this.registerCLI();
    }

    private void registerCLI() {
        CommandEvents.REGISTRATION.register(((dispatcher, registryAccess, environment) -> dispatcher.register(
            literal(COMMAND_CALLBACK)
                .then(argument(CommandHelper.UUID, StringArgumentType.greedyString())
                    .executes(this::$executeCallback)
                ))));
    }

    private int $executeCallback(CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.playerOnlyCommand(ctx, player -> {
            String uuid = StringArgumentType.getString(ctx, CommandHelper.UUID);

            this.executeCallback(uuid, player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    private void executeCallback(String uuid, ServerPlayerEntity player) {
        Consumer<ServerPlayerEntity> consumer = this.uuid2consumer.get(uuid);
        if (consumer == null) {
            LocaleHelper.sendMessageByKey(player, "callback.invalid");
            return;
        }

        consumer.accept(player);
    }

    private ClickEvent makeCallback(String uuid, Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        LogUtil.debug("makeCallback: uuid = {}", uuid);
        this.uuid2consumer.put(uuid, callback, ttl, timeUnit);
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND_CALLBACK + " " + uuid);
    }

    public ClickEvent makeCallback(Consumer<ServerPlayerEntity> callback, long ttl, TimeUnit timeUnit) {
        return this.makeCallback(java.util.UUID.randomUUID().toString(), callback, ttl, timeUnit);
    }
}
