package com.example.ticketscannerapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.ticketscannerapp.api.ApiClient;
import com.example.ticketscannerapp.models.CheckInBatchRequest;
import com.example.ticketscannerapp.models.PublicAttendeesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final int PER_PAGE = 200;
    private static final String PREF_NAME = "CheckInPrefs";

    public interface SyncListener { 
        void onSyncStart();
        void onSyncSuccess(int attendeesUpdated, int pendingSynced);
        void onSyncError(String error);
    }

    private final Context context;
    private final String checkInListId;
    private final LocalAttendeeDatabase localDb;
    private final SessionManager sessionManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Background thread untuk operasi DB — single thread agar tidak race condition
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private SyncListener listener;
    private volatile boolean isSyncing = false;
    private volatile boolean isRunning = false;

    public SyncManager(Context context, String checkInListId) {
        this.context = context.getApplicationContext();
        this.checkInListId = checkInListId;
        this.localDb = new LocalAttendeeDatabase(context);
        this.sessionManager = new SessionManager(context);
    }

    public void setListener(SyncListener listener) {
        this.listener = listener;
    }

    public void startSync() {
        isRunning = true;
        triggerSync();
    }

    public void stopSync() {
        isRunning = false;
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void triggerSync() {
        if (!isRunning) return;
        // Jalankan di background thread agar tidak blokir UI
        dbExecutor.execute(this::performSync);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String getCookieToken() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) return null;
        return "token=" + token;
    }

    // ─── Main Sync Flow ───────────────────────────────────────────────────────
    // Dijalankan di background thread (dbExecutor)

    private void performSync() {
        if (isSyncing) return;
        isSyncing = true;

        notifyStart();

        String cookie = getCookieToken();
        if (cookie == null) {
            Log.e(TAG, "No token, aborting sync");
            finishSync(false, "Token tidak tersedia");
            return;
        }

        // Step 1: push check-ins (blocking di background thread)
        pushCheckInsSync(cookie);

        // Step 2: push check-outs (blocking di background thread)
        pushCheckOutsSync(cookie);

        // Step 3: fetch attendees dari server (async, lanjut via callback)
        fetchAttendeesFromServer(1, 0, cookie);
    }

    // ─── Step 1: Push Check-Ins (blocking) ───────────────────────────────────

    private void pushCheckInsSync(String cookie) {
        List<String> pending = localDb.getPendingCheckIns();
        if (pending.isEmpty()) return;

        String gateName = sessionManager.getGateName();
        if (gateName == null || gateName.isEmpty()) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            gateName = prefs.getString("selected_gate", "Unknown");
        }

        Log.d(TAG, "Pushing " + pending.size() + " check-in(s) | gate: " + gateName);

        CheckInBatchRequest request = new CheckInBatchRequest(new ArrayList<>(pending), gateName);

        try {
            Response<ResponseBody> response = ApiClient.getApiService()
                    .batchCheckIn(checkInListId, request, cookie)
                    .execute(); // blocking

            if (response.isSuccessful()) {
                for (String publicId : pending) {
                    localDb.removePendingCheckIn(publicId);
                }
                Log.d(TAG, "Check-in push success: " + pending.size());
            } else {
                Log.e(TAG, "Check-in push failed: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Check-in push exception: " + e.getMessage());
        }
    }

    // ─── Step 2: Push Check-Outs (blocking) ──────────────────────────────────

    private void pushCheckOutsSync(String cookie) {
        List<String> pending = localDb.getPendingCheckOuts();
        if (pending.isEmpty()) return;

        for (String publicId : new ArrayList<>(pending)) {
            LocalAttendeeDatabase.LocalAttendee attendee = localDb.findAttendeeByPublicId(publicId);
            if (attendee == null || attendee.checkInShortId == null) {
                localDb.removePendingCheckOut(publicId);
                continue;
            }

            try {
                Response<ResponseBody> response = ApiClient.getApiService()
                        .checkOut(checkInListId, attendee.checkInShortId, cookie)
                        .execute(); // blocking

                if (response.isSuccessful()) {
                    localDb.removePendingCheckOut(publicId);
                    Log.d(TAG, "Check-out success: " + publicId);
                } else {
                    Log.e(TAG, "Check-out failed: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Check-out exception: " + e.getMessage());
            }
        }
    }

    // ─── Step 3: Fetch Attendees (async, paginated) ───────────────────────────

    private void fetchAttendeesFromServer(int page, int totalFetched, String cookie) {
        ApiClient.getApiService().getPublicAttendees(
                checkInListId, page, PER_PAGE, null, null, null, null
        ).enqueue(new Callback<PublicAttendeesResponse>() {
            @Override
            public void onResponse(Call<PublicAttendeesResponse> call, Response<PublicAttendeesResponse> response) {
                if (!isRunning) return;

                if (response.isSuccessful() && response.body() != null) {
                    PublicAttendeesResponse body = response.body();
                    List<PublicAttendeesResponse.AttendeeData> attendeeList = body.getData();
                    int newTotal = totalFetched + (attendeeList != null ? attendeeList.size() : 0);

                    // Merge di background thread agar tidak blokir UI
                    dbExecutor.execute(() -> {
                        if (attendeeList != null && !attendeeList.isEmpty()) {
                            mergeAttendeesToLocal(attendeeList);
                        }

                        if (body.hasNextPage()) {
                            fetchAttendeesFromServer(page + 1, newTotal, cookie);
                        } else {
                            Log.d(TAG, "Fetch complete. Total: " + newTotal);
                            finishSync(true, null);
                        }
                    });
                } else {
                    Log.e(TAG, "Fetch failed: " + response.code());
                    finishSync(false, "Fetch failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PublicAttendeesResponse> call, Throwable t) {
                Log.e(TAG, "Fetch error: " + t.getMessage());
                finishSync(false, "Fetch error: " + t.getMessage());
            }
        });
    }

    // ─── Selesai: notifikasi + langsung mulai lagi ────────────────────────────

    private void finishSync(boolean success, String errorMsg) {
        int pendingSynced = localDb.getPendingCheckIns().size();
        int total = localDb.getTotalAttendees();

        isSyncing = false;

        if (success) {
            notifySuccess(total, pendingSynced);
        } else {
            notifyError(errorMsg);
        }

        // Langsung mulai siklus sync berikutnya tanpa jeda
        triggerSync();
    }

    // ─── Merge Attendees (dipanggil dari background thread) ───────────────────

    private void mergeAttendeesToLocal(List<PublicAttendeesResponse.AttendeeData> serverList) {
        List<LocalAttendeeDatabase.LocalAttendee> localList = localDb.getAttendees();
        List<String> pendingCheckIns = localDb.getPendingCheckIns();

        java.util.Map<String, LocalAttendeeDatabase.LocalAttendee> localMap = new java.util.HashMap<>();
        for (LocalAttendeeDatabase.LocalAttendee la : localList) {
            localMap.put(la.publicId, la);
        }

        List<LocalAttendeeDatabase.LocalAttendee> merged = new ArrayList<>();

        for (PublicAttendeesResponse.AttendeeData serverAttendee : serverList) {
            LocalAttendeeDatabase.LocalAttendee local = localMap.get(serverAttendee.getPublicId());
            if (local == null) local = new LocalAttendeeDatabase.LocalAttendee();

            local.id = serverAttendee.getId();
            local.email = serverAttendee.getEmail();
            local.firstName = serverAttendee.getFirstName();
            local.lastName = serverAttendee.getLastName();
            local.publicId = serverAttendee.getPublicId();
            local.status = serverAttendee.getStatus();

            if (pendingCheckIns.contains(serverAttendee.getPublicId())) {
                // Case 1: masih pending → biarkan status lokal

            } else if (serverAttendee.isCheckedIn()) {
                // Case 2: server konfirmasi → update
                local.isCheckedIn = true;
                local.checkedInAt = serverAttendee.getCheckIn().getCheckedInAt();
                local.checkInShortId = serverAttendee.getCheckIn().getShortId();

            } else if (local.isCheckedIn) {
                // Case 3: lokal check-in, server belum reflect → pertahankan
                Log.d(TAG, "Preserving local check-in: " + local.publicId);

            } else {
                // Case 4: keduanya belum → reset
                local.isCheckedIn = false;
                local.checkedInAt = null;
                local.checkInShortId = null;
            }

            merged.add(local);
        }

        localDb.saveAttendees(merged);
    }

    // ─── Listener helpers (selalu ke main thread) ─────────────────────────────

    private void notifyStart() {
        if (listener != null) mainHandler.post(() -> listener.onSyncStart());
    }

    private void notifySuccess(int total, int pending) {
        if (listener != null) mainHandler.post(() -> listener.onSyncSuccess(total, pending));
    }

    private void notifyError(String err) {
        if (listener != null) mainHandler.post(() -> listener.onSyncError(err));
    }
}