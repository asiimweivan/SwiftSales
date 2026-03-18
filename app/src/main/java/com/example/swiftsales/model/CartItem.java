package com.example.swiftsales.model;

public class CartItem {

    public Product product;
    public int     quantity;

    public CartItem(Product product, int quantity) {
        this.product  = product;
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return product.sellingPrice * quantity;
    }

    public String formattedTotal(String currency) {
        return currency + " " + String.format("%.0f", getLineTotal());
    }
}