package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.commands.subcommands.ForceOpen;
import com.artillexstudios.axrewards.commands.subcommands.Open;
import com.artillexstudios.axrewards.commands.subcommands.Reload;
import com.artillexstudios.axrewards.commands.subcommands.Reset;
import com.artillexstudios.axrewards.guis.impl.GuiManager;
import com.artillexstudios.axrewards.utils.CommandMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;

public class Commands implements OrphanCommand {

    @DefaultFor({"~", "~ help"})
    @CommandPermission("axrewards.help")
    public void help(@NotNull CommandSender sender) {
        for (String m : LANG.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
    }

    @Subcommand("open")
    @AutoComplete("@menus")
    public void open(@NotNull Player sender, @Optional String menu) {
        Open.INSTANCE.execute(sender, menu);
    }

    @Subcommand("reload")
    @CommandPermission("axrewards.reload")
    public void reload(@NotNull CommandSender sender) {
        Reload.INSTANCE.execute(sender);
    }

    @Subcommand("reset")
    @CommandPermission("axrewards.reset")
    @AutoComplete("* @menus @rewards")
    public void reset(@NotNull CommandSender sender, @NotNull OfflinePlayer player, @Optional String menu, @Optional String reward) {
        Reset.INSTANCE.execute(sender, player, menu, reward);
    }

    @Subcommand("forceopen")
    @CommandPermission("axrewards.forceopen")
    @AutoComplete("* @menus *")
    public void forceopen(@NotNull CommandSender sender, Player player, @Optional String menu, @Optional Boolean bypass) {
        ForceOpen.INSTANCE.execute(sender, player, menu, bypass);
    }

    private static BukkitCommandHandler handler = null;

    public static void registerCommand() {
        if (handler == null) {
            handler = BukkitCommandHandler.create(AxRewards.getInstance());

            handler.getAutoCompleter().registerSuggestion("rewards", (args, sender, command) -> {
                String menu = args.get(args.size() - 2);
                Config cfg;
                if ((cfg = GuiManager.getMenus().get(menu)) == null) return List.of();
                return cfg.getBackingDocument().getRoutesAsStrings(false).stream().filter(string -> cfg.getSection(string) != null).toList();
            });
            handler.getAutoCompleter().registerSuggestion("menus", (args, sender, command) -> GuiManager.getMenus().keySet());

            handler.registerValueResolver(0, OfflinePlayer.class, context -> {
                String value = context.pop();
                if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) return ((BukkitCommandActor) context.actor()).requirePlayer();
                OfflinePlayer player = NMSHandlers.getNmsHandler().getCachedOfflinePlayer(value);
                if (player == null && !(player = Bukkit.getOfflinePlayer(value)).hasPlayedBefore()) throw new InvalidPlayerException(context.parameter(), value);
                return player;
            });

            handler.getAutoCompleter().registerParameterSuggestions(OfflinePlayer.class, (args, sender, command) -> {
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toSet());
            });

            handler.getTranslator().add(new CommandMessages());
            handler.setLocale(new Locale("en", "US"));
        }

        handler.unregisterAllCommands();
        for (Map.Entry<String, Config> entry : GuiManager.getMenus().entrySet()) {
            try {
                handler.register(Orphans.path(entry.getValue().getStringList("open-commands").toArray(String[]::new)).handler(new OpenCommand(entry.getKey())));
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRewards] Failed to register the command of menu " + entry.getKey() + ", one of the open-commands is already used by another menu/plugin!"));
            }
        }
        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
        handler.registerBrigadier();
    }
}
