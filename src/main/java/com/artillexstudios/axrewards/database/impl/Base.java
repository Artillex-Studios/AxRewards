package com.artillexstudios.axrewards.database.impl;

import com.artillexstudios.axrewards.database.Converter3;
import com.artillexstudios.axrewards.database.Database;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import com.artillexstudios.axrewards.guis.data.Reward;
import com.artillexstudios.axrewards.utils.SQLUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public abstract class Base implements Database {
    private QueryRunner runner;

    private final String CREATE_TABLE1 = """
            CREATE TABLE IF NOT EXISTS axrewards_menus (
                id INT NOT NULL AUTO_INCREMENT,
                name VARCHAR(128) NOT NULL,
                PRIMARY KEY (id), UNIQUE (name)
            );
    """;

    private final String CREATE_TABLE2 = """
            CREATE TABLE IF NOT EXISTS axrewards_players (
                id INT NOT NULL AUTO_INCREMENT,
                uuid VARCHAR(36) NOT NULL,
                name VARCHAR(128),
                PRIMARY KEY (id), UNIQUE (uuid)
            );
    """;

    private final String CREATE_TABLE3 = """
            CREATE TABLE IF NOT EXISTS axrewards_rewards (
                id INT NOT NULL AUTO_INCREMENT,
                name VARCHAR(128) NOT NULL,
                menu_id INT NOT NULL,
                PRIMARY KEY (id), UNIQUE (name, menu_id)
            );
    """;

    private final String CREATE_TABLE4 = """
            CREATE TABLE IF NOT EXISTS axrewards_cooldowns (
                id INT NOT NULL AUTO_INCREMENT,
                player_id INT NOT NULL,
                reward_id INT NOT NULL,
                time BIGINT NOT NULL,
                PRIMARY KEY (id)
            );
    """;

    private final String INSERT_MENU = """
            INSERT INTO axrewards_menus (name) VALUES (?)
    """;

    private final String SELECT_MENU = """
            SELECT id FROM axrewards_menus WHERE name = ? LIMIT 1
    """;

    private final String SELECT_PLAYER_BY_UUID = """
            SELECT id FROM axrewards_players WHERE uuid = ?
    """;

    private final String INSERT_PLAYER = """
            INSERT INTO axrewards_players (uuid, name) VALUES (?, ?)
    """;

    private final String INSERT_REWARD = """
            INSERT INTO axrewards_rewards (name, menu_id) VALUES (?, ?)
    """;

    private final String SELECT_REWARD = """
            SELECT id FROM axrewards_rewards WHERE name = ? AND menu_id = ?
    """;

    private final String LAST_CLAIM = """
            SELECT time FROM axrewards_cooldowns WHERE player_id = ? AND reward_id = ? LIMIT 1;
    """;

    private final String CLAIM_REWARD = """
            INSERT INTO axrewards_cooldowns (player_id, reward_id, time) VALUES (?, ?, ?)
    """;

    private final String RESET_REWARD_SPECIFIC = """
            DELETE FROM axrewards_cooldowns WHERE player_id = ? AND reward_id = ?
    """;

    private final String RESET_REWARD_MENU = """
            DELETE FROM axrewards_cooldowns WHERE player_id = ? AND reward_id IN 
            (SELECT id FROM axrewards_rewards WHERE menu_id = ?)
    """;

    private final String RESET_REWARD_ALL = """
            DELETE FROM axrewards_cooldowns WHERE player_id = ?
    """;

    public abstract Connection getConnection();

    @Override
    public abstract String getType();

    public QueryRunner getRunner() {
        return runner;
    }

    @Override
    public void setup() {
        runner = new QueryRunner();

        try (Connection conn = getConnection()) {
            runner.execute(conn, CREATE_TABLE1);
            runner.execute(conn, CREATE_TABLE2);
            runner.execute(conn, CREATE_TABLE3);
            runner.execute(conn, CREATE_TABLE4);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        reload();

        try (Connection conn = getConnection()) {
            if (SQLUtils.tableExists(conn, "axrewards_claimed")) {
                new Converter3(this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reload() {
        try (Connection conn = getConnection()) {
            for (Map.Entry<String, Menu> entry : MenuManager.getMenus().entrySet()) {
                runner.execute(conn, INSERT_MENU, entry.getKey());
                for (Reward reward : entry.getValue().rewards()) {
                    runner.execute(conn, INSERT_REWARD, reward.name(), getMenuId(reward.menu()));
                }
            }
        } catch (SQLException ex) {
            // ignore errors caused by data already existing
        }
    }

    @Override
    public int getPlayerId(OfflinePlayer player) {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        try (Connection conn = getConnection()) {
            Integer id = runner.query(conn, SELECT_PLAYER_BY_UUID, scalarHandler, player.getUniqueId().toString());
            if (id != null) return id;
            return runner.insert(conn, INSERT_PLAYER, scalarHandler, player.getUniqueId().toString(), player.getName());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        throw new RuntimeException("Could not create user in database!");
    }

    @Override
    public int getMenuId(Menu menu) {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        try (Connection conn = getConnection()) {
            return runner.query(conn, SELECT_MENU, scalarHandler, menu.name());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        throw new RuntimeException("Could not find menu " + menu.name() + " in database!");
    }

    @Override
    public int getRewardId(Reward reward) {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        try (Connection conn = getConnection()) {
            return runner.query(conn, SELECT_REWARD, scalarHandler, reward.name(), getMenuId(reward.menu()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        throw new RuntimeException("Could not find reward " + reward.name() + " in database!");
    }

    @Override
    public long getLastClaim(OfflinePlayer player, Reward reward) {
        ScalarHandler<Long> scalarHandler = new ScalarHandler<>();
        try (Connection conn = getConnection()) {
            Long n = runner.query(conn, LAST_CLAIM, scalarHandler, getPlayerId(player), getRewardId(reward));
            if (n != null) return n;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1; // something went wrong
        }
        return 0; // never claimed it
    }

    @Override
    public void claimReward(OfflinePlayer player, Reward reward) {
        claimReward(player, reward, System.currentTimeMillis());
    }

    public void claimReward(OfflinePlayer player, Reward reward, long time) {
        resetReward(player, reward);
        try (Connection conn = getConnection()) {
            runner.execute(conn, CLAIM_REWARD, getPlayerId(player), getRewardId(reward), time);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resetReward(OfflinePlayer player, Reward reward) {
        try (Connection conn = getConnection()) {
            runner.execute(conn, RESET_REWARD_SPECIFIC, getPlayerId(player), getRewardId(reward));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resetReward(OfflinePlayer player, Menu menu) {
        try (Connection conn = getConnection()) {
            runner.execute(conn, RESET_REWARD_MENU, getPlayerId(player), getMenuId(menu));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resetReward(OfflinePlayer player) {
        try (Connection conn = getConnection()) {
            runner.execute(conn, RESET_REWARD_ALL, getPlayerId(player));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public abstract void disable();
}
