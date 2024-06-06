package io.github.sakurawald.module.mixin.afk;

import io.github.sakurawald.config.Configs;
import io.github.sakurawald.module.initializer.afk.ServerPlayerAfkStateAccessor;
import io.github.sakurawald.util.MessageUtil;
import net.kyori.adventure.text.TextReplacementConfig;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.sakurawald.util.MessageUtil.ofComponent;
import static io.github.sakurawald.util.MessageUtil.toVomponent;

@Mixin(ServerPlayerEntity.class)

public abstract class ServerPlayerMixin implements ServerPlayerAfkStateAccessor {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    @Shadow
    @Final
    public MinecraftServer server;

    @Unique
    private boolean afk = false;

    @Unique
    private long lastLastActionTime = 0;

    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    public void $getPlayerListName(CallbackInfoReturnable<Text> cir) {
        ServerPlayerAfkStateAccessor accessor = (ServerPlayerAfkStateAccessor) player;

        if (accessor.fuji$isAfk()) {
            cir.setReturnValue(Text.literal("afk " + player.getGameProfile().getName()));
            net.kyori.adventure.text.@NotNull Component component = ofComponent(Configs.configHandler.model().modules.afk.format)
                    .replaceText(TextReplacementConfig.builder().match("%player_display_name%").replacement(player.getDisplayName()).build());
            cir.setReturnValue(toVomponent(component));
        } else {
            cir.setReturnValue(null);
        }
    }


    @Inject(method = "updateLastActionTime", at = @At("HEAD"))
    public void $updateLastActionTime(CallbackInfo ci) {
        if (fuji$isAfk()) {
            fuji$setAfk(false);
        }
    }

    @Override
    public void fuji$setAfk(boolean flag) {
        this.afk = flag;
        this.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, (ServerPlayerEntity) (Object) this));
        MessageUtil.sendBroadcast(this.afk ? "afk.on.broadcast" : "afk.off.broadcast", this.player.getGameProfile().getName());
    }

    @Override
    public boolean fuji$isAfk() {
        return this.afk;
    }

    @Override
    public void fuji$setLastLastActionTime(long lastActionTime) {
        this.lastLastActionTime = lastActionTime;
    }

    @Override
    public long fuji$getLastLastActionTime() {
        return this.lastLastActionTime;
    }

}