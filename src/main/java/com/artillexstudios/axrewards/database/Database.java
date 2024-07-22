package com.artillexstudios.axrewards.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Database {

    String getType();

    void setup();

    void claimReward(@NotNull UUID uuid, String menu, String reward);

    void resetReward(@NotNull UUID uuid, @Nullable String menu, @Nullable String reward);

    long getLastClaimed(@NotNull UUID uuid, String menu, String reward);

    void disable();
}
