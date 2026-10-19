package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class CheckInBatchRequest {

    @SerializedName("attendees")
    private final List<AttendeeCheckIn> attendees;

    public CheckInBatchRequest(List<String> publicIds, String gate) {
        this.attendees = new ArrayList<>();
        String normalizedGate = (gate != null && !gate.isEmpty() && !gate.equals("Unknown")) ? gate : null;
        for (String id : publicIds) {
            this.attendees.add(new AttendeeCheckIn(id, normalizedGate));
        }
    }

    public List<AttendeeCheckIn> getAttendees() {
        return attendees;
    }

    public static class AttendeeCheckIn {
        @SerializedName("public_id")
        private final String publicId;

        @SerializedName("action")
        private final String action = "check-in";

        @SerializedName("gate")
        private final String gate;

        public AttendeeCheckIn(String publicId, String gate) {
            this.publicId = publicId;
            this.gate = gate;
        }

        public String getPublicId() { return publicId; }
        public String getAction() { return action; }
        public String getGate() { return gate; }
    }
}