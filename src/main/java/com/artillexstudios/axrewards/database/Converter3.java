package com.artillexstudios.axrewards.database;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.database.impl.Base;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import com.artillexstudios.axrewards.guis.data.Reward;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Converter3 {

    public Converter3(Base base) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFFF00[AxRewards] Migrating database... Don't stop the server while it's running!"));
        int success = 0;

        try (Connection conn = base.getConnection()) {
            MapListHandler mapListHandler = new MapListHandler();

            List<Map<String, Object>> list = base.getRunner().query(conn, "SELECT * FROM axrewards_claimed", mapListHandler);

            for (Map<String, Object> map : list) {
                String rewardStr = (String) map.get("reward");
                String[] menuStr = rewardStr.split("\\|");

                Menu menu;
                Reward reward;
                String rewardName;
                if (menuStr.length == 2) {
                    menu = MenuManager.getMenus().get(menuStr[0]);
                    rewardName = menuStr[1];
                } else {
                    menu = MenuManager.getMenus().get("default");
                    rewardName = menuStr[0];
                }
                if (menu == null) {
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFFF00[AxRewards] Can't find menu " + rewardStr + "!"));
                    continue;
                }

                Optional<Reward> optReward = menu.rewards().stream().filter(rw -> rw.name().equals(rewardName)).findAny();
                if (optReward.isEmpty()) {
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFFF00[AxRewards] Can't find reward " + rewardName + " in menu " + menu.name() + "!"));
                    continue;
                }
                reward = optReward.get();

                UUID uuid = UUID.fromString((String) map.get("uuid"));
                long time = (long) map.get("time");

                base.claimReward(Bukkit.getOfflinePlayer(uuid), reward, time);
                success++;
            }

            base.getRunner().execute(conn, "DROP TABLE axrewards_claimed");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFFF00[AxRewards] Migration finished! Converted " + success + " cooldowns!"));
    }
}
