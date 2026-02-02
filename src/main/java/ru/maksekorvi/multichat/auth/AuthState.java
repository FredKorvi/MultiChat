package ru.maksekorvi.multichat.auth;

public class AuthState {
    private final boolean registered;
    private final boolean loggedIn;

    public AuthState(boolean registered, boolean loggedIn) {
        this.registered = registered;
        this.loggedIn = loggedIn;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
