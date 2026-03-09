package com.example.ticketscannerapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect ke MainActivity
        Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}