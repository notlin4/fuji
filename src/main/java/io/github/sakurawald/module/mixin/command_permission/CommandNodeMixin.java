package io.github.sakurawald.module.mixin.command_permission;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.module.initializer.command_permission.CommandPermissionInitializer;
import io.github.sakurawald.module.initializer.command_permission.structure.WrappedPredicate;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = CommandNode.class, remap = false)
public class CommandNodeMixin {

    @Mutable
    @Shadow
    @Final
    private Predicate<ServerCommandSource> requirement;

    @SuppressWarnings("unchecked")
    @Unique
    final CommandNode<ServerCommandSource> node = (CommandNode<ServerCommandSource>) (Object) this;

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "getRequirement", at = @At("TAIL"))
    private Predicate<?> injected(Predicate<?> original) {
        // wrap the predicate until the dispatcher is initialized.
        CommandDispatcher<ServerCommandSource> dispatcher = ServerHelper.getCommandDispatcher();
        if (dispatcher != null && !(original instanceof WrappedPredicate<?>)) {
            String path = CommandHelper.computeCommandNodePath(node);
            requirement = CommandPermissionInitializer.makeWrappedPredicate(path, (Predicate<ServerCommandSource>) original);
            return requirement;
        }

        return original;
    }

}
