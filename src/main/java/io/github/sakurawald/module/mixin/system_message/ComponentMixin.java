package io.github.sakurawald.module.mixin.system_message;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.config.Configs;
import io.github.sakurawald.util.MessageUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import static io.github.sakurawald.Fuji.LOGGER;

@Mixin(Text.class)
public interface ComponentMixin {

    @Inject(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"), cancellable = true)
    private static void translatable(String key, Object[] args, CallbackInfoReturnable<MutableText> cir) {
        MutableText newValue = transform(key, args);
        if (newValue != null) cir.setReturnValue(newValue);
    }

    @Inject(method = "translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"), cancellable = true)
    private static void translatable(String key, CallbackInfoReturnable<MutableText> cir) {
        MutableText newValue = transform(key);
        if (newValue != null) cir.setReturnValue(newValue);
    }

    @Unique
    private static @Nullable MutableText transform(String key, Object... args) {
        Map<String, String> key2value = Configs.configHandler.model().modules.system_message.key2value;
        if (key2value.containsKey(key)) {
            if (Fuji.SERVER == null) {
                LOGGER.warn("Server is null currently -> cannot hijack message key: {}", key);
                return null;
            }
            String value = key2value.get(key);
            String miniMessageSource = MutableText.of(new TranslatableTextContent("force_fallback", value, args)).getString();
            return MessageUtil.ofVomponent(miniMessageSource).copy();
        }
        return null;
    }
}