package com.example.HUTECHBUS.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tickets")
public class Ticket {
    
    @Id
    private String id; // format: HT123456
    private String username;
    private String route;
    private String date;
    private String time;
    private String seats;
    private Double total;
    private String status; // 'pending', 'success', 'failed'
    
    public Ticket() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
