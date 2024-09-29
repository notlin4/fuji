package io.github.sakurawald.core.service.random_teleport;

import com.google.common.base.Stopwatch;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.service.random_teleport.structure.HeightFinder;
import io.github.sakurawald.core.service.random_teleport.structure.HeightFindingStrategy;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.core.structure.TeleportSetup;
import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Cite("https://github.com/John-Paul-R/Essential-Commands")
@UtilityClass
public class RandomTeleporter {

    public static void request(@NotNull ServerPlayerEntity player, @NotNull TeleportSetup setup, @Nullable Consumer<SpatialPose> postConsumer) {
        CompletableFuture.runAsync(() -> {
            LogUtil.info("request rtp: {}", player.getGameProfile().getName());
            Stopwatch timer = Stopwatch.createStarted();

            ServerWorld world = RegistryHelper.ofServerWorld(Identifier.of(setup.getDimension()));
            if (world == null) {
                LocaleHelper.sendMessageByKey(player,"world.dimension.not_found");
                return;
            }

            Optional<BlockPos> result;

            int triedTimes = 0;
            do {
                triedTimes++;
                result = searchPosition(setup);
            } while (result.isEmpty() && triedTimes <= setup.getMaxTryTimes());

            if (result.isEmpty()) {
                LocaleHelper.sendMessageByKey(player, "rtp.fail");
                return;
            }

            // teleport the player
            SpatialPose spatialPose = new SpatialPose(world, result.get().getX() + 0.5, result.get().getY(), result.get().getZ() + 0.5, 0, 0);
            ServerHelper.getDefaultServer().executeSync(()-> {
                // run the teleport action in main-thread
                spatialPose.teleport(player);
            });

            // post consumer
            if (postConsumer != null) {
                postConsumer.accept(spatialPose);
            }

            // cost
            var cost = timer.stop();
            LogUtil.info("response rtp: {} has been teleported to ({} {} {} {}) (cost = {})", player.getGameProfile().getName(), world.getRegistryKey().getValue(), result.get().getX(), result.get().getY(), result.get().getZ(), cost);
        });
    }

    private static @NotNull Optional<BlockPos> searchPosition(@NotNull TeleportSetup setup) {
        // Search for a valid y-level (not in a block, underwater, out of the world, etc.)
        final BlockPos targetXZ = getRandomXZ(setup);

        ServerWorld serverWorld = setup.ofWorld();
        final Chunk chunk = serverWorld.getChunk(targetXZ);

        for (BlockPos.Mutable candidateBlock : getChunkCandidateBlocks(chunk.getPos())) {
            final int x = candidateBlock.getX();
            final int z = candidateBlock.getZ();

            HeightFinder heightFinder = HeightFindingStrategy.forWorld(setup.ofWorld());
            final OptionalInt yOpt = heightFinder.getY(chunk, x, z);
            if (yOpt.isEmpty()) {
                continue;
            }
            final int y = yOpt.getAsInt();

            if (isSatisfied(setup, chunk, new BlockPos(x, y - 2, z))) {
                return Optional.of(new BlockPos(x, y, z));
            }
        }

        return Optional.empty();
    }

    private static @NotNull BlockPos getRandomXZ(@NotNull TeleportSetup setup) {
        return setup.isCircle() ? getRandomXZWithCircle(setup) : getRandomXZWithRect(setup);
    }

    private static @NotNull BlockPos getRandomXZWithCircle(@NotNull TeleportSetup setup) {
        var rand = new Random();

        int r_min = setup.getMinRange();
        int r_max = setup.getMaxRange();
        int r = (r_max == r_min
                ? r_max
                : rand.nextInt(r_min, r_max));
        final double angle = rand.nextDouble() * 2 * Math.PI;
        final double delta_x = r * Math.cos(angle);
        final double delta_z = r * Math.sin(angle);
        int x = setup.getCenterX() + (int) delta_x;
        int z = setup.getCenterZ() + (int) delta_z;
        return new BlockPos(x, 0, z);
    }

    private static @NotNull BlockPos getRandomXZWithRect(@NotNull TeleportSetup setup) {
        var rand = new Random();
        int r_min = setup.getMinRange();
        int r_max = setup.getMaxRange();

        int x = setup.getCenterX() + rand.nextInt(r_min, r_max);
        int z = setup.getCenterZ() + rand.nextInt(r_min, r_max);
        return new BlockPos(x, 0, z);
    }

    private static boolean isSatisfied(@NotNull TeleportSetup setup, @NotNull Chunk chunk, @NotNull BlockPos pos) {
        BlockState blockState = chunk.getBlockState(pos);
        return pos.getY() >= setup.getMinY()
                && pos.getY() <= setup.getMaxY()
                && blockState.getFluidState().isEmpty()
                && blockState.getBlock() != Blocks.POWDER_SNOW
                && blockState.getBlock() != Blocks.FIRE
                && pos.getY() >= chunk.getBottomY()
                && pos.getY() <= chunk.getTopY();
    }

    public static @NotNull Iterable<BlockPos.Mutable> getChunkCandidateBlocks(@NotNull ChunkPos chunkPos) {
        return () -> new Iterator<>() {
            private final BlockPos.Mutable bp = new BlockPos.Mutable();
            private int i = -1;

            @Override
            public boolean hasNext() {
                return i < 4;
            }

            @Override
            public BlockPos.Mutable next() {
                i++;
                return switch (i) {
                    case 0 -> bp.set(chunkPos.getStartX(), 0, chunkPos.getStartZ());
                    case 1 -> bp.set(chunkPos.getStartX(), 0, chunkPos.getEndZ());
                    case 2 -> bp.set(chunkPos.getEndX(), 0, chunkPos.getStartZ());
                    case 3 -> bp.set(chunkPos.getEndX(), 0, chunkPos.getEndZ());
                    case 4 -> bp.set(chunkPos.getCenterX(), 0, chunkPos.getCenterZ());
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                };
            }
        };
    }

}
