package ru.maksekorvi.multichat.chat;

public enum ChatChannel {
    GLOBAL("global"),
    LOCAL("local"),
    GUILD("guild");

    private final String id;

    ChatChannel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ChatChannel fromId(String id) {
        for (ChatChannel channel : values()) {
            if (channel.id.equalsIgnoreCase(id)) {
                return channel;
            }
        }
        return LOCAL;
    }
}
