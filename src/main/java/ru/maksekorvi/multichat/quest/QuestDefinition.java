package ru.maksekorvi.multichat.quest;

public class QuestDefinition {
    private final String id;
    private final QuestType type;
    private final String target;
    private final int amount;
    private final double rewardMoney;
    private final int rewardExp;
    private final int rewardGuildPoints;
    private final double rewardGuildCurrency;

    public QuestDefinition(String id, QuestType type, String target, int amount, double rewardMoney, int rewardExp,
                           int rewardGuildPoints, double rewardGuildCurrency) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.rewardMoney = rewardMoney;
        this.rewardExp = rewardExp;
        this.rewardGuildPoints = rewardGuildPoints;
        this.rewardGuildCurrency = rewardGuildCurrency;
    }

    public String getId() {
        return id;
    }

    public QuestType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public int getRewardGuildPoints() {
        return rewardGuildPoints;
    }

    public double getRewardGuildCurrency() {
        return rewardGuildCurrency;
    }
}
