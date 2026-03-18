package com.example.swiftsales.ui.pos;

import com.example.swiftsales.model.CartItem;
import java.util.List;

// Simple static holder to pass cart between POS and Checkout
public class CartHolder {
    public static List<CartItem> cart;
    public static String         currency = "RWF";
}