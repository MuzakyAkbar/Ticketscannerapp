package com.example.ticketscannerapp.api;

import com.example.ticketscannerapp.models.AttendeesResponse;
import com.example.ticketscannerapp.models.CheckInBatchRequest;
import com.example.ticketscannerapp.models.CheckInListDetailResponse;
import com.example.ticketscannerapp.models.CheckInListResponse;
import com.example.ticketscannerapp.models.CheckInRequest;
import com.example.ticketscannerapp.models.CheckInResponse;
import com.example.ticketscannerapp.models.LoginRequest;
import com.example.ticketscannerapp.models.LoginResponse;
import com.example.ticketscannerapp.models.PublicAttendeesResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ========== Authentication ==========

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // ========== Old Endpoints (backward compatibility) ==========

    @GET("api/events/{eventId}/check-in-lists")
    Call<CheckInListResponse> getCheckInLists(
            @Path("eventId") String eventId,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("query") String query,
            @Query("sort_by") String sortBy,
            @Query("sort_direction") String sortDirection,
            @Header("Cookie") String cookie
    );

    @GET("api/public/check-in-lists/{listId}/attendees")
    Call<AttendeesResponse> getAttendees(
            @Path("listId") String listId,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("filter_fields[status][eq]") String status,
            @Header("Cookie") String cookie
    );

    @PATCH("api/public/check-in-lists/{listId}/attendees/{attendeeId}")
    Call<CheckInResponse> checkInAttendee(
            @Path("listId") String listId,
            @Path("attendeeId") String attendeeId,
            @Body CheckInRequest request,
            @Header("Cookie") String cookie
    );

    // ========== Public Check-In List Endpoints ==========

    @GET("api/public/check-in-lists/{shortId}")
    Call<CheckInListDetailResponse> getCheckInListDetail(
            @Path("shortId") String shortId
    );

    @GET("api/public/check-in-lists/{shortId}/attendees")
    Call<PublicAttendeesResponse> getPublicAttendees(
            @Path("shortId") String shortId,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("query") String query,
            @Query("sort_by") String sortBy,
            @Query("sort_direction") String sortDirection,
            @Query("filter_fields[status][eq]") String status
    );

    // Batch check-in — body dikirim sebagai object { "attendees": [...] }
    @POST("api/public/check-in-lists/{shortId}/check-ins")
    Call<ResponseBody> batchCheckIn(
            @Path("shortId") String shortId,
            @Body CheckInBatchRequest request,   // ← kirim full object, bukan List
            @Header("Cookie") String cookie
    );

    // Check-out — token via Cookie
    @DELETE("api/public/check-in-lists/{shortId}/check-ins/{checkInId}")
    Call<ResponseBody> checkOut(
            @Path("shortId") String shortId,
            @Path("checkInId") String checkInId,
            @Header("Cookie") String cookie
    );
}