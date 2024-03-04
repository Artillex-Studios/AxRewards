package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.impl.MainGui;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.GUIS;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class Commands implements OrphanCommand {

    @DefaultFor({"~", "~ open"})
    @CommandPermission(value = "axrewards.open", defaultAccess = PermissionDefault.TRUE)
    public void open(@NotNull Player sender) {
        AxRewards.getThreadedQueue().submit(() -> new MainGui(sender).open());
    }

    @Subcommand("help")
    @CommandPermission(value = "axrewards.help", defaultAccess = PermissionDefault.TRUE)
    public void help(@NotNull CommandSender sender) {
        for (String m : LANG.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
    }

    @Subcommand("reload")
    @CommandPermission("axrewards.reload")
    public void reload(@NotNull CommandSender sender) {
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

        if (!GUIS.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Map.of("%file%", "guis.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╠ &#FFEEAAReloaded &fguis.yml&#FFEEAA!"));

        Commands.registerCommand();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00╚ &#FFEEAASuccessful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }

    @Subcommand("reset")
    @CommandPermission("axrewards.reset")
    @AutoComplete("@players @rewards")
    public void reset(@NotNull CommandSender sender, @NotNull OfflinePlayer player, @Optional @Nullable String name) {
        final Map<String, String> replacements = Map.of("%name%", name == null ? "---" : name, "%player%", player.getName() == null ? "---" : player.getName());
        if (name == null) MESSAGEUTILS.sendLang(sender, "reset.all", replacements);
        else MESSAGEUTILS.sendLang(sender, "reset.single", replacements);
        AxRewards.getThreadedQueue().submit(() -> AxRewards.getDatabase().resetReward(player.getUniqueId(), name));
    }

    @Subcommand("forceopen")
    @CommandPermission("axrewards.forceopen")
    public void forceopen(@NotNull CommandSender sender, Player player) {
        new MainGui(player).open();
    }

    public static void registerCommand() {
        final BukkitCommandHandler handler = BukkitCommandHandler.create(AxRewards.getInstance());
        handler.unregisterAllCommands();
        handler.getAutoCompleter().registerSuggestion("players", (args, sender, command) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        handler.getAutoCompleter().registerSuggestion("rewards", (args, sender, command) -> GUIS.getBackingDocument().getRoutesAsStrings(false).stream().filter(string -> GUIS.getSection(string) != null).toList());
        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
        handler.registerBrigadier();
    }
}
