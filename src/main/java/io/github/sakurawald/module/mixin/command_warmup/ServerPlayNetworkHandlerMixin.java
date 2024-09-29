package io.github.sakurawald.module.mixin.command_warmup;

import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.module.initializer.command_warmup.CommandWarmupInitializer;
import io.github.sakurawald.module.initializer.command_warmup.structure.CommandWarmupTicket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1000 - 500)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    public void interceptCommandUsagePackets(@NotNull CommandExecutionC2SPacket commandExecutionC2SPacket, @NotNull CallbackInfo ci) {
        String command = commandExecutionC2SPacket.comp_808();

        int ms = CommandWarmupInitializer.command2ms(command);
        if (ms > 0) {
            Managers.getBossBarManager().addTicket(CommandWarmupTicket.make(player, command, ms));
            ci.cancel();
        }
    }
}
