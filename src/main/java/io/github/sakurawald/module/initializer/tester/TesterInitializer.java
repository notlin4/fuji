package io.github.sakurawald.module.initializer.tester;

import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import lombok.SneakyThrows;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of("run"));


        return 1;
    }

    @CommandNode("$1 minus $2")
    private static int $2(@CommandSource ServerPlayerEntity player, Integer a, Integer b) {
        player.sendMessage(Text.of(String.valueOf(a - b)));
        return 1;
    }

    @CommandNode
    private static int root(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of("root"));
        return 1;
    }

    @CommandNode("register-event")
    private static int registerEvent(@CommandSource ServerPlayerEntity player) {

        return 1;
    }

    @CommandNode("trigger-event")
    private static int triggerEvent(@CommandSource ServerPlayerEntity player) {

        return 1;
    }

}
