package com.artillexstudios.axrewards.hooks;

import com.artillexstudios.axrewards.guis.impl.GuiManager;
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
            for (String s : GuiManager.getMenus().keySet()) {
                am += GuiManager.getClaimable(player, s);
            }
            return "" + am;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("collectable")) {
            return "" + GuiManager.getClaimable(player, args[1]);
        }

        return null;
    }
}
