package com.example.swiftsales.model;

public class Staff {

    public String  id;
    public String  name;
    public String  phone;
    public String  role;      // "Cashier", "Manager", "Supervisor"
    public String  pin;       // 4-digit PIN for POS login
    public String  email;
    public boolean isActive;
    public String  joinDate;

    public Staff() {}

    public Staff(String id, String name, String phone, String role,
                 String pin, String email, boolean isActive, String joinDate) {
        this.id       = id;
        this.name     = name;
        this.phone    = phone;
        this.role     = role;
        this.pin      = pin;
        this.email    = email;
        this.isActive = isActive;
        this.joinDate = joinDate;
    }

    // ── null-safe: name could be null if read from an old DB row ─────────────
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // ── null-safe role emoji ──────────────────────────────────────────────────
    public String getRoleEmoji() {
        if (role == null) return "🧑‍💻";
        switch (role) {
            case "Manager":    return "👔";
            case "Supervisor": return "🧑‍💼";
            default:           return "🧑‍💻";
        }
    }
}