package com.example.swiftsales;

import android.app.Application;
import android.content.Context;

import com.example.swiftsales.utils.LocaleHelper;

/**
 * SwiftSalesApp — Application class.
 *
 * Register this in AndroidManifest.xml:
 *   <application android:name=".SwiftSalesApp" ...>
 */
public class SwiftSalesApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
}