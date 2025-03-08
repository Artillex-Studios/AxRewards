package com.artillexstudios.axrewards.guis.impl;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.GuiFrame;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import com.artillexstudios.axrewards.guis.data.Reward;
import com.artillexstudios.axrewards.utils.SoundUtils;
import com.artillexstudios.axrewards.utils.TimeUtils;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;
import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public class RewardGui extends GuiFrame {
    private static final Set<RewardGui> openMenus = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final BaseGui gui;
    private final Player player;
    private final Menu menu;

    public RewardGui(Player player, Menu menu) {
        super(menu.settings(), player);
        this.player = player;
        this.menu = menu;
        this.gui = Gui
                .gui(GuiType.valueOf(file.getString("type", "CHEST")))
                .disableAllInteractions()
                .title(Component.empty())
                .rows(file.getInt("rows", 6))
                .create();
        gui.updateTitle(StringUtils.formatToString(AxRewards.getPlaceholderParser().setPlaceholders(player, file.getString("title"))));
        setGui(gui);
    }

    public void open() {
        CompletableFuture<Void> cf = new CompletableFuture<>();

        // load menu async
        AxRewards.getThreadedQueue().submit(() -> {
            if (file.getSection("close") != null) {
                super.createItem("close", "close", event -> {
                    Scheduler.get().runAt(player.getLocation(), scheduledTask -> {
                        player.closeInventory();
                        if (file.getSection("close").getStringList("commands") != null) {
                            for (String command : file.getSection("close").getStringList("commands")) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), AxRewards.getPlaceholderParser().setPlaceholders(player, command));
                            }
                        }
                    });
                });
            }

            for (Reward reward : menu.rewards()) {
                String permission = reward.claimPermission();
                if (permission != null && !player.hasPermission(permission)) {
                    Map<String, String> replacements = Map.of("%permission%", permission);
                    super.createItem(reward.name() + ".no-permission", reward.name(), event -> {
                        SoundUtils.playSound(player, LANG.getSection("no-permission"));
                        MESSAGEUTILS.sendLang(player, "no-permission.message", replacements);
                    }, replacements);
                    continue;
                }

                long lastClaim = AxRewards.getDatabase().getLastClaim(player, reward);
                if (!MenuManager.canClaimReward(reward, lastClaim)) {
                    long next = lastClaim - System.currentTimeMillis() + reward.cooldown();
                    Map<String, String> replacements = Map.of("%time%", TimeUtils.fancyTime(next));
                    super.createItem(reward.name() + ".unclaimable", reward.name(), event -> {
                        SoundUtils.playSound(player, LANG.getSection("on-cooldown"));
                        if (next < 0)
                            MESSAGEUTILS.sendLang(player, "on-cooldown.one-time", replacements);
                        else
                            MESSAGEUTILS.sendLang(player, "on-cooldown.message", replacements);
                    }, replacements);
                    continue;
                }

                super.createItem(reward.name() + ".claimable", reward.name(), event -> {
                    long lastClaim2 = AxRewards.getDatabase().getLastClaim(player, reward);
                    if (!MenuManager.canClaimReward(reward, lastClaim2)) {
                        open();
                        return;
                    }

                    if (permission != null && !player.hasPermission(permission)) {
                        SoundUtils.playSound(player, LANG.getSection("no-permission"));
                        MESSAGEUTILS.sendLang(player, "no-permission.message", Map.of("%permission%", permission));
                        return;
                    }

                    SoundUtils.playSound(player, LANG.getSection("claimed"));
                    MESSAGEUTILS.sendLang(player, "claimed.message");
                    AxRewards.getDatabase().claimReward(player, reward);

                    Scheduler.get().run(scheduledTask -> {
                        List<String> finalCommands = new ArrayList<>();
                        List<String> randomBlockCommands = new ArrayList<>();
                        boolean inRandomBlock = false;
                        int randomCount = 1;
                        boolean canRepeat = false;
                        for (String command : reward.claimCommands()) {
                            String trimmed = command.trim();
                            if (trimmed.toUpperCase().startsWith("RANDOM START")) {
                                inRandomBlock = true;
                                String[] parts = trimmed.split("\\s+");
                                if (parts.length >= 3) {
                                    try {
                                        randomCount = Integer.parseInt(parts[2]);
                                    } catch (NumberFormatException e) {
                                        randomCount = 1;
                                    }
                                } else {
                                    randomCount = 1;
                                }
                                if (parts.length >= 4) {
                                    canRepeat = Boolean.parseBoolean(parts[3]);
                                } else {
                                    canRepeat = false;
                                }
                                continue;
                            }
                            if (trimmed.toUpperCase().startsWith("RANDOM END")) {
                                inRandomBlock = false;
                                if (!randomBlockCommands.isEmpty()) {
                                    List<String> chosen = new ArrayList<>();
                                    if (!canRepeat) {
                                        int selectionCount = Math.min(randomCount, randomBlockCommands.size());
                                        Collections.shuffle(randomBlockCommands);
                                        chosen.addAll(randomBlockCommands.subList(0, selectionCount));
                                    } else {
                                        Random random = new Random();
                                        for (int i = 0; i < randomCount; i++) {
                                            chosen.add(randomBlockCommands.get(random.nextInt(randomBlockCommands.size())));
                                        }
                                    }
                                    finalCommands.addAll(chosen);
                                    randomBlockCommands.clear();
                                }
                                continue;
                            }
                            if (inRandomBlock) {
                                randomBlockCommands.add(trimmed);
                            } else {
                                finalCommands.add(trimmed);
                            }
                        }
                        if (inRandomBlock && !randomBlockCommands.isEmpty()) {
                            List<String> chosen = new ArrayList<>();
                            if (!canRepeat) {
                                int selectionCount = Math.min(randomCount, randomBlockCommands.size());
                                Collections.shuffle(randomBlockCommands);
                                chosen.addAll(randomBlockCommands.subList(0, selectionCount));
                            } else {
                                Random random = new Random();
                                for (int i = 0; i < randomCount; i++) {
                                    chosen.add(randomBlockCommands.get(random.nextInt(randomBlockCommands.size())));
                                }
                            }
                            finalCommands.addAll(chosen);
                        }
                        for (String cmd : finalCommands) {
                            cmd = cmd.replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), AxRewards.getPlaceholderParser().setPlaceholders(player, cmd));
                        }

                        for (Map<?, ?> map : reward.claimItems()) {
                            ItemStack it = new ItemBuilder((Map<Object, Object>) map).get();
                            ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), List.of(it), player.getLocation());
                        }
                    });
                    open();
                });
            }
            cf.complete(null);
        });

        // open when the gui is loaded
        cf.thenRun(() -> {
            if (openMenus.contains(this)) {
                gui.update();
                updateTitle();
                return;
            }

            gui.setCloseGuiAction(e -> openMenus.remove(this));

            Scheduler.get().run(t -> {
                gui.open(player);
                openMenus.add(this);
            });
        });
    }

    public void updateTitle() {
        if (!CONFIG.getBoolean("update-gui-title", false)) return;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return;
        final Component title = StringUtils.format(AxRewards.getPlaceholderParser().setPlaceholders(player, file.getString("title")));

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
