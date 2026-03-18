package com.example.swiftsales.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.swiftsales.data.db.AppDatabase;
import com.example.swiftsales.model.CartItem;
import com.example.swiftsales.model.Order;
import com.example.swiftsales.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class OrderRepository {

    private final AppDatabase   db;
    private final SaleRepository saleRepo;   // used when fulfilling

    private static final String TABLE = "orders";

    public OrderRepository(Context context) {
        this.db       = AppDatabase.getInstance(context);
        this.saleRepo = new SaleRepository(context);
    }

    // ── Add new order ─────────────────────────────────────────────────────────

    public boolean add(Order order) {
        try {
            SQLiteDatabase wdb = db.getWritableDatabase();
            ContentValues  cv  = toContentValues(order);
            cv.put("id",         UUID.randomUUID().toString());
            cv.put("status",     Order.STATUS_PENDING);
            cv.put("created_at", now());
            return wdb.insert(TABLE, null, cv) != -1;
        } catch (Exception e) { return false; }
    }

    // ── Get all orders ────────────────────────────────────────────────────────

    public List<Order> getAll() {
        return query(null, null);
    }

    // ── Get by status ─────────────────────────────────────────────────────────

    public List<Order> getByStatus(String status) {
        return query("status = ?", new String[]{status});
    }

    // ── Get pending count (for notifications badge) ───────────────────────────

    public int getPendingCount() {
        try {
            SQLiteDatabase rdb = db.getReadableDatabase();
            Cursor c = rdb.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE + " WHERE status = ?",
                    new String[]{Order.STATUS_PENDING});
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            return count;
        } catch (Exception e) { return 0; }
    }

    // ── Update status ─────────────────────────────────────────────────────────

    public boolean updateStatus(String orderId, String newStatus) {
        try {
            SQLiteDatabase wdb = db.getWritableDatabase();
            ContentValues  cv  = new ContentValues();
            cv.put("status", newStatus);
            return wdb.update(TABLE, cv, "id = ?", new String[]{orderId}) > 0;
        } catch (Exception e) { return false; }
    }

    // ── Fulfil order → saves as Sale so it appears in Reports ─────────────────

    public boolean fulfil(Order order, double taxRate, String paymentMethod) {
        try {
            // 1. Save as a sale (feeds Reports / revenue / profit)
            boolean saleSaved = saleRepo.saveSale(order.items, taxRate, paymentMethod);
            if (!saleSaved) return false;

            // 2. Mark order as fulfilled
            return updateStatus(order.id, Order.STATUS_FULFILLED);
        } catch (Exception e) { return false; }
    }

    // ── Cancel order ──────────────────────────────────────────────────────────

    public boolean cancel(String orderId) {
        return updateStatus(orderId, Order.STATUS_CANCELLED);
    }

    // ── Delete order ──────────────────────────────────────────────────────────

    public boolean delete(String orderId) {
        try {
            return db.getWritableDatabase()
                    .delete(TABLE, "id = ?", new String[]{orderId}) > 0;
        } catch (Exception e) { return false; }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<Order> query(String selection, String[] selectionArgs) {
        List<Order> list = new ArrayList<>();
        try {
            SQLiteDatabase rdb = db.getReadableDatabase();
            Cursor c = rdb.query(TABLE, null,
                    selection, selectionArgs,
                    null, null, "created_at DESC");

            while (c.moveToNext()) {
                Order o          = new Order();
                o.id             = str(c, "id");
                o.customerName   = str(c, "customer_name");
                o.customerContact = str(c, "customer_contact");
                o.channel        = str(c, "channel");
                o.status         = str(c, "status");
                o.total          = c.getDouble(c.getColumnIndexOrThrow("total"));
                o.notes          = str(c, "notes");
                o.createdAt      = str(c, "created_at");
                o.items          = parseItems(str(c, "items_json"));
                list.add(o);
            }
            c.close();
        } catch (Exception ignored) {}
        return list;
    }

    private ContentValues toContentValues(Order o) {
        ContentValues cv = new ContentValues();
        cv.put("customer_name",    o.customerName);
        cv.put("customer_contact", o.customerContact);
        cv.put("channel",          o.channel);
        cv.put("total",            o.total);
        cv.put("notes",            o.notes);
        cv.put("items_json",       serializeItems(o.items));
        return cv;
    }

    // ── JSON serialization for CartItems ─────────────────────────────────────

    private String serializeItems(List<CartItem> items) {
        if (items == null) return "[]";
        try {
            JSONArray arr = new JSONArray();
            for (CartItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("product_id",    item.product.id);
                obj.put("product_name",  item.product.name);
                obj.put("product_emoji", item.product.emoji);
                obj.put("selling_price", item.product.sellingPrice);
                obj.put("cost_price",    item.product.costPrice);
                obj.put("quantity",      item.quantity);
                arr.put(obj);
            }
            return arr.toString();
        } catch (Exception e) { return "[]"; }
    }

    private List<CartItem> parseItems(String json) {
        List<CartItem> items = new ArrayList<>();
        if (json == null || json.isEmpty()) return items;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj  = arr.getJSONObject(i);
                Product p       = new Product();
                p.id            = obj.optString("product_id");
                p.name          = obj.optString("product_name");
                p.emoji         = obj.optString("product_emoji");
                p.sellingPrice  = obj.optDouble("selling_price");
                p.costPrice     = obj.optDouble("cost_price");
                CartItem item   = new CartItem(p, obj.optInt("quantity", 1));
                items.add(item);
            }
        } catch (Exception ignored) {}
        return items;
    }

    private String str(Cursor c, String col) {
        try { return c.getString(c.getColumnIndexOrThrow(col)); }
        catch (Exception e) { return ""; }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }
}