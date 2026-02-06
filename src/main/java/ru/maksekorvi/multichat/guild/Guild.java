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
    private final Map<UUID, Long> reliabilityUpdatedAt;
    private final java.util.Set<UUID> mutedMembers;
    private java.util.List<String> rules;
    private String motd;

    public Guild(String name, int level, int guildPoints, double guildCurrency, Map<UUID, Integer> members, Map<UUID, Integer> reliability,
                 Map<UUID, Long> reliabilityUpdatedAt, java.util.Set<UUID> mutedMembers, java.util.List<String> rules, String motd) {
        this.name = name;
        this.level = level;
        this.guildPoints = guildPoints;
        this.guildCurrency = guildCurrency;
        this.members = members;
        this.reliability = reliability;
        this.reliabilityUpdatedAt = reliabilityUpdatedAt;
        this.mutedMembers = mutedMembers;
        this.rules = rules;
        this.motd = motd;
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

    public Map<UUID, Long> getReliabilityUpdatedAt() {
        return reliabilityUpdatedAt;
    }

    public java.util.Set<UUID> getMutedMembers() {
        return mutedMembers;
    }

    public void addGuildCurrency(double amount) {
        guildCurrency += amount;
    }

    public java.util.List<String> getRules() {
        return rules;
    }

    public void setRules(java.util.List<String> rules) {
        this.rules = rules;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }
}
