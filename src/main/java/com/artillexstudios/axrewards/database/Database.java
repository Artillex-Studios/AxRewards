package com.artillexstudios.axrewards.database;

import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.Reward;
import org.bukkit.OfflinePlayer;

public interface Database {

    String getType();

    void setup();

    void reload();

    int getPlayerId(OfflinePlayer player);

    int getMenuId(Menu menu);

    int getRewardId(Reward reward);

    long getLastClaim(OfflinePlayer player, Reward reward);

    void claimReward(OfflinePlayer player, Reward reward);

    void resetReward(OfflinePlayer player, Reward reward);

    void resetReward(OfflinePlayer player, Menu menu);

    void resetReward(OfflinePlayer player);

    void disable();
}
