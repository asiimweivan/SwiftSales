package com.example.swiftsales.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.settings.SettingsActivity;

public class ProfileActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    TextView     btnBackProfile;
    TextView     tvProfileAvatar, tvProfileName, tvProfileBusiness;
    TextView     tvProfilePhone, tvProfileEmail, tvProfileCurrency, tvProfileRole;
    LinearLayout btnGoSettings, btnGoPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindViews();
        loadProfileData();

        btnBackProfile.setOnClickListener(v -> finish());

        // Both shortcuts open SettingsActivity — the user navigates to what they need
        btnGoSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        btnGoPassword.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData(); // refresh if returning from settings
    }

    private void bindViews() {
        btnBackProfile     = findViewById(R.id.btnBackProfile);
        tvProfileAvatar    = findViewById(R.id.tvProfileAvatar);
        tvProfileName      = findViewById(R.id.tvProfileName);
        tvProfileBusiness  = findViewById(R.id.tvProfileBusiness);
        tvProfilePhone     = findViewById(R.id.tvProfilePhone);
        tvProfileEmail     = findViewById(R.id.tvProfileEmail);
        tvProfileCurrency  = findViewById(R.id.tvProfileCurrency);
        tvProfileRole      = findViewById(R.id.tvProfileRole);
        btnGoSettings      = findViewById(R.id.btnGoToSettings);
        btnGoPassword      = findViewById(R.id.btnGoToPassword);
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);

        String name     = prefs.getString("user_name",     "—");
        String biz      = prefs.getString("business_name", "—");
        String phone    = prefs.getString("user_phone",    "—");
        String email    = prefs.getString("user_email",    "—");
        String currency = prefs.getString("currency",      "RWF");
        String bizType  = prefs.getString("business_type", "—");

        // Avatar initials
        String[] parts  = name.split(" ");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
                : name.substring(0, Math.min(2, name.length())).toUpperCase();

        tvProfileAvatar.setText(initials);
        tvProfileName.setText(name);
        tvProfileBusiness.setText(biz);
        tvProfilePhone.setText(phone);
        tvProfileEmail.setText(email.isEmpty() || email.equals("—") ? "—" : email);
        tvProfileCurrency.setText(currency);
        tvProfileRole.setText(bizType);
    }
}