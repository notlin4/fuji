package io.github.sakurawald.module.mixin.better_fake_player;

import carpet.patches.EntityPlayerMPFake;
import io.github.sakurawald.module.ModuleManager;
import io.github.sakurawald.module.initializer.better_fake_player.BetterFakePlayerModule;
import io.github.sakurawald.util.MessageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


// the carpet-fabric default event handler priority is 1000
@Mixin(value = PlayerEntity.class, priority = 999)

public abstract class PlayerMixin extends LivingEntity {

    @Unique
    private static final BetterFakePlayerModule betterFakePlayerModule = ModuleManager.getInitializer(BetterFakePlayerModule.class);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void $interact(Entity target, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (target instanceof EntityPlayerMPFake fakePlayer) {
            ServerPlayerEntity source = (ServerPlayerEntity) (Object) this;
            if (!betterFakePlayerModule.isMyFakePlayer(source, fakePlayer)) {
                // cancel this event
                cir.setReturnValue(ActionResult.FAIL);

                // main-hand and off-hand will both trigger this event
                if (hand == Hand.MAIN_HAND) {
                    MessageUtil.sendMessage(source, "better_fake_player.manipulate.forbidden");
                }
            }
        }
    }

}