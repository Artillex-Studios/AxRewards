package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axrewards.guis.impl.GuiManager;
import org.bukkit.entity.Player;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum Open {
    INSTANCE;

    public void execute(Player sender, String menu) {
        if ((menu = GuiManager.getFallBack(menu)) == null) {
            MESSAGEUTILS.sendLang(sender, "errors.no-menus");
            return;
        }
        GuiManager.openMenu(sender, menu, false);
    }
}
