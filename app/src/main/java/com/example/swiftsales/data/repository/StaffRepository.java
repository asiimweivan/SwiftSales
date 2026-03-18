package com.example.swiftsales.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.swiftsales.data.db.AppDatabase;
import com.example.swiftsales.model.Staff;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * StaffRepository — all DB operations for staff.
 * Never calls db.close() — singleton safety.
 */
public class StaffRepository {

    private final AppDatabase db;
    private static final String TABLE = AppDatabase.TABLE_STAFF;

    public StaffRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public boolean add(Staff s) {
        s.id       = UUID.randomUUID().toString();
        s.joinDate = now();
        SQLiteDatabase wdb = db.getWritableDatabase();
        long result = wdb.insert(TABLE, null, toValues(s));
        return result != -1;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Staff> getAll() {
        List<Staff> list = new ArrayList<>();
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT * FROM " + TABLE + " ORDER BY name ASC", null);
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public Staff getById(String id) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT * FROM " + TABLE + " WHERE id = ?",
                new String[]{id});
        Staff s = null;
        if (c.moveToFirst()) s = fromCursor(c);
        c.close();
        return s;
    }

    public Staff getByPin(String pin) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT * FROM " + TABLE + " WHERE pin = ? AND is_active = 1",
                new String[]{pin});
        Staff s = null;
        if (c.moveToFirst()) s = fromCursor(c);
        c.close();
        return s;
    }

    /** Returns true if the PIN is already taken by another staff member */
    public boolean pinExists(String pin, String excludeId) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c;
        if (excludeId != null) {
            c = rdb.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE
                            + " WHERE pin = ? AND id != ?",
                    new String[]{pin, excludeId});
        } else {
            c = rdb.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE + " WHERE pin = ?",
                    new String[]{pin});
        }
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count > 0;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean update(Staff s) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        int rows = wdb.update(TABLE, toValues(s),
                "id = ?", new String[]{s.id});
        return rows > 0;
    }

    public boolean toggleActive(String id, boolean active) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        ContentValues cv   = new ContentValues();
        cv.put("is_active", active ? 1 : 0);
        int rows = wdb.update(TABLE, cv, "id = ?", new String[]{id});
        return rows > 0;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean delete(String id) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        int rows = wdb.delete(TABLE, "id = ?", new String[]{id});
        return rows > 0;
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    public int getActiveCount() {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE + " WHERE is_active = 1", null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private ContentValues toValues(Staff s) {
        ContentValues cv = new ContentValues();
        cv.put("id",        s.id);
        cv.put("name",      s.name      != null ? s.name      : "");
        cv.put("phone",     s.phone     != null ? s.phone     : "");
        cv.put("email",     s.email     != null ? s.email     : "");
        cv.put("pin",       s.pin       != null ? s.pin       : "");
        cv.put("role",      s.role      != null ? s.role      : "Cashier");
        cv.put("is_active", s.isActive ? 1 : 0);
        cv.put("join_date", s.joinDate  != null ? s.joinDate  : "");
        return cv;
    }

    private Staff fromCursor(Cursor c) {
        Staff s     = new Staff();
        s.id        = str(c, "id");
        s.name      = str(c, "name");
        s.phone     = str(c, "phone");
        s.email     = str(c, "email");
        s.pin       = str(c, "pin");
        s.role      = str(c, "role");
        s.isActive  = c.getInt(c.getColumnIndexOrThrow("is_active")) == 1;
        s.joinDate  = str(c, "join_date");
        return s;
    }

    private String str(Cursor c, String col) {
        try { return c.getString(c.getColumnIndexOrThrow(col)); }
        catch (Exception e) { return ""; }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }
}