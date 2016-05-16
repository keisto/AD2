package com.sandbaks.sandbaks;

import java.io.Serializable;

/**
 * Created by buNny on 5/15/16.
 */
public class Contact implements Serializable {
    private static final long serialVersionUID = 1234567890L;

    // Variables
    private String name;
    private String phone;
    private String email;
    private int access;


    // Getters & Setters
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getAccess() { return access; }

    // Create Person
    public Contact(String _name, String _phone, String _email, int _access) {
        this.name = _name;
        this.phone = _phone;
        this.email = _email;
        this.access = _access;
    }
}
