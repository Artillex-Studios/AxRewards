package com.artillexstudios.axrewards.database.impl;

import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLite implements Database {
    private Connection conn;

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    public void setup() {

        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s/data.db", AxRewards.getInstance().getDataFolder()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final String CREATE_TABLE = """
                        CREATE TABLE IF NOT EXISTS axrewards_claimed (
                        	uuid VARCHAR(36) NOT NULL,
                        	reward VARCHAR(512) NOT NULL,
                        	time BIGINT NOT NULL
                        );
                """;

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void claimReward(@NotNull UUID uuid, String name) {
        resetReward(uuid, name);
        final String sql = "INSERT INTO axrewards_claimed(uuid, reward, time) VALUES (?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resetReward(@NotNull UUID uuid, @Nullable String name) {
        if (name == null) {
            final String sql = "DELETE FROM axrewards_claimed WHERE uuid = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            final String sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name);
                stmt.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public long getLastClaimed(@NotNull UUID uuid, String name) {
        final String sql = "SELECT time FROM axrewards_claimed WHERE uuid = ? AND reward = ? LIMIT 1;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void disable() {
        try {
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
