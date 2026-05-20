package com.example.ticketscannerapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalAttendeeDatabase {
    private static final String PREF_NAME = "LocalAttendeeDB";
    private static final String KEY_ATTENDEES = "attendees";
    private static final String KEY_PENDING_CHECKINS = "pending_checkins";
    private static final String KEY_PENDING_CHECKOUTS = "pending_checkouts";

    private SharedPreferences prefs;
    private Gson gson;

    public LocalAttendeeDatabase(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Attendee model untuk local storage
    public static class LocalAttendee {
        public int id;
        public String email;
        public String firstName;
        public String lastName;
        public String publicId;
        public String status;
        public boolean isCheckedIn;
        public String checkedInAt;
        public String checkInShortId;  // short_id dari check_in object (untuk checkout)
        public long localCheckInTime; // timestamp lokal untuk tracking

        public String getFullName() {
            return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        }
    }

    // Simpan semua attendees
    public void saveAttendees(List<LocalAttendee> attendees) {
        String json = gson.toJson(attendees);
        prefs.edit().putString(KEY_ATTENDEES, json).apply();
    }

    // Ambil semua attendees
    public List<LocalAttendee> getAttendees() {
        String json = prefs.getString(KEY_ATTENDEES, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<LocalAttendee>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Cari attendee berdasarkan public ID
    public LocalAttendee findAttendeeByPublicId(String publicId) {
        List<LocalAttendee> attendees = getAttendees();
        for (LocalAttendee attendee : attendees) {
            if (attendee.publicId.equals(publicId)) {
                return attendee;
            }
        }
        return null;
    }

    // Check-in attendee secara lokal
    public void checkInLocal(String publicId) {
        List<LocalAttendee> attendees = getAttendees();
        for (LocalAttendee attendee : attendees) {
            if (attendee.publicId.equals(publicId)) {
                attendee.isCheckedIn = true;
                attendee.localCheckInTime = System.currentTimeMillis();
                break;
            }
        }
        saveAttendees(attendees);
        addPendingCheckIn(publicId);
    }

    // Check-out attendee secara lokal
    public void checkOutLocal(String publicId) {
        List<LocalAttendee> attendees = getAttendees();
        for (LocalAttendee attendee : attendees) {
            if (attendee.publicId.equals(publicId)) {
                attendee.isCheckedIn = false;
                attendee.checkedInAt = null;
                attendee.localCheckInTime = 0;
                break;
            }
        }
        saveAttendees(attendees);
        addPendingCheckOut(publicId);
    }

    // Pending check-ins (yang belum di-sync ke server)
    public void addPendingCheckIn(String publicId) {
        List<String> pending = getPendingCheckIns();
        if (!pending.contains(publicId)) {
            pending.add(publicId);
            savePendingCheckIns(pending);
        }
    }

    public List<String> getPendingCheckIns() {
        String json = prefs.getString(KEY_PENDING_CHECKINS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void savePendingCheckIns(List<String> publicIds) {
        String json = gson.toJson(publicIds);
        prefs.edit().putString(KEY_PENDING_CHECKINS, json).apply();
    }

    public void removePendingCheckIn(String publicId) {
        List<String> pending = getPendingCheckIns();
        pending.remove(publicId);
        savePendingCheckIns(pending);
    }

    // Pending check-outs
    public void addPendingCheckOut(String publicId) {
        List<String> pending = getPendingCheckOuts();
        if (!pending.contains(publicId)) {
            pending.add(publicId);
            savePendingCheckOuts(pending);
        }
    }

    public List<String> getPendingCheckOuts() {
        String json = prefs.getString(KEY_PENDING_CHECKOUTS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void savePendingCheckOuts(List<String> publicIds) {
        String json = gson.toJson(publicIds);
        prefs.edit().putString(KEY_PENDING_CHECKOUTS, json).apply();
    }

    public void removePendingCheckOut(String publicId) {
        List<String> pending = getPendingCheckOuts();
        pending.remove(publicId);
        savePendingCheckOuts(pending);
    }

    // Get statistics
    public int getTotalAttendees() {
        return getAttendees().size();
    }

    public int getCheckedInCount() {
        int count = 0;
        for (LocalAttendee attendee : getAttendees()) {
            if (attendee.isCheckedIn) count++;
        }
        return count;
    }

    // Clear all data
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}