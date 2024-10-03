package io.github.sakurawald.module.initializer.command_warmup.structure;

import io.github.sakurawald.core.manager.impl.bossbar.structure.Interruptible;
import lombok.Data;

@Data
public class CommandWarmupEntry {


    final Command command;

    @Data
    public static class Command {
        final String regex;
        final int ms;
    }

    final Interruptible interruptible;

}
