package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.commands.CommandManager;
import com.artillexstudios.axrewards.guis.GuiUpdater;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum Reload {
    INSTANCE;

    public void execute(CommandSender sender, boolean commands) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] &#FFEEAAReloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &fconfig.yml&#FFEEAA!"));

        if (!LANG.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Map.of("%file%", "lang.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &flang.yml&#FFEEAA!"));

        MenuManager.reload();
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &fmenus&#FFEEAA!"));

        if (commands)
            CommandManager.reload();
        GuiUpdater.start();
        AxRewards.getDatabase().reload();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╚ &#FFEEAASuccessful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }
}