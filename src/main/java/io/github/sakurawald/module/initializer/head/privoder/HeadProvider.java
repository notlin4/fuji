package io.github.sakurawald.module.initializer.head.privoder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.structure.Downloader;
import io.github.sakurawald.module.initializer.head.HeadInitializer;
import io.github.sakurawald.module.initializer.head.structure.Category;
import io.github.sakurawald.module.initializer.head.structure.Head;
import lombok.Cleanup;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class HeadProvider {

    private static final Path STORAGE_PATH = ReflectionUtil.getModuleConfigPath(HeadInitializer.class).resolve("head-data").toAbsolutePath();

    private static final String API = "https://minecraft-heads.com/scripts/api.php?cat=%s&tags=true";

    @Getter(lazy = true)
    private static final Multimap<Category, Head> heads = fetchData();

    public static Multimap<Category, Head> fetchData() {
        HashMultimap<Category, Head> result = HashMultimap.create();

        for (Category category : Category.values()) {
            String URL = null;
            try {
                File destination = STORAGE_PATH.resolve(category.getFileName()).toFile();

                // skip
                if (destination.exists()) {
                    loadCategory(result, category);
                    continue;
                }

                URL = API.formatted(category.name);
                URI uri = URI.create(URL);
                Downloader downloader = new Downloader(uri.toURL(), destination) {
                    @Override
                    public void onComplete() {
                        loadCategory(result, category);
                    }
                };
                downloader.start();
            } catch (IOException e) {
                LogUtil.warn("failed to download heads from URL {}", URL);
            }
        }
        return result;
    }

    private static void loadCategory(HashMultimap<Category, Head> result, Category category) {
        try {
            LogUtil.info("load head category: {}", category.name);
            @Cleanup InputStreamReader reader = new InputStreamReader(Files.newInputStream(STORAGE_PATH.resolve(category.name + ".json")));
            JsonArray headsJson = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement headJson : headsJson) {
                try {
                    Head head = BaseConfigurationHandler.getGson().fromJson(headJson, Head.class);
                    result.put(category, head);
                } catch (Exception e) {
                    LogUtil.warn("invalid head: " + headJson);
                }
            }
        } catch (IOException e) {
            LogUtil.error("failed to load head category.", e);
        }
    }
}
