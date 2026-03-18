package com.example.swiftsales.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.dashboard.DashboardActivity;

public class BusinessSetupActivity extends BaseActivity {

    EditText etBusinessName, etTaxRate;
    Spinner  spinnerBusinessType, spinnerCurrency;
    TextView btnFinish, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_setup);

        etBusinessName      = findViewById(R.id.etBusinessName);
        etTaxRate           = findViewById(R.id.etTaxRate);
        spinnerBusinessType = findViewById(R.id.spinnerBusinessType);
        spinnerCurrency     = findViewById(R.id.spinnerCurrency);
        btnFinish           = findViewById(R.id.btnFinish);
        btnCancel           = findViewById(R.id.btnCancel);   // ← FIXED: was inside finishSetup()

        // Business types
        String[] types = {
                "Retail", "Grocery", "Pharmacy", "Restaurant",
                "Salon", "Electronics", "Wholesale", "Other"
        };
        spinnerBusinessType.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, types));

        // Currencies
        String[] currencies = {
                "RWF - Rwandan Franc",  "USD - US Dollar",
                "KES - Kenyan Shilling", "UGX - Ugandan Shilling",
                "TZS - Tanzanian Shilling", "NGN - Nigerian Naira",
                "GHS - Ghanaian Cedi",  "ZAR - South African Rand"
        };
        spinnerCurrency.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, currencies));

        // Pre-fill business name from sign-up
        String savedBusiness = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE)
                .getString("business_name", "");
        etBusinessName.setText(savedBusiness);

        btnFinish.setOnClickListener(v -> finishSetup());

        // ── FIXED: listener is now in onCreate, not inside finishSetup() ──────
        btnCancel.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finishAffinity();
        });
    }

    private void finishSetup() {
        String businessName = etBusinessName.getText().toString().trim();

        if (TextUtils.isEmpty(businessName)) {
            etBusinessName.setError(getString(R.string.error_business_name_required));
            etBusinessName.requestFocus();
            return;
        }

        String businessType = spinnerBusinessType.getSelectedItem().toString();
        String currency     = spinnerCurrency.getSelectedItem().toString();
        String taxRate      = etTaxRate.getText().toString().trim();

        getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE).edit()
                .putString("business_name",  businessName)
                .putString("business_type",  businessType)
                .putString("currency",       currency)
                .putString("tax_rate",       taxRate.isEmpty() ? "0" : taxRate)
                .putBoolean("setup_complete", true)
                .apply();

        Toast.makeText(this,
                getString(R.string.setup_complete_toast), Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, DashboardActivity.class));
        finishAffinity();
    }
}