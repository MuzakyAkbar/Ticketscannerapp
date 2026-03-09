package com.example.ticketscannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ticketscannerapp.models.Ticket;
import com.example.ticketscannerapp.utils.BannerHelper;
import com.example.ticketscannerapp.utils.DataManager;
import com.example.ticketscannerapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageButton btnViewList, btnLogout;
    private TextView tvGateName, tvOperator, tvCheckedIn, tvTotalTickets, tvLastSync;
    private TextInputEditText etTicketId;
    private MaterialButton btnManualScan;
    private CardView cardBannerContainer;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.getInstance();

        initViews();
        setupListeners();
        loadToken();
        loadDataFromAPI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
        if (etTicketId != null) {
            etTicketId.requestFocus();
        }
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
        cardBannerContainer = findViewById(R.id.cardBannerContainer);

        setSupportActionBar(toolbar);
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

        if (btnManualScan != null) {
            btnManualScan.setOnClickListener(v -> processTicket());
        }

        if (etTicketId != null) {
            etTicketId.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    processTicket();
                    return true;
                }
                return false;
            });
        }
    }

    private void loadToken() {
        SharedPreferences prefs = getSharedPreferences("TicketScannerPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        String username = prefs.getString("username", "operator");

        if (token != null) {
            dataManager.setAuthToken(token);
        }

        if (tvOperator != null) {
            tvOperator.setText("Operator: " + username);
        }
    }

    private void loadDataFromAPI() {
        dataManager.loadAttendeesFromAPI(new DataManager.DataLoadCallback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                runOnUiThread(() -> {
                    updateStats();
                    updateLastSync();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    // Silent fail
                });
            }
        });
    }

    private void processTicket() {
        if (etTicketId == null) return;

        String ticketId = etTicketId.getText().toString().trim();

        if (ticketId.isEmpty()) {
            BannerHelper.showErrorBanner(this, cardBannerContainer, "Masukkan Ticket ID");
            hideBannerAfterDelay();
            return;
        }

        etTicketId.setEnabled(false);
        if (btnManualScan != null) {
            btnManualScan.setEnabled(false);
        }

        dataManager.checkInTicketAPI(ticketId, new DataManager.CheckInCallback() {
            @Override
            public void onSuccess(Ticket ticket) {
                runOnUiThread(() -> {
                    if (cardBannerContainer != null) {
                        cardBannerContainer.setVisibility(View.VISIBLE);
                        BannerHelper.showSuccessBanner(MainActivity.this, cardBannerContainer, ticket);
                        hideBannerAfterDelay();
                    }
                    updateStats();
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

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (cardBannerContainer != null) {
                        cardBannerContainer.setVisibility(View.VISIBLE);

                        Ticket ticket = dataManager.getTicket(ticketId);
                        if (ticket != null && ticket.isCheckedIn()) {
                            BannerHelper.showWarningBanner(MainActivity.this, cardBannerContainer, message, ticket);
                        } else {
                            BannerHelper.showErrorBanner(MainActivity.this, cardBannerContainer, message);
                        }

                        hideBannerAfterDelay();
                    }
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
        });
    }

    private void hideBannerAfterDelay() {
        if (cardBannerContainer != null) {
            cardBannerContainer.postDelayed(() -> {
                cardBannerContainer.setVisibility(View.GONE);
            }, 5000);
        }
    }

    private void updateStats() {
        int total = dataManager.getTotalTickets();
        int checkedIn = dataManager.getCheckedInCount();

        if (tvTotalTickets != null) {
            tvTotalTickets.setText(String.valueOf(total));
        }
        if (tvCheckedIn != null) {
            tvCheckedIn.setText(String.valueOf(checkedIn));
        }
    }

    private void updateLastSync() {
        if (tvLastSync != null) {
            String currentTime = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
            tvLastSync.setText("Last sync: " + currentTime);
        }
    }

    private void logout() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}