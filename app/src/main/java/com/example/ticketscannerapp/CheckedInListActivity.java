package com.example.ticketscannerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketscannerapp.adapters.CheckedInAdapter;
import com.example.ticketscannerapp.utils.LocalAttendeeDatabase;
import com.example.ticketscannerapp.utils.SessionManager;
import com.example.ticketscannerapp.utils.SyncManager;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CheckedInListActivity extends AppCompatActivity
        implements SyncManager.SyncListener, CheckedInAdapter.OnCheckInClickListener {

    private static final String TAG = "CheckedInListActivity";
    private static final long UI_REFRESH_INTERVAL = 1000;

    // Filter constants
    private static final int FILTER_ALL = 0;
    private static final int FILTER_CHECKED_IN = 1;
    private static final int FILTER_NOT_CHECKED_IN = 2;

    private RecyclerView recyclerView;
    private CheckedInAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvTitle;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;

    private String currentQuery = "";
    private int currentFilter = FILTER_ALL;

    private LocalAttendeeDatabase localDb;
    private SyncManager syncManager;
    private final List<LocalAttendeeDatabase.LocalAttendee> allAttendeeList = new ArrayList<>();

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable uiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshFromLocalDb();
            uiHandler.postDelayed(this, UI_REFRESH_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_in_list);

        SharedPreferences prefs = getSharedPreferences("CheckInPrefs", MODE_PRIVATE);
        String shortId = prefs.getString("check_in_list_id", null);

        localDb = new LocalAttendeeDatabase(this);
        initViews();

        if (shortId == null || shortId.isEmpty()) {
            showError("Check-in list ID not found. Please login again.");
            return;
        }

        syncManager = new SyncManager(this, shortId);
        syncManager.setListener(this);
        syncManager.startSync();

        refreshFromLocalDb();
        uiHandler.postDelayed(uiRefreshRunnable, UI_REFRESH_INTERVAL);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCheckInLists);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTitle = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckedInAdapter(allAttendeeList, this);
        adapter.setOnCheckInClickListener(this);  // daftarkan callback
        recyclerView.setAdapter(adapter);

        if (tvTitle != null) tvTitle.setText("Daftar Attendee");

        // Chip filter listener
        if (chipGroupFilter != null) {
            chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int id = checkedIds.get(0);
                if (id == R.id.chipCheckedIn) {
                    currentFilter = FILTER_CHECKED_IN;
                } else if (id == R.id.chipNotCheckedIn) {
                    currentFilter = FILTER_NOT_CHECKED_IN;
                } else {
                    currentFilter = FILTER_ALL;
                }
                applyFilterAndSearch();
            });
        }

        // Search listener
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    currentQuery = s != null ? s.toString() : "";
                    applyFilterAndSearch();
                }
            });
        }
    }

    // ─── Callback dari tombol Check-In di adapter ─────────────────────────────

    @Override
    public void onCheckInClick(LocalAttendeeDatabase.LocalAttendee attendee) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Check-In")
                .setMessage("Check-in untuk:\n\n" + attendee.getFullName() + "\n" + attendee.email)
                .setPositiveButton("Check-In", (dialog, which) -> doCheckIn(attendee))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void doCheckIn(LocalAttendeeDatabase.LocalAttendee attendee) {
        // Ambil nama gate dari SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String gate = sessionManager.getGateName();
        if (gate == null || gate.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("CheckInPrefs", MODE_PRIVATE);
            gate = prefs.getString("selected_gate", "Manual");
        }

        // Tandai check-in di lokal DB
        localDb.checkInLocal(attendee.publicId, gate);

        // Trigger sync segera supaya langsung dikirim ke server
        if (syncManager != null) {
            syncManager.triggerSyncNow();
        }

        Toast.makeText(this, "✓ Check-in berhasil: " + attendee.getFullName(), Toast.LENGTH_SHORT).show();

        // Refresh tampilan langsung
        refreshFromLocalDb();
    }

    // ─── Data & Filter ────────────────────────────────────────────────────────

    private void refreshFromLocalDb() {
        List<LocalAttendeeDatabase.LocalAttendee> all = localDb.getAttendees();

        allAttendeeList.clear();
        allAttendeeList.addAll(all);

        applyFilterAndSearch();

        int checkedInCount = 0;
        for (LocalAttendeeDatabase.LocalAttendee a : all) {
            if (a.isCheckedIn) checkedInCount++;
        }
        Log.d(TAG, "Total: " + all.size() + ", Checked-in: " + checkedInCount);

        showLoading(false);
    }

    private void applyFilterAndSearch() {
        List<LocalAttendeeDatabase.LocalAttendee> filtered = new ArrayList<>();
        for (LocalAttendeeDatabase.LocalAttendee a : allAttendeeList) {
            if (currentFilter == FILTER_CHECKED_IN && !a.isCheckedIn) continue;
            if (currentFilter == FILTER_NOT_CHECKED_IN && a.isCheckedIn) continue;
            filtered.add(a);
        }

        adapter.updateData(filtered, currentQuery);
        showEmptyState(adapter.getItemCount() == 0);
    }

    // ─── SyncListener ─────────────────────────────────────────────────────────

    @Override public void onSyncStart() {}

    @Override
    public void onSyncSuccess(int attendeesUpdated, int pendingSynced) {
        Log.d(TAG, "Sync ok — total: " + attendeesUpdated + ", pending: " + pendingSynced);
    }

    @Override
    public void onSyncError(String error) {
        Log.e(TAG, "Sync error: " + error);
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

    private void showError(String message) {
        showLoading(false);
        showEmptyState(true);
        if (tvEmptyState != null) tvEmptyState.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        if (tvEmptyState != null) tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        if (recyclerView != null && !show) recyclerView.setVisibility(View.VISIBLE);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        refreshFromLocalDb();
        uiHandler.postDelayed(uiRefreshRunnable, UI_REFRESH_INTERVAL);
        if (syncManager != null) syncManager.startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(uiRefreshRunnable);
        if (syncManager != null) syncManager.stopSync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(uiRefreshRunnable);
        if (syncManager != null) syncManager.stopSync();
    }
}