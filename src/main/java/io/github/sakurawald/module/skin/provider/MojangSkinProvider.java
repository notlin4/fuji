package io.github.sakurawald.module.skin.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import io.github.sakurawald.util.HttpUtil;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class MojangSkinProvider {

    private static final String API_SERVER = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static Property getSkin(String name) {
        try {
            UUID uuid = getOnlineUUID(name);
            JsonObject texture = JsonParser.parseString(HttpUtil.get(URI.create(SESSION_SERVER + uuid + "?unsigned=false"))).getAsJsonObject().getAsJsonArray("properties").get(0).getAsJsonObject();

            return new Property("textures", texture.get("value").getAsString(), texture.get("signature").getAsString());
        } catch (Exception e) {
            return null;
        }
    }

    private static UUID getOnlineUUID(String name) throws IOException {
        return UUID.fromString(JsonParser.parseString(HttpUtil.get(URI.create(API_SERVER + name))).getAsJsonObject().get("id").getAsString()
                .replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
    }
}
