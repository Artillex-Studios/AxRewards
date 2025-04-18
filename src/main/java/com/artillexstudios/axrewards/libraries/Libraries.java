package com.artillexstudios.axrewards.libraries;

import revxrsal.zapper.Dependency;
import revxrsal.zapper.relocation.Relocation;

import java.util.ArrayList;
import java.util.List;

public enum Libraries {

    MYSQL_CONNECTOR("com{}mysql:mysql-connector-j:9.2.0", relocation("com{}mysql", "com.artillexstudios.axrewards.libs.mysql")),

    SQLITE_JDBC("org{}xerial:sqlite-jdbc:3.49.1.0"),

    H2_JDBC("com{}h2database:h2:2.1.214"),

    POSTGRESQL("org{}postgresql:postgresql:42.7.5", relocation("org{}postgresql", "com.artillexstudios.axrewards.libs.postgresql")),

    HIKARICP("com{}zaxxer:HikariCP:6.3.0", relocation("com{}zaxxer{}hikari", "com.artillexstudios.axrewards.libs.hikari")),

    COMMONS_DBUTILS("commons-dbutils:commons-dbutils:1.8.1");

    private final List<Relocation> relocations = new ArrayList<>();
    private final Dependency library;

    public Dependency fetchLibrary() {
        return this.library;
    }

    private static Relocation relocation(String from, String to) {
        return new Relocation(from.replace("{}", "."), to);
    }

    public List<Relocation> relocations() {
        return List.copyOf(this.relocations);
    }

    Libraries(String lib, Relocation relocation) {
        String[] split = lib.replace("{}", ".").split(":");

        this.library = new Dependency(split[0], split[1], split[2]);
        this.relocations.add(relocation);
    }

    Libraries(String lib) {
        String[] split = lib.replace("{}", ".").split(":");

        this.library = new Dependency(split[0], split[1], split[2]);
    }
}
