package com.artillexstudios.axrewards.guis.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record Reward(Menu menu, String name, int slot, long cooldown, List<String> claimCommands,
                     List<Map<?, ?>> claimItems, ItemStack claimableItem, ItemStack unclaimableItem,
                     @Nullable String claimPermission, ItemStack noPermissionItem) {

    @Override
    public String toString() {
        return "Reward{" +
                "name='" + name + '\'' +
                ", slot=" + slot +
                ", cooldown=" + cooldown +
                ", claimCommands=" + claimCommands +
                ", claimItems=" + claimItems +
                ", claimableItem=" + claimableItem +
                ", unclaimableItem=" + unclaimableItem +
                ", claimPermission='" + claimPermission + '\'' +
                ", noPermissionItem=" + noPermissionItem +
                '}';
    }
}
