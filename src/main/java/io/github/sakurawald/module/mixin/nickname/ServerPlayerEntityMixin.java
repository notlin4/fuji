package io.github.sakurawald.module.mixin.nickname;

import io.github.sakurawald.module.initializer.nickname.NicknameInitializer;
import io.github.sakurawald.util.MessageUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    PlayerEntity player = (PlayerEntity) (Object) this;

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    void getDisplayName(CallbackInfoReturnable<Text> cir) {
        String format = NicknameInitializer.getNicknameHandler().model().format.player2format.get(player.getGameProfile().getName());

        if (format != null) {
            cir.setReturnValue(MessageUtil.ofText(format));
        }
    }

}