package com.example.swiftsales.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {

    public static final String DB_NAME    = "swiftsales.db";
    public static final int    DB_VERSION = 2;   // bumped for orders table + staff columns

    // ── Table names ──────────────────────────────────────────────────────────
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_SALES    = "sales";
    public static final String TABLE_STAFF    = "staff";
    public static final String TABLE_ORDERS   = "orders";   // NEW

    // ── Products columns ──────────────────────────────────────────────────────
    public static final String COL_ID              = "id";
    public static final String COL_NAME            = "name";
    public static final String COL_CATEGORY        = "category";
    public static final String COL_EMOJI           = "emoji";
    public static final String COL_SELLING_PRICE   = "selling_price";
    public static final String COL_COST_PRICE      = "cost_price";
    public static final String COL_STOCK           = "stock";
    public static final String COL_LOW_STOCK_ALERT = "low_stock_alert";
    public static final String COL_BARCODE         = "barcode";

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null)
            instance = new AppDatabase(context.getApplicationContext());
        return instance;
    }

    private AppDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // onCreate — called only on fresh install
    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_SALES_TABLE);
        db.execSQL(CREATE_STAFF_TABLE);
        db.execSQL(CREATE_ORDERS_TABLE);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // onUpgrade — NEVER drops tables (protects user data)
    //             uses additive migrations only
    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            // Add missing staff columns that were absent in v1
            safeAddColumn(db, TABLE_STAFF, "phone",     "TEXT DEFAULT ''");
            safeAddColumn(db, TABLE_STAFF, "is_active", "INTEGER DEFAULT 1");
            safeAddColumn(db, TABLE_STAFF, "join_date", "TEXT DEFAULT ''");

            // Add payment_method alias for sales (v1 used "payment")
            safeAddColumn(db, TABLE_SALES, "payment_method", "TEXT DEFAULT 'Cash'");

            // Create orders table (new in v2)
            db.execSQL(CREATE_ORDERS_TABLE);
        }

        // Template for future versions:
        // if (oldVersion < 3) { ... }
    }

    // ── Table definitions ─────────────────────────────────────────────────────

    private static final String CREATE_PRODUCTS_TABLE =
            "CREATE TABLE IF NOT EXISTS products (" +
                    "id               TEXT PRIMARY KEY," +
                    "name             TEXT NOT NULL," +
                    "category         TEXT," +
                    "emoji            TEXT," +
                    "selling_price    REAL    DEFAULT 0," +
                    "cost_price       REAL    DEFAULT 0," +
                    "stock            INTEGER DEFAULT 0," +
                    "low_stock_alert  INTEGER DEFAULT 5," +
                    "barcode          TEXT" +
                    ")";

    private static final String CREATE_SALES_TABLE =
            "CREATE TABLE IF NOT EXISTS sales (" +
                    "id             TEXT PRIMARY KEY," +
                    "date           TEXT," +
                    "items_json     TEXT," +
                    "total          REAL    DEFAULT 0," +
                    "tax            REAL    DEFAULT 0," +
                    "profit         REAL    DEFAULT 0," +
                    "cashier_id     TEXT," +
                    "payment_method TEXT    DEFAULT 'Cash'" +
                    ")";

    private static final String CREATE_STAFF_TABLE =
            "CREATE TABLE IF NOT EXISTS staff (" +
                    "id        TEXT PRIMARY KEY," +
                    "name      TEXT," +
                    "phone     TEXT    DEFAULT ''," +
                    "email     TEXT    DEFAULT ''," +
                    "pin       TEXT," +
                    "role      TEXT    DEFAULT 'Cashier'," +
                    "is_active INTEGER DEFAULT 1," +
                    "join_date TEXT    DEFAULT ''" +
                    ")";

    private static final String CREATE_ORDERS_TABLE =
            "CREATE TABLE IF NOT EXISTS orders (" +
                    "id               TEXT PRIMARY KEY," +
                    "customer_name    TEXT," +
                    "customer_contact TEXT," +
                    "channel          TEXT," +
                    "status           TEXT    DEFAULT 'Pending'," +
                    "total            REAL    DEFAULT 0," +
                    "items_json       TEXT," +
                    "notes            TEXT," +
                    "created_at       TEXT" +
                    ")";

    // ── Helper — adds a column only if it doesn't already exist ──────────────
    // Prevents crashes if onUpgrade is called multiple times or on partial upgrades

    private void safeAddColumn(SQLiteDatabase db, String table,
                               String column, String definition) {
        try {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (Exception ignored) {
            // Column already exists — safe to ignore
        }
    }
}