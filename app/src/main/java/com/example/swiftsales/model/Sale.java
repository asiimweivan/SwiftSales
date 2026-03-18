package com.example.swiftsales.model;

import java.util.List;

public class Sale {

    public String         id;
    public String         date;
    public List<CartItem> items;
    public double         subtotal;
    public double         tax;
    public double         profit;        // ← ADDED
    public double         total;
    public String         paymentMethod;
    public String         cashierId;

    public Sale() {}

    public Sale(String id, String date, List<CartItem> items,
                double subtotal, double tax, double profit, double total,
                String paymentMethod, String cashierId) {
        this.id            = id;
        this.date          = date;
        this.items         = items;
        this.subtotal      = subtotal;
        this.tax           = tax;
        this.profit        = profit;
        this.total         = total;
        this.paymentMethod = paymentMethod;
        this.cashierId     = cashierId;
    }
}