package com.artillexstudios.axrewards.guis;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axrewards.utils.ItemBuilderUtil;
import com.artillexstudios.axrewards.utils.SoundUtils;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class GuiFrame {
    protected final Config file;
    protected BaseGui gui;
    protected Player player;

    public GuiFrame(Config file, Player player) {
        this.file = file;
        this.player = player;
    }

    public void setGui(BaseGui gui) {
        this.gui = gui;
        for (String str : file.getBackingDocument().getRoutesAsStrings(false)) createItem(str, str);
    }

    @NotNull
    public Config getFile() {
        return file;
    }

    protected ItemStack buildItem(@NotNull String key) {
        return ItemBuilderUtil.newBuilder(player, file.getSection(key)).get();
    }

    protected ItemStack buildItem(@NotNull String key, Map<String, String> replacements) {
        return ItemBuilderUtil.newBuilder(player, file.getSection(key), replacements).get();
    }

    protected void createItem(@NotNull String route, String prevRoute) {
        createItem(route, prevRoute, event -> SoundUtils.playSound(player, file.getSection(route + ".sound")), Map.of());
    }

    protected void createItem(@NotNull String route, String prevRoute, @Nullable GuiAction<InventoryClickEvent> action) {
        createItem(route, prevRoute, action, Map.of());
    }

    protected void createItem(@NotNull String route, String prevRoute, @Nullable GuiAction<InventoryClickEvent> action, Map<String, String> replacements) {
        if (file.getString(route + ".type") == null && file.getString(route + ".material") == null) return;
        final GuiItem guiItem = new GuiItem(buildItem(route, replacements), action);
        final List<Integer> slots = file.getBackingDocument().getIntList(prevRoute + ".slot");
        if (slots.isEmpty()) gui.setItem(file.getInt(prevRoute + ".slot"), guiItem);
        else gui.setItem(slots, guiItem);
    }
}
