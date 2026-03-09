package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {
    @SerializedName("status")
    private String status;

    public CheckInRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}