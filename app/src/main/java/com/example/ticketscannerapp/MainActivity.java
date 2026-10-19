package com.example.ticketscannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ticketscannerapp.utils.BannerHelper;
import com.example.ticketscannerapp.utils.LocalAttendeeDatabase;
import com.example.ticketscannerapp.utils.SessionManager;
import com.example.ticketscannerapp.utils.SyncManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String SCAN_DEBUG = "SCAN_DEBUG";
    private static final String PREF_NAME = "CheckInPrefs";

    private MaterialToolbar toolbar;
    private ImageButton btnViewList, btnLogout;
    private TextView tvGateName, tvOperator, tvCheckedIn, tvTotalTickets, tvLastSync, tvSyncStatus;
    private TextInputEditText etTicketId;
    private MaterialButton btnManualScan;
    private MaterialButton btnCameraScan;
    private MaterialButton btnManualSync;
    private CardView cardBannerContainer;

    private LocalAttendeeDatabase localDb;
    private SyncManager syncManager;
    private SessionManager sessionManager;
    private String checkInListId;
    private String checkInListName;
    private String selectedGate;
    private String scanMode;
    private Runnable hideBannerRunnable;
    private final Handler scanHandler = new Handler(Looper.getMainLooper());
    private Runnable scanRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load check-in list info
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        checkInListId = prefs.getString("check_in_list_id", null);
        checkInListName = prefs.getString("check_in_list_name", "Check-In Gate");
        selectedGate = prefs.getString("selected_gate", "Unknown Gate");
        scanMode = prefs.getString("scan_mode", "Check-In");

        if (checkInListId == null) {
            navigateToLogin();
            return;
        }

        sessionManager = new SessionManager(this);
        localDb = new LocalAttendeeDatabase(this);
        syncManager = new SyncManager(this, checkInListId);

        initViews();
        setupListeners();
        setupSyncListener();
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncManager.startSync();
        updateStats();
        if (etTicketId != null) {
            etTicketId.requestFocus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncManager.stopSync();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnViewList = findViewById(R.id.btnViewList);
        btnLogout = findViewById(R.id.btnLogout);
        tvGateName = findViewById(R.id.tvGateName);
        tvOperator = findViewById(R.id.tvOperator);
        tvCheckedIn = findViewById(R.id.tvCheckedIn);
        tvTotalTickets = findViewById(R.id.tvTotalTickets);
        tvLastSync = findViewById(R.id.tvLastSync);
        etTicketId = findViewById(R.id.etTicketId);
        btnManualScan = findViewById(R.id.btnManualScan);
        btnCameraScan = findViewById(R.id.btnCameraScan);
        btnManualSync = findViewById(R.id.btnManualSync);
        cardBannerContainer = findViewById(R.id.cardBannerContainer);

        boolean isCheckTicket = "Check-Ticket".equals(scanMode);

        if (tvGateName != null) {
            if (isCheckTicket) {
                tvGateName.setText("Mode: Check-Ticket");
            } else {
                tvGateName.setText(selectedGate + "  \u00b7  " + scanMode);
            }
        }

        if (tvOperator != null) {
            tvOperator.setText(checkInListName + "  |  " + checkInListId);
        }

        // Mode Check-Ticket: sembunyikan tombol lihat list
        if (btnViewList != null) {
            btnViewList.setVisibility(isCheckTicket ? android.view.View.GONE : android.view.View.VISIBLE);
        }

        // Ubah label tombol manual sesuai mode
        if (btnManualScan != null) {
            btnManualScan.setText(isCheckTicket ? "Cek Ticket" : "Manual Check In");
        }
       // setSupportActionBar(toolbar); //ini nanti untuk perangkat baru ga dipakai
    }

    private void setupListeners() {
        if (btnViewList != null) {
            btnViewList.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CheckedInListActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        if (btnCameraScan != null) {
            btnCameraScan.setOnClickListener(v -> startCameraScan());
        }

        if (btnManualScan != null) {
            btnManualScan.setOnClickListener(v -> processTicket());
        }

        if (btnManualSync != null) {
            btnManualSync.setOnClickListener(v -> {
                if (syncManager != null) {
                    btnManualSync.setEnabled(false);
                    btnManualSync.setText("Syncing...");
                    syncManager.triggerSyncNow();
                    // Re-enable setelah 3 detik
                    new Handler().postDelayed(() -> {
                        btnManualSync.setEnabled(true);
                        btnManualSync.setText("Sync");
                    }, 3000);
                }
            });
        }

        if (etTicketId != null) {
            // 1. IME action — keyboard software (tombol Done)
            etTicketId.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    cancelScanTimer();
                    processTicket();
                    return true;
                }
                return false;
            });

            // 2. KeyEvent — hardware scanner yang inject ENTER langsung
            etTicketId.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_ENTER) {
                    cancelScanTimer();
                    processTicket();
                    return true;
                }
                return false;
            });

            // 3. TextWatcher — fallback untuk hardware scanner yang TIDAK kirim ENTER
            etTicketId.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    cancelScanTimer();
                    String text = s.toString().trim();
                    if (text.isEmpty()) return;

                    scanRunnable = () -> {
                        if (etTicketId.isEnabled()) {
                            processTicket();
                        }
                    };
                    scanHandler.postDelayed(scanRunnable, 150);
                }
            });
        }
    }

    private void setupSyncListener() {
        syncManager.setListener(new SyncManager.SyncListener() {
            @Override
            public void onSyncStart() {
                // Optional: show sync indicator
            }

            @Override
            public void onSyncSuccess(int attendeesUpdated, int pendingSynced) {
                runOnUiThread(() -> {
                    updateStats();
                    updateLastSync();
                });
            }

            @Override
            public void onSyncError(String error) {
                runOnUiThread(() -> {
                    // Silent fail, keep using local data
                });
            }
        });
    }

    private void cancelScanTimer() {
        if (scanRunnable != null) {
            scanHandler.removeCallbacks(scanRunnable);
            scanRunnable = null;
        }
    }

    private void processTicket() {
        if (etTicketId == null) return;

        String rawInput = etTicketId.getText().toString();
        String ticketId = rawInput.trim().toUpperCase();

        // ── DEBUG: log input vs semua publicId di DB ──────────────────────────
        Log.d(SCAN_DEBUG, "Raw input    : [" + rawInput + "]");
        Log.d(SCAN_DEBUG, "After process: [" + ticketId + "]");
        List<LocalAttendeeDatabase.LocalAttendee> allAttendees = localDb.getAttendees();
        Log.d(SCAN_DEBUG, "DB size: " + allAttendees.size());
        for (LocalAttendeeDatabase.LocalAttendee a : allAttendees) {
            Log.d(SCAN_DEBUG, "DB publicId: [" + a.publicId + "] | match=" + ticketId.equals(a.publicId));
        }
        // ─────────────────────────────────────────────────────────────────────

        if (ticketId.isEmpty()) {
            showBanner(() -> BannerHelper.showErrorBanner(this, cardBannerContainer, "Masukkan Ticket ID"));
            return;
        }

        etTicketId.setEnabled(false);
        if (btnManualScan != null) {
            btnManualScan.setEnabled(false);
        }

        LocalAttendeeDatabase.LocalAttendee attendee = localDb.findAttendeeByPublicId(ticketId);

        if (attendee == null) {
            Log.d(SCAN_DEBUG, "Attendee NOT FOUND for: [" + ticketId + "]");
            if ("Check-Ticket".equals(scanMode)) {
                showCheckTicketResult("not_found");
            } else {
                showError("Ticket tidak ditemukan: " + ticketId);
            }
            resetInput();
            return;
        }

        Log.d(SCAN_DEBUG, "Attendee FOUND: " + attendee.getFullName() + " | isCheckedIn=" + attendee.isCheckedIn);

        if ("Check-Ticket".equals(scanMode)) {
            // Mode check-ticket: hanya tampilkan status, tidak ubah data sama sekali
            if (attendee.isCheckedIn) {
                showCheckTicketResult("already");
            } else {
                showCheckTicketResult("valid");
            }
            resetInput();
            return;
        }

        // Mode check-in: proses check-in normal
        if (attendee.isCheckedIn) {
            showWarning("Ticket sudah di-check-in sebelumnya", attendee);
            resetInput();
            return;
        }

        // Check-in locally, gate akan disertakan saat sync ke server
        localDb.checkInLocal(ticketId, selectedGate);

        showSuccess(attendee);
        resetInput();
        updateStats();
    }

    private void showSuccess(LocalAttendeeDatabase.LocalAttendee attendee) {
        runOnUiThread(() -> {
            if (cardBannerContainer != null) {
                com.example.ticketscannerapp.models.Ticket ticket = convertToTicket(attendee);
                showBanner(() -> BannerHelper.showSuccessBanner(this, cardBannerContainer, ticket));
            }
        });
    }

    private void showCheckTicketResult(String state) {
        runOnUiThread(() -> {
            if (cardBannerContainer != null) {
                showBanner(() -> BannerHelper.showCheckTicketBanner(this, cardBannerContainer, state));
            }
        });
    }

    private void showWarning(String message, LocalAttendeeDatabase.LocalAttendee attendee) {
        runOnUiThread(() -> {
            if (cardBannerContainer != null) {
                com.example.ticketscannerapp.models.Ticket ticket = convertToTicket(attendee);
                showBanner(() -> BannerHelper.showWarningBanner(this, cardBannerContainer, message, ticket));
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            if (cardBannerContainer != null) {
                showBanner(() -> BannerHelper.showErrorBanner(this, cardBannerContainer, message));
            }
        });
    }

    private com.example.ticketscannerapp.models.Ticket convertToTicket(LocalAttendeeDatabase.LocalAttendee attendee) {
        com.example.ticketscannerapp.models.Ticket ticket = new com.example.ticketscannerapp.models.Ticket(
                attendee.publicId,
                attendee.getFullName(),
                attendee.email,
                "Standard",
                0,
                checkInListName,
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())
        );
        ticket.setCheckedIn(attendee.isCheckedIn);
        if (attendee.checkedInAt != null) {
            ticket.setCheckInTime(attendee.checkedInAt);
        }
        return ticket;
    }

    private void resetInput() {
        runOnUiThread(() -> {
            cancelScanTimer();
            if (etTicketId != null) {
                etTicketId.setText("");
                etTicketId.setEnabled(true);
                etTicketId.requestFocus();
            }
            if (btnManualScan != null) {
                btnManualScan.setEnabled(true);
            }
        });
    }

    private void showBanner(Runnable showAction) {
        if (cardBannerContainer == null) return;
        if (hideBannerRunnable != null) {
            cardBannerContainer.removeCallbacks(hideBannerRunnable);
            hideBannerRunnable = null;
        }
        showAction.run();
        cardBannerContainer.setVisibility(View.VISIBLE);
        hideBannerRunnable = () -> {
            cardBannerContainer.setVisibility(View.GONE);
            hideBannerRunnable = null;
        };
        cardBannerContainer.postDelayed(hideBannerRunnable, 3000);
    }

    private void updateStats() {
        int total = localDb.getTotalAttendees();
        int checkedIn = localDb.getCheckedInCount();

        if (tvTotalTickets != null) {
            tvTotalTickets.setText(String.valueOf(total));
        }
        if (tvCheckedIn != null) {
            tvCheckedIn.setText(String.valueOf(checkedIn));
        }
    }

    private void updateLastSync() {
        if (tvLastSync != null) {
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            tvLastSync.setText("Synced: " + currentTime);
        }
    }

    private void logout() {
        syncManager.stopSync();
        sessionManager.logout();
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        navigateToLogin();
    }

    private void startCameraScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE, IntentIntegrator.CODE_128, IntentIntegrator.CODE_39);
        integrator.setPrompt("Arahkan kamera ke barcode / QR ticket");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scanned = result.getContents().trim();
                if (etTicketId != null) {
                    etTicketId.setText(scanned);
                }
                // Langsung proses tanpa tunggu TextWatcher
                cancelScanTimer();
                processTicket();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}