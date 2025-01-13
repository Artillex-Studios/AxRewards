package com.artillexstudios.axrewards.guis.data;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.impl.RewardGui;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class MenuManager {
    private static final ConcurrentHashMap<String, Menu> menus = new ConcurrentHashMap<>();

    public static void reload() {
        for (RewardGui gui : RewardGui.getOpenMenus()) {
            gui.getGui().close(gui.getPlayer());
        }
        menus.clear();

        final File path = new File(AxRewards.getInstance().getDataFolder(), "menus");
        if (!path.exists()) return;

        for (File file : path.listFiles()) {
            if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) continue;
            final String name = file.getName().replace(".yml", "").replace(".yaml", "");

            final Config config = new Config(file);

            List<Reward> rewards = new ArrayList<>();
            Menu menu = new Menu(name, config, rewards);
            for (String route : config.getBackingDocument().getRoutesAsStrings(false)) {
                Section s = config.getSection(route);
                if (s == null) continue;

                Section claimableSection = s.getSection("claimable", null);
                if (claimableSection == null) continue;
                Section permissionItem = s.getSection("no-permission");

                long cd = s.getLong("cooldown");
                Reward reward = new Reward(
                        menu,
                        route,
                        s.getInt("slot"),
                        cd == -1 ? -1 : s.getLong("cooldown") * 1_000L,
                        s.getStringList("claim-commands"),
                        s.getMapList("claim-items"),
                        new ItemBuilder(claimableSection).get(),
                        new ItemBuilder(s.getSection("unclaimable")).get(),
                        s.getString("permission", null),
                        permissionItem == null ? null : new ItemBuilder(permissionItem).get()
                );
                rewards.add(reward);
            }

            menus.put(name, menu);
        }
    }

    public static Map<String, Menu> getMenus() {
        return menus;
    }

    public static void openMenu(Player player, Menu menu, boolean force) {
        if (!force && !player.hasPermission("axrewards.open.*") && !player.hasPermission("axrewards.open." + menu.name())) {
            MESSAGEUTILS.sendLang(player, "errors.no-open-permission", Map.of("%menu%", menu.name()));
            return;
        }

        new RewardGui(player, menu).open();
    }

    public static Menu getFallBack() {
        if (MenuManager.getMenus().isEmpty()) return null;

        Menu menu;
        if ((menu = menus.get("default")) == null) {
            Optional<Menu> optionalMenu = MenuManager.getMenus().values().stream().findFirst();
            if (optionalMenu.isEmpty()) return null;
            menu = optionalMenu.get();
        }
        return menu;
    }

    public static int getClaimable(Player player, Menu menu) {
        int am = 0;
        for (Reward reward : menu.rewards()) {
            long lastClaim = AxRewards.getDatabase().getLastClaim(player, reward);
            boolean canClaim = canClaimReward(reward, lastClaim);

            String permission = reward.claimPermission();
            boolean hasPermission = permission == null || player.hasPermission(permission);

            if (canClaim && hasPermission) am++;
        }
        return am;
    }

    public static boolean canClaimReward(Reward reward, long lastClaim) {
        boolean onCooldown = lastClaim + reward.cooldown() > System.currentTimeMillis();
        boolean claimFailed = lastClaim == -1;
        boolean oneTimeClaimed = lastClaim > 0 && reward.cooldown() == -1;
        return !onCooldown && !claimFailed && !oneTimeClaimed;
    }
}
