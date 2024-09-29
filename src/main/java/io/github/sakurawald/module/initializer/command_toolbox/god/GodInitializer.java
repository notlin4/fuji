package io.github.sakurawald.module.initializer.command_toolbox.god;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class GodInitializer extends ModuleInitializer {

    @CommandNode("god")
    @CommandRequirement(level = 4)
    private static int $god(@CommandSource ServerPlayerEntity player) {
        boolean flag = !player.getAbilities().invulnerable;
        player.getAbilities().invulnerable = flag;
        player.sendAbilitiesUpdate();

        LocaleHelper.sendMessageByKey(player, flag ? "god.on" : "god.off");
        return CommandHelper.Return.SUCCESS;
    }

}
