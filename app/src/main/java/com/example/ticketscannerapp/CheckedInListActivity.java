package com.example.ticketscannerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.ticketscannerapp.utils.SyncManager;

import java.util.ArrayList;
import java.util.List;

public class CheckedInListActivity extends AppCompatActivity implements SyncManager.SyncListener {

    private static final String TAG = "CheckedInListActivity";
    private static final long UI_REFRESH_INTERVAL = 1000;

    private RecyclerView recyclerView;
    private CheckedInAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvTitle;

    private LocalAttendeeDatabase localDb;
    private SyncManager syncManager;
    private final List<LocalAttendeeDatabase.LocalAttendee> checkedInList = new ArrayList<>();

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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckedInAdapter(checkedInList, this);
        recyclerView.setAdapter(adapter);

        if (tvTitle != null) tvTitle.setText("Checked-In List");
    }

    private void refreshFromLocalDb() {
        List<LocalAttendeeDatabase.LocalAttendee> all = localDb.getAttendees();

        checkedInList.clear();
        for (LocalAttendeeDatabase.LocalAttendee attendee : all) {
            if (attendee.isCheckedIn) {
                checkedInList.add(attendee);
            }
        }

        adapter.notifyDataSetChanged();
        showLoading(false);
        showEmptyState(checkedInList.isEmpty());

        Log.d(TAG, "Local DB checked-in count: " + checkedInList.size());
    }

    @Override public void onSyncStart() {}

    @Override
    public void onSyncSuccess(int attendeesUpdated, int pendingSynced) {
        Log.d(TAG, "Sync ok — total: " + attendeesUpdated + ", pending: " + pendingSynced);
    }

    @Override
    public void onSyncError(String error) {
        Log.e(TAG, "Sync error: " + error);
    }

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
        if (recyclerView != null) recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

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