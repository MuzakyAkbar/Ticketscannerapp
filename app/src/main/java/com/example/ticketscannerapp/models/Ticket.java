package com.example.ticketscannerapp.models;

public class Ticket {
    private String ticketId;
    private String attendeeId;
    private String name;
    private String email;
    private String type;
    private double price;
    private String event;
    private String date;
    private boolean checkedIn;
    private String checkInTime;

    public Ticket(String ticketId, String name, String email, String type,
                  double price, String event, String date) {
        this.ticketId = ticketId;
        this.name = name;
        this.email = email;
        this.type = type;
        this.price = price;
        this.event = event;
        this.date = date;
        this.checkedIn = false;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
}