package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import lombok.Data;
import lombok.With;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@Data
@With
public class SpatialPose {
    final String level;
    final double x;
    final double y;
    final double z;
    final float yaw;
    final float pitch;

    public SpatialPose(String level, double x, double y, double z, float yaw, float pitch) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SpatialPose(@NotNull World level, double x, double y, double z, float yaw, float pitch) {
        this(level.getRegistryKey().getValue().toString(), x, y, z, yaw, pitch);
    }

    public static @NotNull SpatialPose of(@NotNull ServerPlayerEntity player) {
        return new SpatialPose(player.getWorld().getRegistryKey().getValue().toString(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
    }

    public static @NotNull SpatialPose of(@NotNull ServerPlayerEntity player, @NotNull ServerWorld world) {
        BlockPos spawnPos = world.getSpawnPos();
        return new SpatialPose(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.getYaw(), player.getPitch());
    }

    @SuppressWarnings("unused")
    public ServerWorld ofDimension() {
        return RegistryHelper.ofServerWorld(Identifier.of(this.level));
    }

    @SuppressWarnings("unused")
    public @NotNull BlockPos ofBlockPos() {
        return new BlockPos((int) this.x, (int) this.y, (int) this.z);
    }

    public boolean sameLevel(@NotNull World level) {
        return this.level.equals(level.getRegistryKey().getValue().toString());
    }

    @SuppressWarnings("unused")
    public double distanceToSqr(@NotNull SpatialPose spatialPose) {
        if (!this.level.equals(spatialPose.level)) return Double.MAX_VALUE;
        double x = this.x - spatialPose.x;
        double y = this.y - spatialPose.y;
        double z = this.z - spatialPose.z;
        return x * x + y * y + z * z;
    }

    public void teleport(@NotNull ServerPlayerEntity player) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(this.level));
        ServerWorld serverLevel = ServerHelper.getDefaultServer().getWorld(worldKey);
        if (serverLevel == null) {
            LocaleHelper.sendMessageByKey(player, "world.dimension.not_found", this.level);
            return;
        }

        player.teleport(serverLevel, this.x, this.y, this.z, this.yaw, this.pitch);
    }
}
