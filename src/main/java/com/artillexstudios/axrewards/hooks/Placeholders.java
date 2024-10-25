package com.artillexstudios.axrewards.hooks;

import org.bukkit.OfflinePlayer;

import java.util.List;

public interface Placeholders {

    default String setPlaceholders(OfflinePlayer player, String txt) {
        return txt;
    }

    default List<String> setPlaceholders(OfflinePlayer player, List<String> txt) {
        return txt;
    }
}
