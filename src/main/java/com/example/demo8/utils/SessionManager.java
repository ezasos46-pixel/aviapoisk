package com.example.demo8.utils;

import com.example.demo8.models.User;

import java.util.UUID;
import java.util.prefs.Preferences;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private static final Preferences PREFS = Preferences.userNodeForPackage(SessionManager.class);
    private static final String KEY_ID    = "session_id";
    private static final String KEY_EMAIL = "session_email";
    private static final String KEY_PHONE = "session_phone";

    private SessionManager() {
        restore();
    }

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            PREFS.put(KEY_ID,    user.getId().toString());
            PREFS.put(KEY_EMAIL, user.getEmail() != null ? user.getEmail() : "");
            PREFS.put(KEY_PHONE, user.getPhone() != null ? user.getPhone() : "");
        } else {
            clear();
        }
    }

    public boolean isLoggedIn() { return currentUser != null; }

    public void logout() {
        this.currentUser = null;
        clear();
    }

    private void restore() {
        String id = PREFS.get(KEY_ID, null);
        if (id == null) return;
        try {
            User user = new User();
            user.setId(UUID.fromString(id));
            user.setEmail(PREFS.get(KEY_EMAIL, ""));
            user.setPhone(PREFS.get(KEY_PHONE, ""));
            this.currentUser = user;
        } catch (Exception ignored) {
            clear();
        }
    }

    private void clear() {
        PREFS.remove(KEY_ID);
        PREFS.remove(KEY_EMAIL);
        PREFS.remove(KEY_PHONE);
    }
}



