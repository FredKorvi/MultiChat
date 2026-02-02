package ru.maksekorvi.multichat.guild;

import java.util.Map;
import java.util.UUID;

public class Guild {
    private final String name;
    private int level;
    private int guildPoints;
    private double guildCurrency;
    private final Map<UUID, Integer> members;

    public Guild(String name, int level, int guildPoints, double guildCurrency, Map<UUID, Integer> members) {
        this.name = name;
        this.level = level;
        this.guildPoints = guildPoints;
        this.guildCurrency = guildCurrency;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getGuildPoints() {
        return guildPoints;
    }

    public double getGuildCurrency() {
        return guildCurrency;
    }

    public Map<UUID, Integer> getMembers() {
        return members;
    }

    public void addGuildCurrency(double amount) {
        guildCurrency += amount;
    }
}
