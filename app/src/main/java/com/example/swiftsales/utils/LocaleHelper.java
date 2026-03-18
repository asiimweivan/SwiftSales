package com.example.swiftsales.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * LocaleHelper — persists the user's chosen language and wraps every
 * Activity's Context so strings load in the correct locale.
 *
 * Usage:
 *   1. In SwiftSalesApp.attachBaseContext()  → wrap with LocaleHelper.wrap()
 *   2. In every Activity.attachBaseContext() → wrap with LocaleHelper.wrap()
 *   3. To change language                   → LocaleHelper.setLocale(ctx, "rw")
 *      then recreate() the Activity
 *
 * Supported locale codes:
 *   "en"  → English   (res/values/)
 *   "fr"  → French    (res/values-fr/)
 *   "rw"  → Kinyarwanda (res/values-rw/)
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "SwiftSalesAuth";
    private static final String KEY_LANG   = "app_language";
    public  static final String LANG_EN    = "en";
    public  static final String LANG_FR    = "fr";
    public  static final String LANG_RW    = "rw";

    // ── Persist chosen language ───────────────────────────────────────────────

    public static void setLocale(Context context, String languageCode) {
        persist(context, languageCode);
        updateResources(context, languageCode);
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, LANG_EN);
    }

    // ── Call from attachBaseContext() ─────────────────────────────────────────

    public static Context wrap(Context context) {
        String lang = getSavedLanguage(context);
        return updateResources(context, lang);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static void persist(Context context, String lang) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANG, lang)
                .apply();
    }

    private static Context updateResources(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            //noinspection deprecation
            context.getResources().updateConfiguration(
                    config, context.getResources().getDisplayMetrics());
            return context;
        }
    }
}