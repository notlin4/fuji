package io.github.sakurawald.module.initializer.command_toolbox.near;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;

public class NearInitializer extends ModuleInitializer {

    private static int distance(ServerPlayerEntity a, ServerPlayerEntity b) {
        if (a.getServerWorld() != b.getServerWorld()) return Integer.MAX_VALUE;
        return (int) a.getBlockPos().getSquaredDistance(b.getBlockPos().toCenterPos());
    }

    @CommandNode("near")
    private static int near(@CommandSource ServerPlayerEntity player, Optional<Integer> distance) {
        MinecraftServer server = ServerHelper.getDefaultServer();

        int $distance = distance.orElse(128);

        int sd = $distance * $distance;
        List<String> result = ServerHelper.getPlayers().stream().filter(p -> p != player && distance(player, p) <= sd).map(p -> p.getGameProfile().getName()).toList();

        LocaleHelper.sendMessageByKey(player, "near.format", result);
        return CommandHelper.Return.SUCCESS;
    }

}
