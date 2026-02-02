package ru.maksekorvi.multichat.guild;

import java.util.Map;
import java.util.UUID;

public class Guild {
    private final String name;
    private int level;
    private int guildPoints;
    private double guildCurrency;
    private final Map<UUID, Integer> members;
    private final Map<UUID, Integer> reliability;

    public Guild(String name, int level, int guildPoints, double guildCurrency, Map<UUID, Integer> members, Map<UUID, Integer> reliability) {
        this.name = name;
        this.level = level;
        this.guildPoints = guildPoints;
        this.guildCurrency = guildCurrency;
        this.members = members;
        this.reliability = reliability;
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

    public void setGuildPoints(int guildPoints) {
        this.guildPoints = guildPoints;
    }

    public double getGuildCurrency() {
        return guildCurrency;
    }

    public Map<UUID, Integer> getMembers() {
        return members;
    }

    public Map<UUID, Integer> getReliability() {
        return reliability;
    }

    public void addGuildCurrency(double amount) {
        guildCurrency += amount;
    }
}
