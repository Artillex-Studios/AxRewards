package com.artillexstudios.axrewards.guis.impl;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.GuiFrame;
import com.artillexstudios.axrewards.utils.SoundUtils;
import com.artillexstudios.axrewards.utils.TimeUtils;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class RewardGui extends GuiFrame {
    private static final Set<RewardGui> openMenus = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final BaseGui gui;
    private final Player player;
    private final String menu;

    public RewardGui(Player player, Config settings, String menu) {
        super(settings, player);
        this.player = player;
        this.menu = menu;
        this.gui = Gui
                .gui(GuiType.valueOf(file.getString("type", "CHEST")))
                .disableAllInteractions()
                .title(Component.empty())
                .rows(file.getInt("rows", 6))
                .create();
        gui.updateTitle(StringUtils.formatToString(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null ? file.getString("title") : PlaceholderAPI.setPlaceholders(player, file.getString("title"))));
        setGui(gui);
    }

    public void open() {
        for (String str : file.getBackingDocument().getRoutesAsStrings(false)) {
            final Section section = file.getSection(str);
            if (section == null) continue;

            final String permission = section.getString("permission", null);
            if (permission != null && !player.hasPermission(permission)) {
                final Map<String, String> replacements = Map.of("%permission%", permission);
                super.createItem(str + ".no-permission", str, event -> {
                    SoundUtils.playSound(player, LANG.getSection("no-permission"));
                    MESSAGEUTILS.sendLang(player, "no-permission.message", replacements);
                }, replacements);
                continue;
            }

            long lastClaim = AxRewards.getDatabase().getLastClaimed(player.getUniqueId(), menu, str);
            long claimCooldown = section.getLong("cooldown") * 1_000L;

            if ((lastClaim != 0 && section.getLong("cooldown") == -1) || (lastClaim > System.currentTimeMillis() - claimCooldown)) {
                long time = lastClaim - System.currentTimeMillis() + claimCooldown;
                final Map<String, String> replacements = Map.of("%time%", TimeUtils.fancyTime(time));
                super.createItem(str + ".unclaimable", str, event -> {
                    SoundUtils.playSound(player, LANG.getSection("on-cooldown"));
                    if (time < 0)
                        MESSAGEUTILS.sendLang(player, "on-cooldown.one-time", replacements);
                    else
                        MESSAGEUTILS.sendLang(player, "on-cooldown.message", replacements);
                }, replacements);
                continue;
            }

            super.createItem(str + ".claimable", str, event -> {
                long lastClaim2 = AxRewards.getDatabase().getLastClaimed(player.getUniqueId(), menu, str);
                if ((lastClaim2 != 0 && section.getLong("cooldown") == -1) || (lastClaim2 > System.currentTimeMillis() - claimCooldown)) {
                    long time = lastClaim2 - System.currentTimeMillis() + claimCooldown;
                    SoundUtils.playSound(player, LANG.getSection("on-cooldown"));
                    final Map<String, String> replacements = Map.of("%time%", TimeUtils.fancyTime(time));
                    if (time < 0)
                        MESSAGEUTILS.sendLang(player, "on-cooldown.one-time", replacements);
                    else
                        MESSAGEUTILS.sendLang(player, "on-cooldown.message", replacements);
                    return;
                }

                SoundUtils.playSound(player, LANG.getSection("claimed"));
                MESSAGEUTILS.sendLang(player, "claimed.message");
                AxRewards.getDatabase().claimReward(player.getUniqueId(), menu, str);

                Scheduler.get().run(scheduledTask -> {
                    for (String command : section.getStringList("claim-commands")) {
                        command = command.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI") ? PlaceholderAPI.setPlaceholders(player, command) : command
                        );
                    }
                });
                open();
            });
        }

        if (file.getSection("close") != null) {
            super.createItem("close", "close", event -> {
                Scheduler.get().runAt(player.getLocation(), scheduledTask -> {
                    player.closeInventory();
                });
            });
        }

        if (openMenus.contains(this)) {
            gui.update();
            updateTitle();
            return;
        }

        final RewardGui mainGui = this;
        gui.setCloseGuiAction(e -> openMenus.remove(mainGui));

        Scheduler.get().run(t -> {
            gui.open(player);
            openMenus.add(this);
        });
    }

    public void updateTitle() {
        if (!CONFIG.getBoolean("update-gui-title", false)) return;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return;
        final Component title = StringUtils.format(PlaceholderAPI.setPlaceholders(player, file.getString("title")));

        final Inventory topInv = player.getPlayer().getOpenInventory().getTopInventory();
        if (topInv.equals(gui.getInventory())) {
            NMSHandlers.getNmsHandler().setTitle(player.getPlayer().getOpenInventory().getTopInventory(), title);
        }
    }

    public BaseGui getGui() {
        return gui;
    }

    public Player getPlayer() {
        return player;
    }

    public static Set<RewardGui> getOpenMenus() {
        return openMenus;
    }
}
