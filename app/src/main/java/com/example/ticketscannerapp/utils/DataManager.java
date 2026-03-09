package com.example.ticketscannerapp.utils;

import android.util.Log;

import com.example.ticketscannerapp.api.ApiClient;
import com.example.ticketscannerapp.models.AttendeesResponse;
import com.example.ticketscannerapp.models.CheckInRequest;
import com.example.ticketscannerapp.models.CheckInResponse;
import com.example.ticketscannerapp.models.Ticket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataManager {
    private static final String TAG = "DataManager";
    private static DataManager instance;
    private final Map<String, Ticket> ticketDatabase;
    private String checkInListId = "cil_e2CqWkS3Tvyso";
    private String authToken;

    public interface DataLoadCallback {
        void onSuccess(List<Ticket> tickets);
        void onError(String message);
    }

    public interface CheckInCallback {
        void onSuccess(Ticket ticket);
        void onError(String message);
    }

    private DataManager() {
        ticketDatabase = new HashMap<>();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void setAuthToken(String token) {
        this.authToken = "token=" + token;
    }

    public void setCheckInListId(String listId) {
        this.checkInListId = listId;
    }

    // Load attendees dari API
    public void loadAttendeesFromAPI(final DataLoadCallback callback) {
        if (authToken == null) {
            callback.onError("Token tidak tersedia");
            return;
        }

        ApiClient.getApiService().getAttendees(
                checkInListId,
                1,
                150,
                "ACTIVE",
                authToken
        ).enqueue(new Callback<AttendeesResponse>() {
            @Override
            public void onResponse(Call<AttendeesResponse> call, Response<AttendeesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ticket> tickets = new ArrayList<>();
                    for (AttendeesResponse.AttendeeData attendee : response.body().getData()) {
                        Ticket ticket = new Ticket(
                                attendee.getTicketId(),
                                attendee.getAttendeeName(),
                                attendee.getAttendeeEmail(),
                                attendee.getProductName(),
                                attendee.getProductPrice(),
                                "Persita vs Persija",
                                "Jan 27, 2026 9:00pm"
                        );
                        ticket.setAttendeeId(attendee.getId());
                        ticket.setCheckedIn("CHECKED_IN".equals(attendee.getStatus()));
                        ticket.setCheckInTime(attendee.getCheckedInAt());

                        addTicketToDatabase(ticket);
                        tickets.add(ticket);
                    }
                    callback.onSuccess(tickets);
                } else {
                    callback.onError("Gagal memuat data: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AttendeesResponse> call, Throwable t) {
                Log.e(TAG, "Error loading attendees", t);
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    // Check-in via API
    public void checkInTicketAPI(final String ticketId, final CheckInCallback callback) {
        Ticket ticket = getTicket(ticketId);

        if (ticket == null) {
            callback.onError("Tiket tidak ditemukan");
            return;
        }

        if (ticket.isCheckedIn()) {
            callback.onError("Tiket sudah check-in");
            return;
        }

        if (authToken == null) {
            callback.onError("Token tidak tersedia");
            return;
        }

        if (ticket.getAttendeeId() == null) {
            callback.onError("Attendee ID tidak valid");
            return;
        }

        CheckInRequest request = new CheckInRequest("CHECKED_IN");

        ApiClient.getApiService().checkInAttendee(
                checkInListId,
                ticket.getAttendeeId(),
                request,
                authToken
        ).enqueue(new Callback<CheckInResponse>() {
            @Override
            public void onResponse(Call<CheckInResponse> call, Response<CheckInResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ticket.setCheckedIn(true);
                    ticket.setCheckInTime(getCurrentTime());
                    addTicketToDatabase(ticket);
                    callback.onSuccess(ticket);
                } else {
                    callback.onError("Gagal check-in: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CheckInResponse> call, Throwable t) {
                Log.e(TAG, "Error checking in", t);
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public synchronized void addTicketToDatabase(Ticket ticket) {
        if (ticket != null && ticket.getTicketId() != null) {
            ticketDatabase.put(normalizeTicketId(ticket.getTicketId()), ticket);
        }
    }

    public synchronized Ticket getTicket(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) return null;
        return ticketDatabase.get(normalizeTicketId(ticketId));
    }

    public String normalizeTicketId(String ticketId) {
        if (ticketId == null) return "";
        return ticketId.toUpperCase()
                .replace("-", "")
                .replace(" ", "")
                .replace("_", "")
                .trim();
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public synchronized int getCheckedInCount() {
        int count = 0;
        for (Ticket ticket : ticketDatabase.values()) {
            if (ticket.isCheckedIn()) count++;
        }
        return count;
    }

    public int getTotalTickets() {
        return ticketDatabase.size();
    }

    public synchronized List<Ticket> getCheckedInTickets() {
        List<Ticket> checkedIn = new ArrayList<>();
        for (Ticket ticket : ticketDatabase.values()) {
            if (ticket.isCheckedIn()) {
                checkedIn.add(ticket);
            }
        }

        Collections.sort(checkedIn, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket t1, Ticket t2) {
                if (t1.getCheckInTime() == null) return 1;
                if (t2.getCheckInTime() == null) return -1;
                return t2.getCheckInTime().compareTo(t1.getCheckInTime());
            }
        });

        return checkedIn;
    }

    public synchronized List<Ticket> getRecentCheckedInTickets(int limit) {
        List<Ticket> checkedIn = getCheckedInTickets();
        return checkedIn.size() > limit ? checkedIn.subList(0, limit) : checkedIn;
    }
}