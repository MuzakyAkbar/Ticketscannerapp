package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;

public class CheckInResponse {
    @SerializedName("data")
    private AttendeesResponse.AttendeeData data;

    @SerializedName("message")
    private String message;

    public AttendeesResponse.AttendeeData getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}