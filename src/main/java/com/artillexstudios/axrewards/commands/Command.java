package com.artillexstudios.axrewards.commands;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;

import java.util.List;

public abstract class Command {
    protected CommandAPICommand command;
    protected List<String> aliases;

    public void register() {
        command.register();
    }

    public void unregister() {
        System.out.println(aliases);
        CommandAPIBukkit.unregister(aliases.get(0), true, true);
    }
}