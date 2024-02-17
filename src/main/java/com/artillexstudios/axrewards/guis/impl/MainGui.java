package com.artillexstudios.axrewards.guis.impl;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.GuiFrame;
import com.artillexstudios.axrewards.utils.SoundUtils;
import com.artillexstudios.axrewards.utils.TimeUtils;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

import static com.artillexstudios.axrewards.AxRewards.GUIS;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class MainGui extends GuiFrame {
    private static final WeakHashMap<MainGui, Void> map = new WeakHashMap<>();
    private boolean opened = false;
    private final BaseGui gui = Gui.gui(GuiType.valueOf(GUIS.getString("type", "CHEST"))).disableAllInteractions().title(StringUtils.format(GUIS.getString("title"))).rows(GUIS.getInt("rows", 6)).create();

    public MainGui(Player player) {
        super(GUIS, player);
        setGui(gui);
        map.put(this, null);
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

        if (opened) {
            gui.update();
            return;
        }
        opened = true;

        final MainGui mainGui = this;
        gui.setCloseGuiAction(inventoryCloseEvent -> map.remove(mainGui));

        Scheduler.get().run(scheduledTask -> gui.open(player));
    }

    public static WeakHashMap<MainGui, Void> getMap() {
        return map;
    }
}
