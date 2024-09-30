package io.github.sakurawald.module.initializer.placeholder;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.auxiliary.DateUtil;
import io.github.sakurawald.core.auxiliary.RandomUtil;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.placeholder.job.UpdateSumUpPlaceholderJob;
import io.github.sakurawald.module.initializer.placeholder.structure.SumUpPlaceholder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlaceholderInitializer extends ModuleInitializer {
    private final Map<String, Map<String, String>> rotate = new HashMap<>();

    private static final String NO_PLAYER = "no player";
    private static final Pattern ESCAPE_PARSER = Pattern.compile("\\s*([\\s\\S]+)\\s+(\\d+)\\s*");

    @Override
    public void onInitialize() {
        /* register placeholders */
        registerPlayerMinedPlaceholder();
        registerServerMinedPlaceholder();

        registerPlayerPlacedPlaceholder();
        registerServerPlacedPlaceholder();

        registerPlayerKilledPlaceholder();
        registerServerKilledPlaceholder();


        registerPlayerMovedPlaceholder();
        registerServerMovedPlaceholder();

        registerPlayerPlaytimePlaceholder();
        registerServerPlaytimePlaceholder();

        registerHealthBarPlaceholder();
        registerRotatePlaceholder();
        registerHasPermissionPlaceholder();
        registerGetMetaPlaceholder();
        registerRandomPlayerPlaceholder();
        registerRandomPlaceholder();
        registerEscapePlaceholder();
        registerDatePlaceholder();

        /* events */
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SumUpPlaceholder.ofServer();
            new UpdateSumUpPlaceholderJob().schedule();
        });
    }

    private void registerDatePlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "date"),
                (ctx, arg) -> {

                    if (arg == null || arg.isEmpty()) {
                        return PlaceholderResult.value(Text.literal(DateUtil.getCurrentDate()));
                    }

                    try {
                        String currentDate = DateUtil.getCurrentDate(new SimpleDateFormat(arg));
                        return PlaceholderResult.value(Text.literal(currentDate));
                    } catch (Exception e) {
                        return PlaceholderResult.invalid("Invalid date formatter: " + arg);
                    }
                });
    }

    private void registerEscapePlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "escape"), (ctx, args) -> {
            if (args == null) return PlaceholderResult.invalid();

            Matcher matcher = ESCAPE_PARSER.matcher(args);
            if (matcher.find()) {
                String placeholder = matcher.group(1);
                int level = Integer.parseInt(matcher.group(2));

                if (level == 1) return PlaceholderResult.value(Text.literal("%" + placeholder + "%"));
                if (level > 1) return PlaceholderResult.value(Text.literal("%fuji:escape " + placeholder + " " + (level - 1) + "%"));

            }

            return PlaceholderResult.value(Text.literal("%" + args + "%"));
        });
    }

    private void registerHasPermissionPlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "has_permission"), (ctx, args) -> {
            if (ctx.player() == null) {
                return PlaceholderResult.invalid();
            }

            boolean value = PermissionHelper.hasPermission(ctx.player().getUuid(), args);
            return PlaceholderResult.value(Text.literal(String.valueOf(value)));
        });
    }

    private void registerGetMetaPlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "get_meta"), (ctx, args) -> {
            if (ctx.player() == null) {
                return PlaceholderResult.invalid();
            }

            Optional<String> o = PermissionHelper.getMeta(ctx.player().getUuid(), args, String::valueOf);
            String value = o.orElse("NOT_EXIST");
            return PlaceholderResult.value(Text.literal(value));
        });
    }

    private void registerRandomPlayerPlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "random_player"), (ctx, args) -> {
            List<ServerPlayerEntity> playerList = ServerHelper.getPlayers();
            ServerPlayerEntity serverPlayerEntity = RandomUtil.drawList(playerList);
            return PlaceholderResult.value(Text.literal(serverPlayerEntity.getGameProfile().getName()));
        });
    }

    private void registerRandomPlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "random"), (ctx, args) -> {
            if (args == null) return PlaceholderResult.invalid();
            String[] split = args.split(" ");
            if (split.length != 2) return PlaceholderResult.invalid();

            int i;
            try {
                i = RandomUtil.getRandom().nextInt(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } catch (Exception e) {
                return PlaceholderResult.invalid();
            }

            return PlaceholderResult.value(Text.literal(String.valueOf(i)));
        });
    }

    private void registerHealthBarPlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "health_bar"), (ctx, args) -> {
            if (ctx.player() == null) {
                return PlaceholderResult.invalid();
            }

            ServerPlayerEntity player = ctx.player();

            int totalHearts = 10;
            int filledHearts = (int) (player.getHealth() / 2);
            int unfilledHearts = totalHearts - filledHearts;
            String str = "♥".repeat(filledHearts) + "♡".repeat(unfilledHearts);
            return PlaceholderResult.value(Text.literal(str));
        });
    }

    private void registerRotatePlaceholder() {
        Placeholders.register(Identifier.of(Fuji.MOD_ID, "rotate"), (ctx, args) -> {
            String namespace = "default";
            if (ctx.player() != null) {
                namespace = ctx.player().getGameProfile().getName();
            }

            rotate.putIfAbsent(namespace, new HashMap<>());
            Map<String, String> rotateMap = rotate.get(namespace);
            rotateMap.putIfAbsent(args, args);

            String frame = rotateMap.get(args);
            rotateMap.put(args, StringUtils.rotate(frame, -1));

            return PlaceholderResult.value(Text.literal(frame));
        });
    }


    private static void registerServerPlaytimePlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "server_playtime"),
                (ctx, arg) -> {
                    SumUpPlaceholder sumUpPlaceholder = SumUpPlaceholder.ofServer();
                    return PlaceholderResult.value(Text.literal(String.valueOf(sumUpPlaceholder.playtime)));
                });
    }

    private static void registerPlayerPlaytimePlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "player_playtime"),
                (ctx, arg) -> {
                    if (ctx.player() == null) PlaceholderResult.invalid(NO_PLAYER);
                    SumUpPlaceholder sumUpPlaceholder = SumUpPlaceholder.ofPlayer(ctx.player().getUuidAsString());
                    return PlaceholderResult.value(Text.literal(String.valueOf(sumUpPlaceholder.playtime)));
                });
    }

    private static void registerServerMovedPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "server_moved"),
                (ctx, arg) -> {
                    SumUpPlaceholder sumUpPlaceholder = SumUpPlaceholder.ofServer();
                    return PlaceholderResult.value(Text.literal(String.valueOf(sumUpPlaceholder.moved)));
                });
    }

    private static void registerPlayerMovedPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "player_moved"),
                (ctx, arg) -> {
                    if (ctx.player() == null) PlaceholderResult.invalid(NO_PLAYER);
                    SumUpPlaceholder sumUpPlaceholder = SumUpPlaceholder.ofPlayer(ctx.player().getUuidAsString());
                    return PlaceholderResult.value(Text.literal(String.valueOf(sumUpPlaceholder.moved)));
                });
    }

    private static void registerServerKilledPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "server_killed"),
                (ctx, arg) -> PlaceholderResult.value(Text.literal(String.valueOf(SumUpPlaceholder.ofServer().killed))));
    }

    private static void registerPlayerKilledPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "player_killed"),
                (ctx, arg) -> {
                    if (ctx.player() == null) PlaceholderResult.invalid(NO_PLAYER);
                    return PlaceholderResult.value(Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(ctx.player().getUuidAsString()).killed)));
                });
    }

    private static void registerServerPlacedPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "server_placed"),
                (ctx, arg) -> PlaceholderResult.value(Text.literal(String.valueOf(SumUpPlaceholder.ofServer().placed))));
    }

    private static void registerPlayerPlacedPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "player_placed"),
                (ctx, arg) -> {
                    if (ctx.player() == null) PlaceholderResult.invalid(NO_PLAYER);
                    return PlaceholderResult.value(Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(ctx.player().getUuidAsString()).placed)));
                });
    }

    private static void registerServerMinedPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "server_mined"),
                (ctx, arg) -> PlaceholderResult.value(Text.literal(String.valueOf(SumUpPlaceholder.ofServer().mined))));
    }

    private static void registerPlayerMinedPlaceholder() {
        Placeholders.register(
                Identifier.of(Fuji.MOD_ID, "player_mined"),
                (ctx, arg) -> {
                    if (ctx.player() == null) PlaceholderResult.invalid(NO_PLAYER);
                    return PlaceholderResult.value(Text.literal(String.valueOf(SumUpPlaceholder.ofPlayer(ctx.player().getUuidAsString()).mined)));
                });
    }

}
