package io.github.sakurawald.module.initializer.world;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.core.accessor.SimpleRegistryAccessor;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.event.impl.ServerTickEvents;
import io.github.sakurawald.core.event.impl.ServerWorldEvents;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.core.structure.TeleportTicket;
import io.github.sakurawald.module.initializer.world.accessor.IDimensionOptions;
import io.github.sakurawald.module.initializer.world.structure.DimensionEntry;
import io.github.sakurawald.module.initializer.world.structure.MyServerWorld;
import io.github.sakurawald.module.initializer.world.structure.MyWorldProperties;
import io.github.sakurawald.module.initializer.world.structure.VoidWorldGenerationProgressListener;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorldManager {

    private static final Set<ServerWorld> deletionQueue = new ReferenceOpenHashSet<>();
    private static final Set<DimensionEntry> creationQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> tick());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void requestToDeleteWorld(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();
        server.submit(() -> {
            deletionQueue.add(world);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void requestToCreateWorld(DimensionEntry dimensionEntry) {
        ServerHelper.getDefaultServer().submit(() -> {
            creationQueue.add(dimensionEntry);
        });
    }

    private static void tick() {
        if (!deletionQueue.isEmpty()) {
            deletionQueue.removeIf(WorldManager::tryDeleteWorld);
        }

        if (!creationQueue.isEmpty()) {
            creationQueue.removeIf(WorldManager::tryCreateWorld);
        }
    }

    private static boolean tryCreateWorld(@NotNull DimensionEntry dimensionEntry) {
        // wait until the deletion of this dimension is completed.
        if (deletionQueue.stream().anyMatch(it -> RegistryHelper.ofString(it).equals(dimensionEntry.getDimension()))) {
            return false;
        }

        // register the dimension
        MinecraftServer server = ServerHelper.getDefaultServer();
        Identifier dimension = Identifier.of(dimensionEntry.getDimension());
        Identifier dimensionType = Identifier.of(dimensionEntry.getDimension_type());
        long seed = dimensionEntry.getSeed();
        registerWorld(server, dimension, dimensionType, seed);
        return true;
    }

    private static boolean tryDeleteWorld(@NotNull ServerWorld world) {
        if (world.getPlayers().isEmpty()) {
            deleteWorld(world);
            return true;
        } else {
            kickPlayers(world);
            return false;
        }
    }

    private static void kickPlayers(@NotNull ServerWorld world) {
        if (world.getPlayers().isEmpty()) {
            return;
        }

        ServerWorld overworld = world.getServer().getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();

        List<ServerPlayerEntity> players = new ArrayList<>(world.getPlayers());
        for (ServerPlayerEntity player : players) {
            // fix: if the player is inside resource-world while resetting the worlds, then resource worlds will delay its deletion until the player left the resource-world.
            SpatialPose from = SpatialPose.of(player);
            SpatialPose to = new SpatialPose(overworld, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 0, 0);

            // totalMs = 1000, the value is unused
            TeleportTicket teleportTicket = TeleportTicket.makeVipTicket(player, from, to);
            Managers.getBossBarManager().addTicket(teleportTicket);
        }
    }

    private static void deleteWorld(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();

        RegistryKey<World> dimensionKey = world.getRegistryKey();
        if (server.worlds.remove(dimensionKey, world)) {
            // fire unload event
            ServerWorldEvents.UNLOAD.invoker().fire(server, world);

            // remove the entry from registry
            SimpleRegistry<DimensionOptions> dimensionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
            SimpleRegistryAccessor.remove(dimensionsRegistry, dimensionKey.getValue());

            // delete files
            File worldDirectory = server.session.getWorldDirectory(dimensionKey).toFile();
            deleteFiles(worldDirectory);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFiles(@NotNull File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File child : files) {
                if (child.isDirectory()) {
                    deleteFiles(child);
                } else {
                    child.delete();
                }
            }
        }
    }

    /**
     * To avoid share the same reference of the vanilla minecraft default `DimensionOptions` instance,
     * we must create new instance.
     */
    private static @NotNull DimensionOptions makeDimensionOptions(@NotNull Registry<DimensionOptions> registry, Identifier dimensionTypeIdentifier) {
        DimensionOptions template = registry.get(dimensionTypeIdentifier);
        if (template == null) {
            throw new IllegalArgumentException("The dimension type %s can't be used as template.".formatted(dimensionTypeIdentifier));
        }

        return new DimensionOptions(template.dimensionTypeEntry(), template.chunkGenerator());
    }

    @SuppressWarnings("deprecation")
    private static void registerWorld(@NotNull MinecraftServer server, Identifier dimensionIdentifier, @NotNull Identifier dimenstionTypeIdentifier, long seed) {
        /* create the world */
        // note: we use the same WorldData from OVERWORLD
        MyWorldProperties worldProperties = new MyWorldProperties(server.getSaveProperties(), seed);

        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, dimensionIdentifier);
        LogUtil.debug("make instance of world with registry key of type `World`: {}", worldRegistryKey);

        Registry<DimensionOptions> registry = RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        DimensionOptions dimensionOptions = makeDimensionOptions(registry, dimenstionTypeIdentifier);
        ((IDimensionOptions) (Object) dimensionOptions).fuji$setSaveProperties(false);

        ServerWorld world;
        try {
            world = new MyServerWorld(server,
                Util.getMainWorkerExecutor(),
                server.session,
                worldProperties,
                worldRegistryKey,
                dimensionOptions,
                VoidWorldGenerationProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(seed),
                ImmutableList.of(),
                true,
                null);
        } catch (Exception e) {
            LogUtil.error("failed to make ServerWorld instance: dimensionId = {}, dimensionTypeId = {}", dimensionIdentifier, dimenstionTypeIdentifier, e);
            return;
        }

        // start dragon fight if the dimension type is the end.
        if (dimenstionTypeIdentifier.equals(DimensionTypes.THE_END_ID)) {
            world.setEnderDragonFight(new EnderDragonFight(world, world.getSeed(), EnderDragonFight.Data.DEFAULT));
        }

        /* register the world */
        SimpleRegistry<DimensionOptions> dimensionOptionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        boolean original = ((SimpleRegistryAccessor<?>) dimensionOptionsRegistry).fuji$isFrozen();
        ((SimpleRegistryAccessor<?>) dimensionOptionsRegistry).fuji$setFrozen(false);

        RegistryKey<DimensionOptions> dimensionOptionsRegistryKey = RegistryKeys.toDimensionKey(worldRegistryKey);

        if (!dimensionOptionsRegistry.contains(dimensionOptionsRegistryKey)) {
            LogUtil.debug("add entry for dimension options registry: key = {}, value = {}", dimensionOptionsRegistryKey, dimensionOptions);
            dimensionOptionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, RegistryEntryInfo.DEFAULT);
        }
        ((SimpleRegistryAccessor<?>) dimensionOptionsRegistry).fuji$setFrozen(original);

        server.worlds.put(world.getRegistryKey(), world);
        ServerWorldEvents.LOAD.invoker().fire(server, world);

        world.tick(() -> true);
    }
}
