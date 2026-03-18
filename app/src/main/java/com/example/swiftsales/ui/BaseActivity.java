package com.example.swiftsales.ui;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swiftsales.utils.LocaleHelper;

/**
 * BaseActivity — extends AppCompatActivity and wraps the context so every
 * screen automatically renders in the user's chosen language.
 *
 * Every Activity in SwiftSales should extend BaseActivity instead of
 * AppCompatActivity:
 *
 *   public class DashboardActivity extends BaseActivity { ... }
 *   public class InventoryActivity  extends BaseActivity { ... }
 *   public class StaffActivity      extends BaseActivity { ... }
 *   ... etc.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}