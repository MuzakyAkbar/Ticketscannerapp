package com.example.ticketscannerapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.ticketscannerapp.models.User;

public class SessionManager {
    private static final String PREF_NAME = "TicketScannerSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GATE = "gate";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_ACCOUNT_NAME = "accountName";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_GATE, user.getGate());
        editor.putString(KEY_TOKEN, user.getToken());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putInt(KEY_ACCOUNT_ID, user.getAccountId());
        editor.putString(KEY_ACCOUNT_NAME, user.getAccountName());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getGate() {
        return prefs.getString(KEY_GATE, null);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public int getAccountId() {
        return prefs.getInt(KEY_ACCOUNT_ID, 0);
    }

    public String getAccountName() {
        return prefs.getString(KEY_ACCOUNT_NAME, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}