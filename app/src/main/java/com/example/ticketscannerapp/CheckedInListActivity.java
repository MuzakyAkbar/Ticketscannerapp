package com.example.ticketscannerapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.example.ticketscannerapp.adapters.CheckedInAdapter;
import com.example.ticketscannerapp.models.Ticket;
import com.example.ticketscannerapp.utils.DataManager;
import java.util.ArrayList;
import java.util.List;

public class CheckedInListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CheckedInAdapter adapter;
    private TextInputEditText etSearch;
    private TextView tvCount;
    private MaterialToolbar toolbar;
    private DataManager dataManager;
    private List<Ticket> allCheckedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_in_list);

        dataManager = DataManager.getInstance();

        initViews();
        setupToolbar();
        loadCheckedInTickets();
        setupSearch();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewCheckedIn);
        etSearch = findViewById(R.id.etSearch);
        tvCount = findViewById(R.id.tvCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckedInAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCheckedInTickets() {
        allCheckedIn = dataManager.getCheckedInTickets();
        adapter.setTickets(allCheckedIn);
        updateCount(allCheckedIn.size());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTickets(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTickets(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setTickets(allCheckedIn);
            updateCount(allCheckedIn.size());
            return;
        }

        List<Ticket> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Ticket ticket : allCheckedIn) {
            if (ticket.getName().toLowerCase().contains(lowerQuery) ||
                    ticket.getTicketId().toLowerCase().contains(lowerQuery)) {
                filtered.add(ticket);
            }
        }

        adapter.setTickets(filtered);
        updateCount(filtered.size());
    }

    private void updateCount(int count) {
        tvCount.setText(count + " tiket checked-in");
    }
}