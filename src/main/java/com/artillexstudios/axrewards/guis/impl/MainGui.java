package com.artillexstudios.axrewards.guis.impl;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.nms.NMSHandlers;
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

import static com.artillexstudios.axrewards.AxRewards.GUIS;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class MainGui extends GuiFrame {
    private static final Set<MainGui> openMenus = Collections.newSetFromMap(new WeakHashMap<>());
    private final BaseGui gui = Gui
            .gui(GuiType.valueOf(GUIS.getString("type", "CHEST")))
            .disableAllInteractions()
            .title(Component.empty())
            .rows(GUIS.getInt("rows", 6))
            .create();

    public MainGui(Player player) {
        super(GUIS, player);
        gui.updateTitle(StringUtils.formatToString(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null ? GUIS.getString("title") : PlaceholderAPI.setPlaceholders(player, GUIS.getString("title"))));
        setGui(gui);
    }

    public void open() {
        for (String str : GUIS.getBackingDocument().getRoutesAsStrings(false)) {
            final Section section = GUIS.getSection(str);
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

            final long lastClaim = AxRewards.getDatabase().getLastClaimed(player.getUniqueId(), str);
            final long claimCooldown = section.getLong("cooldown") * 1_000L;
            if (lastClaim > System.currentTimeMillis() - claimCooldown) {
                final Map<String, String> replacements = Map.of("%time%", TimeUtils.fancyTime(lastClaim - System.currentTimeMillis() + claimCooldown));
                super.createItem(str + ".unclaimable", str, event -> {
                    SoundUtils.playSound(player, LANG.getSection("on-cooldown"));
                    MESSAGEUTILS.sendLang(player, "on-cooldown.message", replacements);
                }, replacements);
                continue;
            }

            super.createItem(str + ".claimable", str, event -> {
                SoundUtils.playSound(player, LANG.getSection("claimed"));
                MESSAGEUTILS.sendLang(player, "claimed.message");
                AxRewards.getDatabase().claimReward(player.getUniqueId(), str);

                Scheduler.get().run(scheduledTask -> {
                    for (String command : section.getStringList("claim-commands")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                    }
                });
                open();
            });
        }

        if (GUIS.getSection("close") != null) {
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
        openMenus.add(this);

        final MainGui mainGui = this;
        gui.setCloseGuiAction(inventoryCloseEvent -> openMenus.remove(mainGui));

        Scheduler.get().run(scheduledTask -> gui.open(player));
    }

    public void updateTitle() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return;
        Component title = StringUtils.format(PlaceholderAPI.setPlaceholders(player, GUIS.getString("title")));

        final Inventory topInv = player.getPlayer().getOpenInventory().getTopInventory();
        if (topInv.equals(gui.getInventory())) {
            NMSHandlers.getNmsHandler().setTitle(player.getPlayer().getOpenInventory().getTopInventory(), title);
        }
    }

    public static Set<MainGui> getOpenMenus() {
        return openMenus;
    }
}
