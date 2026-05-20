package com.example.ticketscannerapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Request body untuk endpoint POST /api/public/check-in-lists/{shortId}/check-ins
 *
 * Format yang diharapkan backend:
 * {
 *   "attendees": [
 *     { "public_id": "A-ASTUIQP", "action": "check-in" }
 *   ]
 * }
 */
public class CheckInBatchRequest {

    @SerializedName("attendees")
    private final List<AttendeeCheckIn> attendees;

    public CheckInBatchRequest(List<String> publicIds, String gate) {
        this.attendees = new ArrayList<>();
        for (String id : publicIds) {
            this.attendees.add(new AttendeeCheckIn(id));
        }
    }

    /**
     * Kirim object ini langsung sebagai @Body — Gson akan serialize menjadi:
     * { "attendees": [ { "public_id": "...", "action": "check-in" } ] }
     */
    public List<AttendeeCheckIn> getAttendees() {
        return attendees;
    }

    public static class AttendeeCheckIn {
        @SerializedName("public_id")
        private final String publicId;

        @SerializedName("action")
        private final String action = "check-in";

        public AttendeeCheckIn(String publicId) {
            this.publicId = publicId;
        }

        public String getPublicId() { return publicId; }
        public String getAction() { return action; }
    }
}