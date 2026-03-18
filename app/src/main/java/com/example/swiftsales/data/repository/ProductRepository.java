package com.example.swiftsales.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.swiftsales.data.db.AppDatabase;
import com.example.swiftsales.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ProductRepository — all DB operations for products.
 *
 * IMPORTANT: We never call db.close() on a singleton SQLiteOpenHelper.
 * Closing the shared instance causes "attempt to reopen an already-closed
 * object" crashes on the next query. The OS closes the file when the
 * process ends.
 */
public class ProductRepository {

    private final AppDatabase db;

    public ProductRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public boolean add(Product p) {
        p.id = UUID.randomUUID().toString();
        SQLiteDatabase wdb = db.getWritableDatabase();
        long result = wdb.insert(AppDatabase.TABLE_PRODUCTS, null, toValues(p));
        return result != -1;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Product> getAll() {
        return query("SELECT * FROM " + AppDatabase.TABLE_PRODUCTS
                + " ORDER BY name ASC", null);
    }

    public List<Product> getByCategory(String category) {
        return query(
                "SELECT * FROM " + AppDatabase.TABLE_PRODUCTS
                        + " WHERE category = ? ORDER BY name ASC",
                new String[]{category});
    }

    public List<Product> search(String q) {
        return query(
                "SELECT * FROM " + AppDatabase.TABLE_PRODUCTS
                        + " WHERE name LIKE ? OR barcode LIKE ? ORDER BY name ASC",
                new String[]{"%" + q + "%", "%" + q + "%"});
    }

    public Product getById(String id) {
        List<Product> list = query(
                "SELECT * FROM " + AppDatabase.TABLE_PRODUCTS + " WHERE id = ?",
                new String[]{id});
        return list.isEmpty() ? null : list.get(0);
    }

    /** Products where stock > 0 AND stock <= low_stock_alert */
    public List<Product> getLowStock() {
        return query(
                "SELECT * FROM " + AppDatabase.TABLE_PRODUCTS
                        + " WHERE stock > 0 AND stock <= low_stock_alert ORDER BY stock ASC",
                null);
    }

    /** Products where stock <= 0 */
    public List<Product> getOutOfStock() {
        return query(
                "SELECT * FROM " + AppDatabase.TABLE_PRODUCTS
                        + " WHERE stock <= 0",
                null);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean update(Product p) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        int rows = wdb.update(AppDatabase.TABLE_PRODUCTS,
                toValues(p), "id = ?", new String[]{p.id});
        return rows > 0;
    }

    public boolean updateStock(String productId, int newStock) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        ContentValues cv   = new ContentValues();
        cv.put(AppDatabase.COL_STOCK, newStock);
        int rows = wdb.update(AppDatabase.TABLE_PRODUCTS,
                cv, "id = ?", new String[]{productId});
        return rows > 0;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean delete(String id) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        int rows = wdb.delete(AppDatabase.TABLE_PRODUCTS,
                "id = ?", new String[]{id});
        return rows > 0;
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    public int getTotalCount() {
        return singleInt("SELECT COUNT(*) FROM " + AppDatabase.TABLE_PRODUCTS);
    }

    public int getLowStockCount() {
        return singleInt("SELECT COUNT(*) FROM " + AppDatabase.TABLE_PRODUCTS
                + " WHERE stock > 0 AND stock <= low_stock_alert");
    }

    public int getOutOfStockCount() {
        return singleInt("SELECT COUNT(*) FROM " + AppDatabase.TABLE_PRODUCTS
                + " WHERE stock <= 0");
    }

    public double getTotalStockValue() {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT SUM(selling_price * stock) FROM " + AppDatabase.TABLE_PRODUCTS,
                null);
        double val = 0;
        if (c.moveToFirst()) val = c.getDouble(0);
        c.close();
        return val;
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private List<Product> query(String sql, String[] args) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(sql, args);
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
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

    private ContentValues toValues(Product p) {
        ContentValues cv = new ContentValues();
        cv.put(AppDatabase.COL_ID,              p.id);
        cv.put(AppDatabase.COL_NAME,            p.name);
        cv.put(AppDatabase.COL_CATEGORY,        p.category);
        cv.put(AppDatabase.COL_EMOJI,           p.emoji);
        cv.put(AppDatabase.COL_SELLING_PRICE,   p.sellingPrice);
        cv.put(AppDatabase.COL_COST_PRICE,      p.costPrice);
        cv.put(AppDatabase.COL_STOCK,           p.stock);
        cv.put(AppDatabase.COL_LOW_STOCK_ALERT, p.lowStockAlert);
        cv.put(AppDatabase.COL_BARCODE,         p.barcode);
        return cv;
    }

    private Product fromCursor(Cursor c) {
        return new Product(
                str(c, AppDatabase.COL_ID),
                str(c, AppDatabase.COL_NAME),
                str(c, AppDatabase.COL_CATEGORY),
                str(c, AppDatabase.COL_EMOJI),
                c.getDouble(c.getColumnIndexOrThrow(AppDatabase.COL_SELLING_PRICE)),
                c.getDouble(c.getColumnIndexOrThrow(AppDatabase.COL_COST_PRICE)),
                c.getInt(c.getColumnIndexOrThrow(AppDatabase.COL_STOCK)),
                c.getInt(c.getColumnIndexOrThrow(AppDatabase.COL_LOW_STOCK_ALERT)),
                str(c, AppDatabase.COL_BARCODE)
        );
    }

    private String str(Cursor c, String col) {
        try { return c.getString(c.getColumnIndexOrThrow(col)); }
        catch (Exception e) { return ""; }
    }
}