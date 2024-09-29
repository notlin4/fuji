package io.github.sakurawald.module.initializer.command_toolbox.reply;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;


public class ReplyInitializer extends ModuleInitializer {

    private static final HashMap<String, String> player2target = new HashMap<>();

    public static void updateReplyTarget(String player, String target) {
        player2target.put(player, target);
    }


    @CommandNode("reply")
    private static int $reply(@CommandSource ServerPlayerEntity player, GreedyString message) {
        String target = player2target.get(player.getGameProfile().getName());

        try {
            ServerHelper.getCommandDispatcher().execute("msg %s %s".formatted(target, message.getValue()), player.getCommandSource());
        } catch (CommandSyntaxException e) {
            LocaleHelper.sendMessageByKey(player, "reply.no_target");
        }

        return CommandHelper.Return.SUCCESS;
    }

}
