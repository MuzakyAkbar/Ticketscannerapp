package com.example.ticketscannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketscannerapp.api.ApiClient;
import com.example.ticketscannerapp.api.ApiService;
import com.example.ticketscannerapp.models.CheckInListDetailResponse;
import com.example.ticketscannerapp.models.LoginRequest;
import com.example.ticketscannerapp.models.LoginResponse;
import com.example.ticketscannerapp.models.User;
import com.example.ticketscannerapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREF_REMEMBER = "RememberMe";
    private static final String PREF_EMAIL = "SavedEmail";
    private static final String PREF_PASSWORD = "SavedPassword";
    private static final String PREF_CHECK_IN_URL = "SavedCheckInUrl";
    private static final String PREF_GATE = "SavedGate";
    private static final String PREF_SCAN_MODE = "SavedScanMode";
    private static final String PREF_NAME = "CheckInPrefs";

    // Scan mode options
    private static final String[] SCAN_MODE_LIST = {
            "Check-In", "Check-Ticket"
    };

    // Daftar gate yang tersedia
    private static final String[] GATE_LIST = {
            "Gate 1", "Gate 2", "Gate 3", "Gate 4", "Gate 5",
            "Gate 6", "Gate 7", "Gate 8", "Gate 9", "Gate 10",
            "Gate 11", "Gate 12", "Gate 13", "Gate 14", "Gate 15",
            "Gate 16", "Gate 17", "Gate 18",
            "VIP Utara", "VIP Selatan", "VVIP"
    };

    private TextInputEditText etUsername, etPassword, etCheckInUrl;
    private MaterialButton btnScanQr;
    private MaterialButton btnClearUrl;
    private MaterialButton btnLogin;
    private CheckBox cbRememberMe;
    private AutoCompleteTextView spinnerScanMode;
    private AutoCompleteTextView spinnerGate;
    private com.google.android.material.textfield.TextInputLayout tilGate;
    private SessionManager sessionManager;
    private SharedPreferences prefs;
    private SharedPreferences checkInPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        checkInPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if already logged in with valid check-in list
        if (sessionManager.isLoggedIn() && hasValidCheckInList()) {
            navigateToMain();
            return;
        }

        initViews();
        loadSavedCredentials();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etCheckInUrl = findViewById(R.id.etCheckInUrl);
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnClearUrl = findViewById(R.id.btnClearUrl);
        spinnerScanMode = findViewById(R.id.spinnerScanMode);
        spinnerGate = findViewById(R.id.spinnerGate);
        tilGate = findViewById(R.id.tilGate);

        // Setup scan mode dropdown adapter
        ArrayAdapter<String> scanModeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                SCAN_MODE_LIST
        );
        spinnerScanMode.setAdapter(scanModeAdapter);

        // Show/hide gate based on scan mode selection
        spinnerScanMode.setOnItemClickListener((parent, view, position, id) -> {
            String selected = SCAN_MODE_LIST[position];
            if ("Check-Ticket".equals(selected)) {
                tilGate.setVisibility(android.view.View.GONE);
                spinnerGate.setText("", false);
            } else {
                tilGate.setVisibility(android.view.View.VISIBLE);
            }
        });

        // Setup gate dropdown adapter
        ArrayAdapter<String> gateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                GATE_LIST
        );
        spinnerGate.setAdapter(gateAdapter);
    }

    private boolean hasValidCheckInList() {
        String checkInListId = checkInPrefs.getString("check_in_list_id", null);
        return checkInListId != null && !checkInListId.isEmpty();
    }

    private void loadSavedCredentials() {
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER, false);
        if (rememberMe) {
            String savedEmail = prefs.getString(PREF_EMAIL, "");
            String savedPassword = prefs.getString(PREF_PASSWORD, "");
            String savedCheckInUrl = prefs.getString(PREF_CHECK_IN_URL, "");
            String savedGate = prefs.getString(PREF_GATE, "");

            String savedScanMode = prefs.getString(PREF_SCAN_MODE, "");

            etUsername.setText(savedEmail);
            etPassword.setText(savedPassword);
            etCheckInUrl.setText(savedCheckInUrl);
            cbRememberMe.setChecked(true);

            // Restore base URL ke ApiClient dari URL yang tersimpan
            if (!savedCheckInUrl.isEmpty()) {
                String savedBaseUrl = extractBaseUrl(savedCheckInUrl);
                if (savedBaseUrl != null) ApiClient.setBaseUrl(savedBaseUrl);
            }

            // Restore scan mode selection
            if (!savedScanMode.isEmpty()) {
                spinnerScanMode.setText(savedScanMode, false);
                // Apply gate visibility based on restored mode
                if ("Check-Ticket".equals(savedScanMode)) {
                    tilGate.setVisibility(android.view.View.GONE);
                } else {
                    tilGate.setVisibility(android.view.View.VISIBLE);
                }
            }

            // Restore gate selection — false agar tidak trigger filter dropdown
            if (!savedGate.isEmpty()) {
                spinnerGate.setText(savedGate, false);
            }
        }
    }

    private void saveCredentials(String email, String password, String checkInUrl, String gate, String scanMode) {
        SharedPreferences.Editor editor = prefs.edit();

        if (cbRememberMe.isChecked()) {
            editor.putBoolean(PREF_REMEMBER, true);
            editor.putString(PREF_EMAIL, email);
            editor.putString(PREF_PASSWORD, password);
            editor.putString(PREF_CHECK_IN_URL, checkInUrl);
            editor.putString(PREF_GATE, gate);
            editor.putString(PREF_SCAN_MODE, scanMode);
        } else {
            editor.putBoolean(PREF_REMEMBER, false);
            editor.remove(PREF_EMAIL);
            editor.remove(PREF_PASSWORD);
            editor.remove(PREF_CHECK_IN_URL);
            editor.remove(PREF_GATE);
            editor.remove(PREF_SCAN_MODE);
        }

        editor.apply();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        if (btnClearUrl != null) {
            btnClearUrl.setOnClickListener(v -> {
                etCheckInUrl.setText("");
                etCheckInUrl.requestFocus();
            });
        }

        // Kamera QR scan
        if (btnScanQr != null) {
            btnScanQr.setOnClickListener(v -> startQrScan());
        }

        // Hardware scanner (USB/Bluetooth) — auto-submit saat dapat Enter
        if (etCheckInUrl != null) {
            etCheckInUrl.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    handleLogin();
                    return true;
                }
                return false;
            });
        }
    }

    private void handleLogin() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String checkInUrl = etCheckInUrl.getText().toString().trim();
        String selectedScanMode = spinnerScanMode.getText().toString().trim();
        String selectedGate = spinnerGate.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            etUsername.setError("Email required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }

        if (checkInUrl.isEmpty()) {
            etCheckInUrl.setError("Check-In URL required");
            return;
        }

        // Validate scan mode selection
        if (selectedScanMode.isEmpty()) {
            spinnerScanMode.setError("Pilih opsi terlebih dahulu");
            Toast.makeText(this, "Pilih opsi: Check-In atau Check-Ticket", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean validMode = false;
        for (String mode : SCAN_MODE_LIST) {
            if (mode.equals(selectedScanMode)) {
                validMode = true;
                break;
            }
        }
        if (!validMode) {
            spinnerScanMode.setError("Opsi tidak valid");
            return;
        }

        // Validate gate selection — hanya wajib untuk mode Check-In
        boolean isCheckTicketMode = "Check-Ticket".equals(selectedScanMode);
        if (!isCheckTicketMode) {
            if (selectedGate.isEmpty()) {
                spinnerGate.setError("Pilih gate terlebih dahulu");
                Toast.makeText(this, "Pilih gate terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate gate value against known list
            boolean validGate = false;
            for (String gate : GATE_LIST) {
                if (gate.equals(selectedGate)) {
                    validGate = true;
                    break;
                }
            }
            if (!validGate) {
                spinnerGate.setError("Gate tidak valid");
                return;
            }
        }

        // Extract short_id from URL
        String shortId = extractShortId(checkInUrl);
        if (shortId == null || shortId.isEmpty()) {
            etCheckInUrl.setError("Invalid Check-In URL");
            Toast.makeText(this, "Invalid Check-In URL format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save credentials if Remember Me is checked
        saveCredentials(email, password, checkInUrl, selectedGate, selectedScanMode);

        // Show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Extract base URL dari input (misal: https://dev1.pirantisolusi.com/ci/cil_xxx → https://dev1.pirantisolusi.com)
        String baseUrl = extractBaseUrl(checkInUrl);
        if (baseUrl == null) {
            etCheckInUrl.setError("Invalid Check-In URL");
            Toast.makeText(this, "Tidak dapat menentukan base URL dari input", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.setBaseUrl(baseUrl);

        // Step 1: Login with API
        performLogin(email, password, shortId, selectedGate, selectedScanMode);
    }

    private void startQrScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Arahkan kamera ke QR code Check-In URL");
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
                etCheckInUrl.setText(result.getContents());
                etCheckInUrl.setSelection(result.getContents().length());
                Toast.makeText(this, "QR berhasil discan!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String extractBaseUrl(String input) {
        if (input == null || input.isEmpty()) return null;
        input = input.trim();
        try {
            // Kalau hanya short_id (cil_xxx), tidak bisa extract base URL
            if (input.startsWith("cil_")) return null;

            // Ambil scheme + host, buang path
            // contoh: https://dev1.pirantisolusi.com/ci/cil_xxx → https://dev1.pirantisolusi.com
            int schemeEnd = input.indexOf("://");
            if (schemeEnd < 0) return null;
            String afterScheme = input.substring(schemeEnd + 3); // "dev1.pirantisolusi.com/ci/cil_xxx"
            int slashIdx = afterScheme.indexOf("/");
            String host = slashIdx >= 0 ? afterScheme.substring(0, slashIdx) : afterScheme;
            return input.substring(0, schemeEnd + 3) + host; // "https://dev1.pirantisolusi.com"
        } catch (Exception e) {
            Log.e(TAG, "Error extracting base URL", e);
            return null;
        }
    }

    private String extractShortId(String input) {
        if (input == null || input.isEmpty()) return null;
        input = input.trim();

        if (input.startsWith("cil_")) {
            return input.split("[/?#]")[0];
        }

        try {
            String path = input.contains("://") ? input.split("://", 2)[1] : input;
            for (String segment : path.split("/")) {
                String clean = segment.split("[?#]")[0];
                if (clean.startsWith("cil_")) {
                    return clean;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing URL", e);
        }

        return input;
    }

    private void performLogin(String email, String password, String shortId, String selectedGate, String selectedScanMode) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        ApiClient.getApiService().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.getToken() != null && loginResponse.getUser() != null) {
                        // Save user session (termasuk gate)
                        saveUserSession(loginResponse, selectedGate);

                        // Step 2: Validate check-in list
                        validateCheckInList(shortId, selectedGate, selectedScanMode);
                    } else {
                        resetLoginButton();
                        Toast.makeText(LoginActivity.this,
                                "Login failed - Invalid response",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    resetLoginButton();
                    String errorMsg = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                resetLoginButton();
                Toast.makeText(LoginActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Login API call failed", t);
            }
        });
    }

    private void saveUserSession(LoginResponse loginResponse, String selectedGate) {
        String gateName = loginResponse.getUser().getFullName();
        if (gateName == null || gateName.isEmpty()) {
            gateName = loginResponse.getUser().getEmail();
        }

        User user = new User(
                loginResponse.getUser().getEmail(),
                "",
                gateName
        );
        user.setToken(loginResponse.getToken());
        user.setUserId(loginResponse.getUser().getId());

        // Simpan gate yang dipilih ke User
        user.setGateName(selectedGate);

        if (loginResponse.getAccounts() != null && !loginResponse.getAccounts().isEmpty()) {
            user.setAccountId(loginResponse.getAccounts().get(0).getId());
            user.setAccountName(loginResponse.getAccounts().get(0).getName());
        }

        sessionManager.createLoginSession(user);
        Log.d(TAG, "User session saved with gate: " + selectedGate);
    }

    private void validateCheckInList(String shortId, String selectedGate, String selectedScanMode) {
        ApiService apiService = ApiClient.getApiService();

        apiService.getCheckInListDetail(shortId).enqueue(new Callback<CheckInListDetailResponse>() {
            @Override
            public void onResponse(Call<CheckInListDetailResponse> call, Response<CheckInListDetailResponse> response) {
                resetLoginButton();

                if (response.isSuccessful() && response.body() != null) {
                    CheckInListDetailResponse.CheckInListDetail detail = response.body().getData();

                    if (detail != null) {
                        if (!detail.isActive()) {
                            Toast.makeText(LoginActivity.this,
                                    "This check-in list is not active",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (detail.isExpired()) {
                            Toast.makeText(LoginActivity.this,
                                    "This check-in list has expired",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        saveCheckInListInfo(detail, selectedGate, selectedScanMode);

                        Toast.makeText(LoginActivity.this,
                                "Login berhasil! Gate: " + selectedGate + " — " + detail.getName(),
                                Toast.LENGTH_SHORT).show();

                        navigateToMain();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Invalid check-in list data",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Failed to validate check-in list";
                    if (response.code() == 404) {
                        errorMsg = "Check-in list not found";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Check-in list validation error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CheckInListDetailResponse> call, Throwable t) {
                resetLoginButton();
                Toast.makeText(LoginActivity.this,
                        "Error validating check-in list: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Check-in list API call failed", t);
            }
        });
    }

    private void saveCheckInListInfo(CheckInListDetailResponse.CheckInListDetail detail, String selectedGate, String selectedScanMode) {
        SharedPreferences.Editor editor = checkInPrefs.edit();
        editor.putString("check_in_list_id", detail.getShortId());
        editor.putString("check_in_list_name", detail.getName());
        // Simpan gate ke CheckInPrefs agar bisa diakses oleh SyncManager
        editor.putString("selected_gate", selectedGate);
        // Simpan scan mode: "Check-In" atau "Check-Ticket"
        editor.putString("scan_mode", selectedScanMode);

        if (detail.getEvent() != null) {
            editor.putString("event_name", detail.getEvent().getTitle());
        }

        editor.apply();
        Log.d(TAG, "Saved check-in list: " + detail.getShortId() + " | gate: " + selectedGate);
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}