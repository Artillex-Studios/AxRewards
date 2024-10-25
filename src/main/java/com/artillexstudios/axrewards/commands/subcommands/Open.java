package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import org.bukkit.entity.Player;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum Open {
    INSTANCE;

    public void execute(Player sender, Menu menu) {
        if (menu == null && (menu = MenuManager.getFallBack()) == null) {
            MESSAGEUTILS.sendLang(sender, "errors.no-menus");
            return;
        }
        MenuManager.openMenu(sender, menu, false);
    }
}
