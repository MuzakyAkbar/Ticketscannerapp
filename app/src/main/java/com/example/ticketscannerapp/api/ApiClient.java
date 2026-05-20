package com.example.ticketscannerapp.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://dev1.pirantisolusi.com/";
    private static final String BASE_ORIGIN = "https://dev1.pirantisolusi.com";
    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor: tambahkan header agar request Android dikenali
            // sama seperti request dari browser (frontend React).
            //
            // Backend memvalidasi kombinasi:
            //   - Accept: application/json      → return JSON, bukan redirect HTML
            //   - X-Requested-With: XMLHttpRequest → ditandai sebagai AJAX/API
            //   - Origin + Referer              → dipakai CORS & middleware backend
            Interceptor apiHeaderInterceptor = chain -> {
                Request original = chain.request();

                // Bangun Referer dari path request yang sedang dikirim
                String referer = BASE_ORIGIN + original.url().encodedPath();

                Request request = original.newBuilder()
                        .header("Accept", "application/json, text/plain, */*")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Origin", BASE_ORIGIN)
                        .header("Referer", referer)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            };

            // Interceptor: log response tidak terduga
            Interceptor responseCheckInterceptor = chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);

                String contentType = response.header("Content-Type", "");
                int statusCode = response.code();

                if (contentType != null && contentType.contains("text/html")) {
                    Log.e(TAG, "Unexpected HTML response for: " + request.url()
                            + " | Status: " + statusCode
                            + " | Location: " + response.header("Location", "-"));
                }

                if (statusCode == 302 && request.method().equals("POST")) {
                    Log.e(TAG, "POST 302 redirect. URL: " + request.url()
                            + " | Location: " + response.header("Location", "-"));
                }

                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(apiHeaderInterceptor)       // ← header Accept, Origin, Referer
                    .addInterceptor(interceptor)                // ← logging
                    .addInterceptor(responseCheckInterceptor)  // ← log response aneh
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .serializeNulls()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}