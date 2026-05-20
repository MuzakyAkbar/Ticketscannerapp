package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckInListDetailResponse {
    @SerializedName("data")
    private CheckInListDetail data;

    public CheckInListDetail getData() {
        return data;
    }

    public static class CheckInListDetail {
        @SerializedName("id")
        private int id;

        @SerializedName("short_id")
        private String shortId;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("expires_at")
        private String expiresAt;

        @SerializedName("activates_at")
        private String activatesAt;

        @SerializedName("total_attendees")
        private int totalAttendees;

        @SerializedName("checked_in_attendees")
        private int checkedInAttendees;

        @SerializedName("is_expired")
        private boolean isExpired;

        @SerializedName("is_active")
        private boolean isActive;

        @SerializedName("event")
        private Event event;

        @SerializedName("products")
        private List<Product> products;

        // Getters
        public int getId() { return id; }
        public String getShortId() { return shortId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getExpiresAt() { return expiresAt; }
        public String getActivatesAt() { return activatesAt; }
        public int getTotalAttendees() { return totalAttendees; }
        public int getCheckedInAttendees() { return checkedInAttendees; }
        public boolean isExpired() { return isExpired; }
        public boolean isActive() { return isActive; }
        public Event getEvent() { return event; }
        public List<Product> getProducts() { return products; }
    }

    public static class Event {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("category")
        private String category;

        @SerializedName("start_date")
        private String startDate;

        @SerializedName("end_date")
        private String endDate;

        @SerializedName("status")
        private String status;

        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getStatus() { return status; }
    }

    public static class Product {
        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("type")
        private String type;

        @SerializedName("event_id")
        private int eventId;

        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getType() { return type; }
        public int getEventId() { return eventId; }
    }
}