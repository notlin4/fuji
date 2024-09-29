package io.github.sakurawald;

import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.manager.Managers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@Cite({
    "https://github.com/EssentialsX/Essentials/"
    , "https://github.com/NucleusPowered/Nucleus"
    , "https://github.com/Zrips/CMI-API"
    , "https://github.com/ForgeEssentials/ForgeEssentials"
})
public class Fuji implements ModInitializer {

    public static final String MOD_ID = "fuji";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toAbsolutePath();

    @Override
    public void onInitialize() {
        Managers.getStandardBackupManager().onInitialize();
        Managers.getBossBarManager().onInitialize();
        Managers.getModuleManager().onInitialize();
        Managers.getCallbackManager().onInitialize();
        Managers.getCommandManager().onInitialize();
        Managers.getScheduleManager().onInitialize();
    }
}
