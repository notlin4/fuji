package io.github.sakurawald.module.mixin.command_cooldown;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.module.initializer.command_cooldown.CommandCooldownInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    public void watchCommandExecution(@NotNull ParseResults<ServerCommandSource> parseResults, String string, @NotNull CallbackInfo ci) {
        ServerPlayerEntity player = parseResults.getContext().getSource().getPlayer();
        if (player == null) return;

        long cooldownMs = CommandCooldownInitializer.computeLeftTime(player, string);

        if (cooldownMs > 0) {
            LocaleHelper.sendActionBarByKey(player, "command_cooldown.cooldown", cooldownMs / 1000);
            ci.cancel();
        }
    }
}
