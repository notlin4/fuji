package io.github.sakurawald.module.initializer.command_warmup.structure;

import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.manager.impl.bossbar.structure.InterruptibleTicket;
import io.github.sakurawald.core.structure.SpatialPose;
import lombok.Getter;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@Getter
public class CommandWarmupTicket extends InterruptibleTicket {

    private final String input;

    public CommandWarmupTicket(@NotNull ServerPlayerEntity player, @NotNull String input, CommandWarmupEntry entry) {
        super(new ServerBossBar(LocaleHelper.getTextByKey(player, "command_warmup.bossbar.name", input), net.minecraft.entity.boss.BossBar.Color.GREEN, net.minecraft.entity.boss.BossBar.Style.PROGRESS)
            , entry.getCommand().getMs()
            , player
            , SpatialPose.of(player)
            , entry.getInterruptible());

        this.input = input;
    }

    @Override
    protected void onComplete() {
        player.networkHandler.executeCommand(input);
    }

    public static CommandWarmupTicket make(ServerPlayerEntity player, String input, CommandWarmupEntry entry) {
        return new CommandWarmupTicket(player, input, entry);
    }

}
