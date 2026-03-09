package com.example.ticketscannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketscannerapp.api.ApiClient;
import com.example.ticketscannerapp.models.LoginRequest;
import com.example.ticketscannerapp.models.LoginResponse;
import com.example.ticketscannerapp.models.User;
import com.example.ticketscannerapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREF_REMEMBER = "RememberMe";
    private static final String PREF_EMAIL = "SavedEmail";
    private static final String PREF_PASSWORD = "SavedPassword";

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private CheckBox cbRememberMe;
    private SessionManager sessionManager;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
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
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
    }

    private void loadSavedCredentials() {
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER, false);
        if (rememberMe) {
            String savedEmail = prefs.getString(PREF_EMAIL, "");
            String savedPassword = prefs.getString(PREF_PASSWORD, "");

            etUsername.setText(savedEmail);
            etPassword.setText(savedPassword);
            cbRememberMe.setChecked(true);
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = prefs.edit();

        if (cbRememberMe.isChecked()) {
            editor.putBoolean(PREF_REMEMBER, true);
            editor.putString(PREF_EMAIL, email);
            editor.putString(PREF_PASSWORD, password);
        } else {
            editor.putBoolean(PREF_REMEMBER, false);
            editor.remove(PREF_EMAIL);
            editor.remove(PREF_PASSWORD);
        }

        editor.apply();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etUsername.setError("Email required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }

        // Save credentials if Remember Me is checked
        saveCredentials(email, password);

        // Show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Make API call
        ApiClient.getApiService().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.getToken() != null && loginResponse.getUser() != null) {
                        // Create user object
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

                        // Set account info if available
                        if (loginResponse.getAccounts() != null && !loginResponse.getAccounts().isEmpty()) {
                            user.setAccountId(loginResponse.getAccounts().get(0).getId());
                            user.setAccountName(loginResponse.getAccounts().get(0).getName());
                        }

                        // Save session
                        sessionManager.createLoginSession(user);

                        Toast.makeText(LoginActivity.this,
                                "Login successful! Welcome " + user.getGate(),
                                Toast.LENGTH_SHORT).show();

                        navigateToMain();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed - Invalid response",
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Token or user is null");
                    }
                } else {
                    String errorMsg = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                Toast.makeText(LoginActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call failed", t);
            }
        });
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