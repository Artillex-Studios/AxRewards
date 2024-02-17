package com.artillexstudios.axrewards.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Database {

    String getType();

    void setup();

    void claimReward(@NotNull UUID uuid, String name);

    void resetReward(@NotNull UUID uuid, @Nullable String name);

    long getLastClaimed(@NotNull UUID uuid, String name);

    void disable();
}
