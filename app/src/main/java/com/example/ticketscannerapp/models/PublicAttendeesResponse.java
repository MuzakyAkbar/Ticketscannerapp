package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PublicAttendeesResponse {

    @SerializedName("data")
    private List<AttendeeData> data;

    @SerializedName("links")
    private Links links;

    @SerializedName("meta")
    private Meta meta;

    public List<AttendeeData> getData() { return data; }
    public Links getLinks() { return links; }
    public Meta getMeta() { return meta; }

    public boolean hasNextPage() {
        return links != null && links.getNext() != null && !links.getNext().isEmpty();
    }

    // ─── Attendee ────────────────────────────────────────────────────────────

    public static class AttendeeData {
        @SerializedName("id")
        private int id;

        @SerializedName("email")
        private String email;

        @SerializedName("first_name")
        private String firstName;

        @SerializedName("last_name")
        private String lastName;

        @SerializedName("public_id")
        private String publicId;

        @SerializedName("product_id")
        private int productId;

        @SerializedName("product_price_id")
        private int productPriceId;

        @SerializedName("status")
        private String status;

        @SerializedName("locale")
        private String locale;

        @SerializedName("order_id")
        private int orderId;

        @SerializedName("check_in")
        private CheckIn checkIn;  // null = belum check-in

        // Getters
        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPublicId() { return publicId; }
        public int getProductId() { return productId; }
        public int getProductPriceId() { return productPriceId; }
        public String getStatus() { return status; }
        public String getLocale() { return locale; }
        public int getOrderId() { return orderId; }
        public CheckIn getCheckIn() { return checkIn; }

        public String getFullName() {
            String fn = firstName != null ? firstName : "";
            String ln = lastName != null ? lastName : "";
            return (fn + " " + ln).trim();
        }

        public boolean isCheckedIn() {
            return checkIn != null;
        }
    }

    // ─── CheckIn object (null when not checked in) ────────────────────────────

    public static class CheckIn {
        @SerializedName("id")
        private int id;

        @SerializedName("short_id")
        private String shortId;

        @SerializedName("check_in_list_id")
        private int checkInListId;

        @SerializedName("attendee_id")
        private int attendeeId;

        @SerializedName("checked_in_at")
        private String checkedInAt;

        @SerializedName("order_id")
        private int orderId;

        public int getId() { return id; }
        public String getShortId() { return shortId; }
        public int getCheckInListId() { return checkInListId; }
        public int getAttendeeId() { return attendeeId; }
        public String getCheckedInAt() { return checkedInAt; }
        public int getOrderId() { return orderId; }
    }

    // ─── Links ────────────────────────────────────────────────────────────────

    public static class Links {
        @SerializedName("first")
        private String first;

        @SerializedName("last")
        private String last;

        @SerializedName("prev")
        private String prev;

        @SerializedName("next")
        private String next;

        public String getFirst() { return first; }
        public String getLast() { return last; }
        public String getPrev() { return prev; }
        public String getNext() { return next; }
    }

    // ─── Meta ─────────────────────────────────────────────────────────────────

    public static class Meta {
        @SerializedName("current_page")
        private int currentPage;

        @SerializedName("current_page_url")
        private String currentPageUrl;

        @SerializedName("from")
        private int from;

        @SerializedName("path")
        private String path;

        @SerializedName("per_page")
        private int perPage;

        @SerializedName("to")
        private int to;

        public int getCurrentPage() { return currentPage; }
        public String getCurrentPageUrl() { return currentPageUrl; }
        public int getFrom() { return from; }
        public String getPath() { return path; }
        public int getPerPage() { return perPage; }
        public int getTo() { return to; }
    }
}