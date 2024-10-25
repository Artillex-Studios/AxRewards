package com.artillexstudios.axrewards;

import com.artillexstudios.axapi.config.Config;

import java.io.File;
import java.util.List;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;

public class Converter2 {

    public Converter2() {
        final List<String> aliases = CONFIG.getStringList("command-aliases");
        aliases.remove("reward");
        aliases.remove("rewards");
        CONFIG.set("command-aliases", aliases);
        CONFIG.save();

        LANG.set("help", LANG.getBackingDocument().getDefaults().getStringList("help"));
        LANG.save();

        final File guis = new File(AxRewards.getInstance().getDataFolder(), "guis.yml");
        final File updated = new File(AxRewards.getInstance().getDataFolder(), "menus/default.yml");
        guis.renameTo(updated);

        final Config def = new Config(updated, AxRewards.getInstance().getResource("menus/default.yml"));
        def.set("open-commands", List.of("reward", "rewards"));
        def.getBackingDocument().remove("version");

        def.save();
    }
}
