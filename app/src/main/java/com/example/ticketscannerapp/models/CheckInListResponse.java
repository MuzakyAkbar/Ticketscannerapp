package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckInListResponse {
    @SerializedName("data")
    private List<CheckInListData> data;

    @SerializedName("meta")
    private Meta meta;

    public List<CheckInListData> getData() {
        return data;
    }

    public Meta getMeta() {
        return meta;
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

        @SerializedName("is_active")
        private boolean isActive;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getActivatesAt() {
            return activatesAt;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public boolean isActive() {
            return isActive;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

    public static class Meta {
        @SerializedName("current_page")
        private int currentPage;

        @SerializedName("per_page")
        private int perPage;

        @SerializedName("total")
        private int total;

        @SerializedName("last_page")
        private int lastPage;

        public int getCurrentPage() {
            return currentPage;
        }

        public int getPerPage() {
            return perPage;
        }

        public int getTotal() {
            return total;
        }

        public int getLastPage() {
            return lastPage;
        }
    }
}