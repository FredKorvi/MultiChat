package ru.maksekorvi.multichat.quest;

public class ActiveQuest {
    private final QuestDefinition definition;
    private int progress;

    public ActiveQuest(QuestDefinition definition) {
        this.definition = definition;
        this.progress = 0;
    }

    public QuestDefinition getDefinition() {
        return definition;
    }

    public int getProgress() {
        return progress;
    }

    public void addProgress(int amount) {
        this.progress += amount;
    }

    public boolean isCompleted() {
        return progress >= definition.getAmount();
    }
}
