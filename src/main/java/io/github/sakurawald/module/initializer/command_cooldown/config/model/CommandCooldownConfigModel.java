package io.github.sakurawald.module.initializer.command_cooldown.config.model;

import java.util.HashMap;
import java.util.Map;

public class CommandCooldownConfigModel {

    public Map<String, Long> regex2ms = new HashMap<>() {
        {
            this.put("world tp (overworld|the_nether|the_end)", 120 * 1000L);
            this.put("chunks\\s*", 60 * 1000L);
            this.put("rtp\\s*", 60 * 1000L);
            this.put("download\\s*", 120 * 1000L);
            this.put("heal\\s*", 300 * 1000L);
            this.put("repair\\s*", 300 * 1000L);
        }
    };
}
