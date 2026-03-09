package com.example.ticketscannerapp.models;

import java.util.ArrayList;
import java.util.List;

public class CheckInList {
    private String name;
    private String description;
    private List<String> productIds;
    private String activatesAt;
    private String expiresAt;
    private List<Ticket> tickets;

    public CheckInList(String name, String description) {
        this.name = name;
        this.description = description;
        this.productIds = new ArrayList<>();
        this.tickets = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getProductIds() { return productIds; }
    public void setProductIds(List<String> productIds) { this.productIds = productIds; }

    public String getActivatesAt() { return activatesAt; }
    public void setActivatesAt(String activatesAt) { this.activatesAt = activatesAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}