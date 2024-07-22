package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.commands.Commands;
import com.artillexstudios.axrewards.guis.impl.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum Reload {
    INSTANCE;

    public void execute(CommandSender sender) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] &#FFEEAAReloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &fconfig.yml&#FFEEAA!"));

        if (!LANG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "lang.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &flang.yml&#FFEEAA!"));

        GuiManager.reload();
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &fmenus&#FFEEAA!"));

        Commands.registerCommand();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╚ &#FFEEAASuccessful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }
}