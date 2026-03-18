package com.example.swiftsales.model;

import java.util.List;

/**
 * Represents a customer order received via any channel.
 * When status moves to FULFILLED it is saved as a Sale in the sales table.
 */
public class Order {

    // ── Status constants ──────────────────────────────────────────────────────
    public static final String STATUS_PENDING   = "Pending";
    public static final String STATUS_FULFILLED = "Fulfilled";
    public static final String STATUS_CANCELLED = "Cancelled";

    // ── Channel constants ─────────────────────────────────────────────────────
    public static final String CHANNEL_PHONE    = "Phone";
    public static final String CHANNEL_WHATSAPP = "WhatsApp";
    public static final String CHANNEL_DELIVERY = "Delivery Platform";
    public static final String CHANNEL_WALKIN   = "Walk-in";
    public static final String CHANNEL_OTHER    = "Other";

    // ── Fields ────────────────────────────────────────────────────────────────
    public String         id;
    public String         customerName;
    public String         customerContact;
    public String         channel;
    public String         status;
    public double         total;
    public String         createdAt;
    public String         notes;
    public List<CartItem> items;

    public Order() {}

    // ── Channel emoji ─────────────────────────────────────────────────────────
    public String getChannelEmoji() {
        if (channel == null) return "📋";
        switch (channel) {
            case CHANNEL_PHONE:    return "📞";
            case CHANNEL_WHATSAPP: return "💬";
            case CHANNEL_DELIVERY: return "🚚";
            case CHANNEL_WALKIN:   return "🚶";
            default:               return "📋";
        }
    }

    // ── Status badge drawable — was wrongly checking channel, now checks status
    public String getStatusBadgeDrawable() {
        if (status == null) return "badge_warning";
        switch (status) {
            case STATUS_FULFILLED: return "badge_success";
            case STATUS_CANCELLED: return "badge_danger";
            default:               return "badge_warning";  // Pending
        }
    }
}