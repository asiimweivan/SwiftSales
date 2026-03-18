package com.example.swiftsales.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.swiftsales.data.db.AppDatabase;
import com.example.swiftsales.model.CartItem;
import com.example.swiftsales.model.Sale;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * SaleRepository — all DB operations for sales.
 * Never calls db.close() — singleton safety.
 */
public class SaleRepository {

    private final AppDatabase db;

    private static final String TABLE = AppDatabase.TABLE_SALES;

    public SaleRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // ── SAVE SALE ─────────────────────────────────────────────────────────────

    public boolean saveSale(List<CartItem> items, double taxRate, String paymentMethod) {
        if (items == null || items.isEmpty()) return false;

        double subtotal = 0;
        double profit   = 0;
        for (CartItem item : items) {
            subtotal += item.getLineTotal();
            profit   += (item.product.sellingPrice - item.product.costPrice) * item.quantity;
        }
        double tax   = subtotal * (taxRate / 100);
        double total = subtotal + tax;

        ContentValues cv = new ContentValues();
        cv.put("id",             UUID.randomUUID().toString());
        cv.put("date",           now());
        cv.put("items_json",     serializeItems(items));
        cv.put("total",          total);
        cv.put("tax",            tax);
        cv.put("profit",         profit);
        cv.put("payment_method", paymentMethod != null ? paymentMethod : "Cash");

        try {
            SQLiteDatabase wdb = db.getWritableDatabase();
            boolean ok = wdb.insert(TABLE, null, cv) != -1;

            // Decrement stock for each sold item directly (no circular dependency)
            if (ok) {
                for (CartItem item : items) {
                    wdb.execSQL(
                            "UPDATE products SET stock = MAX(0, stock - ?) WHERE id = ?",
                            new Object[]{item.quantity, item.product.id});
                }
            }
            return ok;
        } catch (Exception e) { return false; }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Today's total revenue */
    public double getTodayTotal() {
        return singleDouble(
                "SELECT COALESCE(SUM(total), 0) FROM " + TABLE
                        + " WHERE date(date) = date('now', 'localtime')");
    }

    /** Today's order count */
    public int getTodayOrderCount() {
        return singleInt(
                "SELECT COUNT(*) FROM " + TABLE
                        + " WHERE date(date) = date('now', 'localtime')");
    }

    /** Today's profit */
    public double getTodayProfit() {
        return singleDouble(
                "SELECT COALESCE(SUM(profit), 0) FROM " + TABLE
                        + " WHERE date(date) = date('now', 'localtime')");
    }

    /** Revenue for a given period: Today / Week / Month / All */
    public double getRevenue(String period) {
        return singleDouble("SELECT COALESCE(SUM(total), 0) FROM "
                + TABLE + periodWhere(period));
    }

    /** Profit for a given period */
    public double getProfit(String period) {
        return singleDouble("SELECT COALESCE(SUM(profit), 0) FROM "
                + TABLE + periodWhere(period));
    }

    /** Order count for a given period */
    public int getOrderCount(String period) {
        return singleInt("SELECT COUNT(*) FROM " + TABLE + periodWhere(period));
    }

    /** Count of a specific payment method for a given period */
    public int getPaymentCount(String period, String paymentMethod) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE
                        + periodWhere(period)
                        + " AND payment_method = ?",
                new String[]{paymentMethod});
        int val = 0;
        if (c.moveToFirst()) val = c.getInt(0);
        c.close();
        return val;
    }

    /** All sales for a given period, newest first */
    public List<Sale> getSales(String period) {
        return querySales("SELECT * FROM " + TABLE
                + periodWhere(period) + " ORDER BY date DESC");
    }

    /**
     * Most recent N sales — used by Dashboard recent sales list.
     * Returns up to [limit] sales ordered newest first.
     */
    public List<Sale> getRecentSales(int limit) {
        return querySales("SELECT * FROM " + TABLE
                + " ORDER BY date DESC LIMIT " + limit);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private String periodWhere(String period) {
        switch (period != null ? period : "Today") {
            case "Week":  return " WHERE date(date) >= date('now', '-6 days', 'localtime')";
            case "Month": return " WHERE date(date) >= date('now', 'start of month', 'localtime')";
            case "All":   return "";
            default:      return " WHERE date(date) = date('now', 'localtime')"; // Today
        }
    }

    private List<Sale> querySales(String sql) {
        List<Sale> list = new ArrayList<>();
        try {
            SQLiteDatabase rdb = db.getReadableDatabase();
            Cursor c = rdb.rawQuery(sql, null);
            while (c.moveToNext()) {
                Sale s          = new Sale();
                s.id            = str(c, "id");
                s.date          = str(c, "date");
                s.total         = c.getDouble(c.getColumnIndexOrThrow("total"));
                s.tax           = c.getDouble(c.getColumnIndexOrThrow("tax"));
                s.profit        = c.getDouble(c.getColumnIndexOrThrow("profit"));
                s.paymentMethod = str(c, "payment_method");
                list.add(s);
            }
            c.close();
        } catch (Exception ignored) {}
        return list;
    }

    private int singleInt(String sql) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(sql, null);
        int val = 0;
        if (c.moveToFirst()) val = c.getInt(0);
        c.close();
        return val;
    }

    private double singleDouble(String sql) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(sql, null);
        double val = 0;
        if (c.moveToFirst()) val = c.getDouble(0);
        c.close();
        return val;
    }

    private String str(Cursor c, String col) {
        try { return c.getString(c.getColumnIndexOrThrow(col)); }
        catch (Exception e) { return ""; }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private String serializeItems(List<CartItem> items) {
        try {
            JSONArray arr = new JSONArray();
            for (CartItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("product_id",    item.product.id);
                obj.put("product_name",  item.product.name);
                obj.put("selling_price", item.product.sellingPrice);
                obj.put("cost_price",    item.product.costPrice);
                obj.put("quantity",      item.quantity);
                arr.put(obj);
            }
            return arr.toString();
        } catch (Exception e) { return "[]"; }
    }
}