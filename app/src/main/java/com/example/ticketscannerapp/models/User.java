package com.example.ticketscannerapp.models;

public class User {
    private String username;
    private String password;
    private String gate;
    private String token;
    private String role;
    private int userId;
    private int accountId;
    private String accountName;

    // Gate yang dipilih saat login (Gate 1 - Gate 10, VIP Utara, VIP Selatan, VVIP)
    private String gateName;

    public User(String username, String password, String gate) {
        this.username = username;
        this.password = password;
        this.gate = gate;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    /**
     * Gate yang dipilih operator saat login (misal: "Gate 1", "VIP Utara", "VVIP").
     * Nilai ini akan dikirim ke server saat melakukan check-in batch.
     */
    public String getGateName() { return gateName; }
    public void setGateName(String gateName) { this.gateName = gateName; }
}