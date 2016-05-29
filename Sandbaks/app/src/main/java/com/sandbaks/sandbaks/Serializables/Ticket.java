package com.sandbaks.sandbaks.Serializables;

import java.io.Serializable;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 987654321L;

    // Variables
    private int id;
    private String company;
    private String attention;
    private String location;
    private String supervisor;
    private String description;
    private String created;
    private String start;
    private String end;
    private String afe;
    private String job;
    private String work;
    private float hours;
    private int status;

    // Create Ticket
    public Ticket(int id, String company, String attention, String location, String supervisor,
                  String description, String created, String start, String end, String afe,
                  String job, String work, float hours, int status) {
        this.id = id;
        this.company = company;
        this.attention = attention;
        this.location = location;
        this.supervisor = supervisor;
        this.description = description;
        this.created = created;
        this.start = start;
        this.end = end;
        this.afe = afe;
        this.job = job;
        this.work = work;
        this.hours = hours;
        this.status = status;
    }

    // Getters
    public String getCompany()     { return company; }
    public String getAttention()   { return attention; }
    public String getLocation()    { return location; }
    public String getSupervisor()  { return supervisor; }
    public String getDescription() { return description; }
    public String getCreated()     { return created; }
    public String getStart()       { return start; }
    public String getEnd()         { return end; }
    public String getWork()        { return work; }
    public String getAfe()         { return afe; }
    public String getJob()         { return job; }
    public float  getHours()       { return hours; }
    public int    getStatus()      { return status; }
    public int    getId()          { return id; }
}
