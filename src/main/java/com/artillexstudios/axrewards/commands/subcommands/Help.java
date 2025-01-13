package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.command.CommandSender;

import static com.artillexstudios.axrewards.AxRewards.LANG;

public enum Help {
    INSTANCE;

    public void execute(CommandSender sender) {
        for (String m : LANG.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
    }
}
