package com.artillexstudios.axrewards.hooks;

import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @NotNull
    @Override
    public String getAuthor() {
        return "ArtillexStudios";
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "axrewards";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String[] args = params.split("_");

        if (offlinePlayer == null) return "---";
        final Player player = offlinePlayer.getPlayer();
        if (player == null) return "---";

        if (args.length == 1 && args[0].equalsIgnoreCase("collectable")) {
            int am = 0;
            for (String s : MenuManager.getMenus().keySet()) {
                Menu menu = MenuManager.getMenus().get(s);
                if (menu == null) continue;
                am += MenuManager.getClaimable(player, menu);
            }
            return "" + am;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("collectable")) {
            Menu menu = MenuManager.getMenus().get(args[1]);
            if (menu == null) return "Menu not found";
            return "" + MenuManager.getClaimable(player, menu);
        }

        return null;
    }
}
