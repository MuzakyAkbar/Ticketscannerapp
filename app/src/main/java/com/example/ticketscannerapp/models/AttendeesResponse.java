package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendeesResponse {
    @SerializedName("data")
    private List<AttendeeData> data;

    public List<AttendeeData> getData() {
        return data;
    }

    public static class AttendeeData {
        @SerializedName("id")
        private String id;

        @SerializedName("ticket_id")
        private String ticketId;

        @SerializedName("attendee_name")
        private String attendeeName;

        @SerializedName("attendee_email")
        private String attendeeEmail;

        @SerializedName("product_name")
        private String productName;

        @SerializedName("product_price")
        private double productPrice;

        @SerializedName("status")
        private String status;

        @SerializedName("checked_in_at")
        private String checkedInAt;

        public String getId() { return id; }
        public String getTicketId() { return ticketId; }
        public String getAttendeeName() { return attendeeName; }
        public String getAttendeeEmail() { return attendeeEmail; }
        public String getProductName() { return productName; }
        public double getProductPrice() { return productPrice; }
        public String getStatus() { return status; }
        public String getCheckedInAt() { return checkedInAt; }
    }
}