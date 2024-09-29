package io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.DateUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager.config.model.FakePlayerManagerConfigModel;
import io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager.job.ManageFakePlayersJob;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FakePlayerManagerInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<FakePlayerManagerConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, FakePlayerManagerConfigModel.class);

    public static final List<String> CONSTANT_EMPTY_LIST = new ArrayList<>();
    public static final Map<String, List<String>> player2fakePlayers = new HashMap<>();
    public static final Map<String, Long> player2expiration = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> new ManageFakePlayersJob().schedule());
    }

    @CommandNode("player renew")
    private static int $renew(@CommandSource ServerPlayerEntity player) {
        renewFakePlayers(player);
        return CommandHelper.Return.SUCCESS;
    }


    @CommandNode("player who")
    private static int $who(@CommandSource CommandContext<ServerCommandSource> context) {
        /* validate */
        validateFakePlayers();

        /* output */
        StringBuilder builder = new StringBuilder();
        for (String player : player2fakePlayers.keySet()) {
            List<String> fakePlayers = player2fakePlayers.get(player);
            if (fakePlayers.isEmpty()) continue;
            builder.append(player).append(": ");
            for (String fakePlayer : fakePlayers) {
                builder.append(fakePlayer).append(" ");
            }
            builder.append("\n");
        }
        ServerCommandSource source = context.getSource();
        source.sendMessage(LocaleHelper.getTextByKey(source, "fake_player_manager.who.header").copy().append(Text.literal(builder.toString())));
        return CommandHelper.Return.SUCCESS;
    }

    public static void renewFakePlayers(@NotNull ServerPlayerEntity player) {
        String name = player.getGameProfile().getName();
        int duration = config.getModel().renew_duration_ms;
        long newTime = System.currentTimeMillis() + duration;
        player2expiration.put(name, newTime);
        LocaleHelper.sendMessageByKey(player, "fake_player_manager.renew.success", DateUtil.toStandardDateFormat(newTime));
    }

    public static void validateFakePlayers() {
        /* remove invalid fake-player */
        Iterator<Map.Entry<String, List<String>>> it = player2fakePlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();

            List<String> myFakePlayers = entry.getValue();
            // fix: NPE
            if (myFakePlayers == null || myFakePlayers.isEmpty()) {
                it.remove();
                continue;
            }
            myFakePlayers.removeIf(name -> {
                ServerPlayerEntity fakePlayer = ServerHelper.getDefaultServer().getPlayerManager().getPlayer(name);
                return fakePlayer == null || fakePlayer.isRemoved();
            });
        }
    }

    public static boolean canSpawnFakePlayer(@NotNull ServerPlayerEntity player) {
        /* validate */
        validateFakePlayers();

        /* check */
        int limit = getCurrentAmountLimit();
        int current = player2fakePlayers.getOrDefault(player.getGameProfile().getName(), CONSTANT_EMPTY_LIST).size();
        return current < limit;
    }

    public static void addFakePlayer(@NotNull ServerPlayerEntity player, String fakePlayer) {
        player2fakePlayers.computeIfAbsent(player.getGameProfile().getName(), k -> new ArrayList<>()).add(fakePlayer);
    }

    public static boolean canManipulateFakePlayer(@NotNull CommandContext<ServerCommandSource> ctx, String fakePlayer) {
        // IMPORTANT: disable /player ... shadow command for online-player
        if (ctx.getNodes().get(2).getNode().getName().equals("shadow")) return false;

        // bypass: console
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return true;

        // bypass: op
        if (ServerHelper.getDefaultServer().getPlayerManager().isOperator(player.getGameProfile())) return true;

        List<String> myFakePlayers = player2fakePlayers.getOrDefault(player.getGameProfile().getName(), CONSTANT_EMPTY_LIST);
        return myFakePlayers.contains(fakePlayer);
    }

    public static int getCurrentAmountLimit() {
        List<List<Integer>> rules = config.getModel().caps_limit_rule;
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        int currentDays = currentDate.getDayOfWeek().getValue();
        int currentMinutes = currentTime.getHour() * 60 + currentTime.getMinute();
        for (List<Integer> rule : rules) {
            if (currentDays >= rule.get(0) && currentMinutes >= rule.get(1)) return rule.get(2);
        }
        return -1;
    }

    public static boolean isMyFakePlayer(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity fakePlayer) {
        return player2fakePlayers.getOrDefault(player.getGameProfile().getName(), CONSTANT_EMPTY_LIST).contains(fakePlayer.getGameProfile().getName());
    }

    public @NotNull GameProfile createOfflineGameProfile(@NotNull String fakePlayerName) {
        UUID offlinePlayerUUID = Uuids.getOfflinePlayerUuid(fakePlayerName);
        return new GameProfile(offlinePlayerUUID, fakePlayerName);
    }

}
