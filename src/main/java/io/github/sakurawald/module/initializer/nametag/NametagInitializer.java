package io.github.sakurawald.module.initializer.nametag;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.LocaleHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.nametag.config.model.NametagConfigModel;
import io.github.sakurawald.module.initializer.nametag.job.UpdateNametagJob;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class NametagInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<NametagConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, NametagConfigModel.class);

    private static Map<ServerPlayerEntity, DisplayEntity.TextDisplayEntity> player2nametag;

    @Override
    public void onInitialize() {
        player2nametag = new ConcurrentHashMap<>();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> new UpdateNametagJob().schedule());
    }

    @Override
    public void onReload() {
        player2nametag.forEach((key, value) -> value.stopRiding());
    }

    private static DisplayEntity.TextDisplayEntity makeNametag(ServerPlayerEntity player) {
        LogUtil.debug("make nametag for player: {}", player.getGameProfile().getName());

        DisplayEntity.TextDisplayEntity nametag = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, player.getWorld()) {
            private void discardNametag() {
                ServerHelper.sendPacketToAll(new EntitiesDestroyS2CPacket(this.getId()));
                this.remove(RemovalReason.DISCARDED);
            }

            @Override
            public void tick() {
                super.tick();

                /* discard nametag if the vehicle is empty */
                if (this.getVehicle() == null) {
                    LogUtil.debug("discard nametag entity {}: its vehicle is null", this);
                    this.discardNametag();
                    return;
                }

                /* discard nametag if the vehicle is sneaking */
                if (this.getVehicle().isSneaking()) {
                    this.discardNametag();
                }

            }
        };
        // let the nametag riding in internal server-side.
        nametag.setInvulnerable(true);
        nametag.startRiding(player);

        sendExistingNametagsToTheNewJoinedPlayer(player);
        broadcastTheNewNametagToAllPlayers(player, nametag);
        return nametag;
    }

    private static BlockPos computeNametagSpawnBlockPos(ServerPlayerEntity bindingPlayer) {
        // spawn the nametag over the player's head, so that the player won't see the nametag ride animation.
        return bindingPlayer.getBlockPos().add(0, 3, 0);
    }

    private static void sendExistingNametagsToTheNewJoinedPlayer(ServerPlayerEntity player) {
        player2nametag.forEach((key, value) -> {
            BlockPos blockPos = computeNametagSpawnBlockPos(key);
            EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(value, 0, blockPos);
            player.networkHandler.sendPacket(entitySpawnS2CPacket);

            EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(key);
            player.networkHandler.sendPacket(entityPassengersSetS2CPacket);

            player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(value.getId(), value.getDataTracker().getChangedEntries()));
        });
    }

    private static void broadcastTheNewNametagToAllPlayers(ServerPlayerEntity player, DisplayEntity.TextDisplayEntity textDisplayEntity) {
        // spawn entity packet
        BlockPos blockPos = computeNametagSpawnBlockPos(player);
        EntitySpawnS2CPacket entitySpawnS2CPacket = new EntitySpawnS2CPacket(textDisplayEntity, 0, blockPos);
        ServerHelper.sendPacketToAll(entitySpawnS2CPacket);

        // ride entity packet
        EntityPassengersSetS2CPacket entityPassengersSetS2CPacket = new EntityPassengersSetS2CPacket(player);
        ServerHelper.sendPacketToAll(entityPassengersSetS2CPacket);
    }

    private static byte setTextDisplayFlags(int base, int flag, boolean value) {
        return (byte) (value ? base | flag : base & ~flag);
    }

    private static void setDisplayFlag(DataTracker dataTracker, byte flag, boolean value) {
        Byte original = dataTracker.get(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS);
        dataTracker.set(DisplayEntity.TextDisplayEntity.TEXT_DISPLAY_FLAGS, setTextDisplayFlags(original, flag, value));
    }

    private static void updateNametag(DisplayEntity.TextDisplayEntity nametag, ServerPlayerEntity player) {
        /* update props of nametag entity */
        var config = NametagInitializer.config.getModel();

        nametag.setBillboardMode(DisplayEntity.BillboardMode.CENTER);

        Text text = LocaleHelper.getTextByValue(player, config.style.text);
        nametag.setText(text);

        nametag.getDataTracker().set(DisplayEntity.TRANSLATION, new Vector3f(config.style.offset.x, config.style.offset.y, config.style.offset.z));

        nametag.setDisplayWidth(config.style.size.width);
        nametag.setDisplayHeight(config.style.size.height);

        nametag.setBackground(config.style.color.background);
        nametag.setTextOpacity(config.style.color.text_opacity);

        nametag.getDataTracker().set(DisplayEntity.SCALE, new Vector3f(config.style.scale.x, config.style.scale.y, config.style.scale.z));

        setDisplayFlag(nametag.getDataTracker(), DisplayEntity.TextDisplayEntity.SHADOW_FLAG, config.style.shadow.shadow);
        nametag.setShadowRadius(config.style.shadow.shadow_radius);
        nametag.setShadowStrength(config.style.shadow.shadow_strength);

        setDisplayFlag(nametag.getDataTracker(), DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG, config.render.see_through_blocks);
        nametag.setViewRange(config.render.view_range);

        if (config.style.brightness.override_brightness) {
            nametag.setBrightness(new Brightness(config.style.brightness.block, config.style.brightness.sky));
        }

        /* send update props packet */
        if (nametag.getDataTracker().isDirty()) {
            var dirty = nametag.getDataTracker().getDirtyEntries();
            if (dirty != null) {
                int entityId = nametag.getId();
                ServerHelper.sendPacketToAll(new EntityTrackerUpdateS2CPacket(entityId, dirty));
            }
        }
    }

    public static void updateNametags() {
        // since the virtual entity is not added into the server, so we should tick() it ourselves.
        player2nametag.values().forEach(DisplayEntity::tick);

        // invalidate keys
        player2nametag.entrySet().removeIf(entry -> entry.getKey().isRemoved() || entry.getValue().isRemoved());

        // update
        ServerHelper.getPlayers().forEach(player -> {
            if (player.isDead()) return;
            if (player.isSneaking()) return;

            // make if not exists
            if (!player2nametag.containsKey(player)) {
                player2nametag.put(player, makeNametag(player));
            }

            DisplayEntity.TextDisplayEntity nametag = player2nametag.get(player);
            updateNametag(nametag, player);
        });
    }

}
