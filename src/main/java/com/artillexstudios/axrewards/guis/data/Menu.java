package com.artillexstudios.axrewards.guis.data;

import com.artillexstudios.axapi.config.Config;

import java.util.List;

public record Menu(String name, Config settings, List<Reward> rewards) {
}