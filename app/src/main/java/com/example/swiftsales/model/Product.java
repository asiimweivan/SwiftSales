package com.example.swiftsales.model;

public class Product {

    public String id;
    public String name;
    public String category;
    public String emoji;
    public double sellingPrice;
    public double costPrice;
    public int    stock;
    public int    lowStockAlert;
    public String barcode;

    public Product() {}

    public Product(String id, String name, String category, String emoji,
                   double sellingPrice, double costPrice,
                   int stock, int lowStockAlert, String barcode) {
        this.id            = id;
        this.name          = name;
        this.category      = category;
        this.emoji         = emoji;
        this.sellingPrice  = sellingPrice;
        this.costPrice     = costPrice;
        this.stock         = stock;
        this.lowStockAlert = lowStockAlert;
        this.barcode       = barcode;
    }

    // ── Helpers ──
    public boolean isLowStock()    { return stock > 0 && stock <= lowStockAlert; }
    public boolean isOutOfStock()  { return stock <= 0; }

    public String formattedPrice(String currency) {
        return currency + " " + String.format("%.0f", sellingPrice);
    }
}