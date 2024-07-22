package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axrewards.guis.impl.GuiManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum ForceOpen {
    INSTANCE;

    public void execute(CommandSender sender, Player player, @Nullable String menu, @Nullable Boolean force) {
        if (force == null) force = false;
        if ((menu = GuiManager.getFallBack(menu)) == null) {
            MESSAGEUTILS.sendLang(sender, "errors.no-menus");
            return;
        }
        GuiManager.openMenu(player, menu, force);
    }
}
