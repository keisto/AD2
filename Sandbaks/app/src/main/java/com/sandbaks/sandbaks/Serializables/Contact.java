package com.sandbaks.sandbaks.Serializables;

import java.io.Serializable;

public class Contact implements Serializable {
    private static final long serialVersionUID = 1234567890L;
    public static String CONTACT = "com.sandbaks.sandbaks.CONTACT.CONTACT";
    public static String NAME    = "com.sandbaks.sandbaks.CONTACT.NAME";
    public static String PHONE   = "com.sandbaks.sandbaks.CONTACT.PHONE";
    public static String EMAIL   = "com.sandbaks.sandbaks.CONTACT.EMAIL";
    public static String ACCESS  = "com.sandbaks.sandbaks.CONTACT.ACCESS";

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
