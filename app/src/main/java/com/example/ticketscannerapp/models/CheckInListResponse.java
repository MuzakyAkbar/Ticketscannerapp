package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;

public class CheckInListResponse {
    @SerializedName("data")
    private CheckInListData data;

    public CheckInListData getData() {
        return data;
    }

    public static class CheckInListData {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("activates_at")
        private String activatesAt;

        @SerializedName("expires_at")
        private String expiresAt;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getActivatesAt() { return activatesAt; }
        public String getExpiresAt() { return expiresAt; }
    }
}