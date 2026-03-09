package com.example.ticketscannerapp.api;

import com.example.ticketscannerapp.models.AttendeesResponse;
import com.example.ticketscannerapp.models.CheckInRequest;
import com.example.ticketscannerapp.models.CheckInResponse;
import com.example.ticketscannerapp.models.LoginRequest;
import com.example.ticketscannerapp.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/public/check-in-lists/{listId}/attendees")
    Call<AttendeesResponse> getAttendees(
            @Path("listId") String listId,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("filter_fields[status][eq]") String status,
            @Header("Cookie") String token
    );

    @PATCH("api/public/check-in-lists/{listId}/attendees/{attendeeId}")
    Call<CheckInResponse> checkInAttendee(
            @Path("listId") String listId,
            @Path("attendeeId") String attendeeId,
            @Body CheckInRequest request,
            @Header("Cookie") String token
    );
}