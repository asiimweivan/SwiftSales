package com.example.swiftsales.ui.language;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.dashboard.DashboardActivity;
import com.example.swiftsales.ui.onboarding.OnboardingActivity;
import com.example.swiftsales.utils.LocaleHelper;

public class LanguageActivity extends BaseActivity {

    private String  selectedLang = LocaleHelper.LANG_EN;
    private boolean isLoggedIn   = false;

    // ── Views — IDs match activity_language.xml exactly ──────────────────────
    private View     optionEnglish, optionFrench, optionKinyarwanda;
    private TextView checkEnglish, checkFrench, checkKinyarwanda;
    private TextView btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        isLoggedIn   = prefs.getBoolean("is_logged_in", false);
        selectedLang = LocaleHelper.getSavedLanguage(this);

        bindViews();
        highlightSelected();
        setupClickListeners();
    }

    private void bindViews() {
        optionEnglish     = findViewById(R.id.optionEnglish);
        optionFrench      = findViewById(R.id.optionFrench);
        optionKinyarwanda = findViewById(R.id.optionKinyarwanda);
        checkEnglish      = findViewById(R.id.checkEnglish);
        checkFrench       = findViewById(R.id.checkFrench);
        checkKinyarwanda  = findViewById(R.id.checkKinyarwanda);
        btnContinue       = findViewById(R.id.btnContinue);
    }

    private void setupClickListeners() {
        optionEnglish.setOnClickListener(v     -> selectLanguage(LocaleHelper.LANG_EN));
        optionFrench.setOnClickListener(v      -> selectLanguage(LocaleHelper.LANG_FR));
        optionKinyarwanda.setOnClickListener(v -> selectLanguage(LocaleHelper.LANG_RW));
        btnContinue.setOnClickListener(v       -> proceed());
    }

    private void selectLanguage(String langCode) {
        selectedLang = langCode;
        LocaleHelper.setLocale(this, langCode);
        highlightSelected();
        recreate(); // re-inflate so labels update immediately
    }

    private void highlightSelected() {
        setOptionActive(optionEnglish,     checkEnglish,     LocaleHelper.LANG_EN.equals(selectedLang));
        setOptionActive(optionFrench,      checkFrench,      LocaleHelper.LANG_FR.equals(selectedLang));
        setOptionActive(optionKinyarwanda, checkKinyarwanda, LocaleHelper.LANG_RW.equals(selectedLang));
    }

    private void setOptionActive(View option, TextView check, boolean active) {
        if (option == null) return;
        option.setBackgroundResource(
                active ? R.drawable.lang_option_selected : R.drawable.lang_option_normal);
        if (check != null)
            check.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
    }

    private void proceed() {
        // ── Mark language as chosen so SplashActivity routes correctly next time
        getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE)
                .edit()
                .putBoolean("lang_chosen", true)
                .apply();

        Intent intent = isLoggedIn
                ? new Intent(this, DashboardActivity.class)
                : new Intent(this, OnboardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}