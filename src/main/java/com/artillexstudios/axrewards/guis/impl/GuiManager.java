package com.artillexstudios.axrewards.guis.impl;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrewards.AxRewards;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class GuiManager {
    private static final HashMap<String, Config> menus = new HashMap<>();

    public static void reload() {
        for (RewardGui gui : RewardGui.getOpenMenus()) {
            gui.getGui().close(gui.getPlayer());
        }
        menus.clear();

        final File path = new File(AxRewards.getInstance().getDataFolder(), "menus");
        if (path.exists()) {
            for (File file : path.listFiles()) {
                if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) continue;
                final String name = file.getName().replace(".yml", "").replace(".yaml", "");

                final Config config = new Config(file);
                menus.put(name, config);
            }
        }
    }

    public static HashMap<String, Config> getMenus() {
        return menus;
    }

    public static void openMenu(Player player, String name, boolean force) {
        final Config config = menus.get(name);
        if (config == null) return;

        if (!force && !player.hasPermission("axrewards.open.*") && !player.hasPermission("axrewards.open." + name)) {
            MESSAGEUTILS.sendLang(player, "errors.no-open-permission", Map.of("%menu%", name));
            return;
        }

        new RewardGui(player, config, name).open();
    }

    public static String getFallBack(String menu) {

        if (menus.get(menu) != null) return menu;
        if (GuiManager.getMenus().isEmpty()) return null;

        if (menu == null) {
            if (menus.get("default") != null)
                menu = "default";
            else
                menu = GuiManager.getMenus().keySet().stream().findFirst().get();
        }
        return menu;
    }

    public static int getClaimable(Player player, String menu) {
        int am = 0;
        Config cfg;
        if ((cfg = menus.get(menu)) == null) return am;
        for (String str : cfg.getBackingDocument().getRoutesAsStrings(false)) {
            final Section section = cfg.getSection(str);
            if (section == null) continue;
            if (section.getSection("claimable") == null) continue;

            final String permission = section.getString("permission", null);
            if (permission != null && !player.hasPermission(permission)) continue;

            final long lastClaim = AxRewards.getDatabase().getLastClaimed(player.getUniqueId(), menu, str);
            if (lastClaim == 1 && section.getLong("cooldown") == -1) continue;
            final long claimCooldown = section.getLong("cooldown") * 1_000L;
            if (lastClaim > System.currentTimeMillis() - claimCooldown) continue;
            am++;
        }
        return am;
    }
}
