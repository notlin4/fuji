package io.github.sakurawald.core.command.structure;

import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

/*

Cases:
1. A command is initialized by player alice, and executed as player bob.
2. A command is initialized by player alice, and executed as the console.
3. A command is initialized by the console, and executed as the console. (command scheduler)
4. A command is initialized by a player, and executed as the player. (interactive sign)

 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtendedCommandSource {

    @NotNull ServerCommandSource initiatingSource;
    @NotNull ServerCommandSource executingSource;
    boolean parsePlaceholder;

    public boolean sameSource() {
        return executingSource.getName().equals(initiatingSource.getName());
    }

    private ServerCommandSource getPlaceholderParsingSource() {
        // use the deepest source as the source for placeholder parsing.
        if (executingSource.isExecutedByPlayer()) {
            return executingSource;
        }

        return initiatingSource;
    }

    public String expandCommand(String string) {
        // escape the placeholder parsing.
        if (!this.parsePlaceholder) return string;

        ServerPlayerEntity contextualPlayer = getPlaceholderParsingSource().getPlayer();
        if (contextualPlayer != null) {
            string = LocaleHelper.resolvePlaceholder(contextualPlayer, string);
        } else {
            string = LocaleHelper.resolvePlaceholder(ServerHelper.getDefaultServer(), string);
        }

        return string;
    }

    public static ExtendedCommandSource fromSource(@NotNull ServerCommandSource initiatingSource) {
        return new ExtendedCommandSource(initiatingSource, initiatingSource, true);
    }

    public static ExtendedCommandSource asConsole(@NotNull ServerCommandSource initiatingSource, boolean parsePlaceholder) {
        return new ExtendedCommandSource(initiatingSource, ServerHelper.getDefaultServer().getCommandSource(), parsePlaceholder);
    }

    public static ExtendedCommandSource asPlayer(@NotNull ServerCommandSource initiatingSource, PlayerEntity executingPlayer, boolean parsePlaceholder) {
        return new ExtendedCommandSource(initiatingSource, executingPlayer.getCommandSource(), parsePlaceholder);
    }

    public static ExtendedCommandSource asFakeOp(@NotNull ServerCommandSource initiatingSource, PlayerEntity executingPlayer, boolean parsePlaceholder) {
        return new ExtendedCommandSource(initiatingSource, executingPlayer.getCommandSource().withLevel(4), parsePlaceholder);
    }

    public static ExtendedCommandSource asConsole(@NotNull ServerCommandSource initiatingSource) {
        return asConsole(initiatingSource, true);
    }


    public static ExtendedCommandSource asPlayer(@NotNull ServerCommandSource initiatingSource, PlayerEntity executingPlayer) {
        return asPlayer(initiatingSource, executingPlayer, true);
    }

    public static ExtendedCommandSource asFakeOp(@NotNull ServerCommandSource initiatingSource, PlayerEntity executingPlayer) {
        return asFakeOp(initiatingSource, executingPlayer, true);
    }

}
