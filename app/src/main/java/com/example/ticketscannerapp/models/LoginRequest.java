package com.example.ticketscannerapp.models;

public class LoginRequest {
    private String email;
    private String password;
    private String account_id;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.account_id = "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }
}