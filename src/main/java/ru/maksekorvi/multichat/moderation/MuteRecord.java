package ru.maksekorvi.multichat.moderation;

public class MuteRecord {
    private final String reason;
    private final long expiresAt;

    public MuteRecord(String reason, long expiresAt) {
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

    public String getReason() {
        return reason;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
