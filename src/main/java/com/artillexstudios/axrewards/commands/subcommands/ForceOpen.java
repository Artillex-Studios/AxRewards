package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum ForceOpen {
    INSTANCE;

    public void execute(CommandSender sender, Player player, @Nullable Menu menu, @Nullable Boolean force) {
        if (force == null) force = false;
        if (menu == null && (menu = MenuManager.getFallBack()) == null) {
            MESSAGEUTILS.sendLang(sender, "errors.no-menus");
            return;
        }
        MenuManager.openMenu(player, menu, force);
    }
}
