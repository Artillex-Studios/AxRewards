package com.artillexstudios.axrewards.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;

public class MySQL extends Base {
    private final HikariConfig hConfig = new HikariConfig();
    private HikariDataSource dataSource;

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getType() {
        return "MySQL";
    }

    @Override
    public void setup() {
        hConfig.setPoolName("axrewards-pool");

        // Ensure these values are sensible
        int maxPoolSize = CONFIG.getInt("database.pool.maximum-pool-size", 10); // default 10
        int minIdle = CONFIG.getInt("database.pool.minimum-idle", 2); // default 2
        int maxLifetime = CONFIG.getInt("database.pool.maximum-lifetime", 1800000); // default 30 minutes
        int keepAliveTime = CONFIG.getInt("database.pool.keepalive-time", 30000); // default 30 seconds
        int connectionTimeout = CONFIG.getInt("database.pool.connection-timeout", 30000); // default 30 seconds

        hConfig.setMaximumPoolSize(maxPoolSize);
        hConfig.setMinimumIdle(minIdle);
        hConfig.setMaxLifetime(maxLifetime);
        hConfig.setKeepaliveTime(keepAliveTime);
        hConfig.setConnectionTimeout(connectionTimeout);

        hConfig.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Updated to use the latest MySQL driver
        hConfig.setJdbcUrl("jdbc:mysql://" + CONFIG.getString("database.address") + ":" + CONFIG.getString("database.port") + "/" + CONFIG.getString("database.database") + "?useSSL=false&serverTimezone=UTC"); // Added parameters to avoid warnings
        hConfig.addDataSourceProperty("user", CONFIG.getString("database.username"));
        hConfig.addDataSourceProperty("password", CONFIG.getString("database.password"));

        try {
            dataSource = new HikariDataSource(hConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        super.setup();
    }

    @Override
    public void disable() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
