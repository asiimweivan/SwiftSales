package com.example.swiftsales.ui.settings;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.swiftsales.R;
import com.example.swiftsales.data.db.AppDatabase;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.auth.SignInActivity;
import com.example.swiftsales.ui.language.LanguageActivity;
import com.example.swiftsales.utils.LocaleHelper;

public class SettingsActivity extends BaseActivity {

    // ── Views (same IDs as before) ───────────────────────────────────────────
    TextView     btnBackSettings, tvAppVersion;
    TextView     tvSettingsAvatar, tvSettingsBusinessName,
            tvSettingsUserName, tvSettingsCurrency;

    LinearLayout rowBusinessName, rowBusinessType, rowCurrency, rowTax;
    TextView     tvBizName, tvBizType, tvCurrency, tvTaxRate;

    LinearLayout rowFullName, rowPhone, rowPassword;
    TextView     tvFullName, tvPhone;

    LinearLayout rowLanguage, rowReceipt;
    TextView     tvLanguage;

    LinearLayout rowSignOut, rowClearData;

    SharedPreferences prefs;

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);

        bindViews();
        loadCurrentValues();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentValues();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        btnBackSettings        = findViewById(R.id.btnBackSettings);
        tvAppVersion           = findViewById(R.id.tvAppVersion);
        tvSettingsAvatar       = findViewById(R.id.tvSettingsAvatar);
        tvSettingsBusinessName = findViewById(R.id.tvSettingsBusinessName);
        tvSettingsUserName     = findViewById(R.id.tvSettingsUserName);
        tvSettingsCurrency     = findViewById(R.id.tvSettingsCurrency);

        rowBusinessName = findViewById(R.id.rowBusinessName);
        rowBusinessType = findViewById(R.id.rowBusinessType);
        rowCurrency     = findViewById(R.id.rowCurrency);
        rowTax          = findViewById(R.id.rowTax);
        tvBizName       = findViewById(R.id.tvBizName);
        tvBizType       = findViewById(R.id.tvBizType);
        tvCurrency      = findViewById(R.id.tvCurrency);
        tvTaxRate       = findViewById(R.id.tvTaxRate);

        rowFullName  = findViewById(R.id.rowFullName);
        rowPhone     = findViewById(R.id.rowPhone);
        rowPassword  = findViewById(R.id.rowPassword);
        tvFullName   = findViewById(R.id.tvFullName);
        tvPhone      = findViewById(R.id.tvPhone);

        rowLanguage = findViewById(R.id.rowLanguage);
        rowReceipt  = findViewById(R.id.rowReceipt);
        tvLanguage  = findViewById(R.id.tvLanguage);

        rowSignOut   = findViewById(R.id.rowSignOut);
        rowClearData = findViewById(R.id.rowClearData);
    }

    private void loadCurrentValues() {
        String name     = prefs.getString("user_name",     "—");
        String phone    = prefs.getString("user_phone",    "—");
        String bizName  = prefs.getString("business_name", "—");
        String bizType  = prefs.getString("business_type", "—");
        String currency = prefs.getString("currency",      "RWF");
        String taxRate  = prefs.getString("tax_rate",      "0");

        // ── FIX: read language from the same prefs key LocaleHelper writes ──
        String lang = LocaleHelper.getSavedLanguage(this);

        // Header card
        tvSettingsBusinessName.setText(bizName);
        tvSettingsUserName.setText(name);
        tvSettingsCurrency.setText(currency);

        // Avatar initials
        String[] parts  = name.split(" ");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
                : name.substring(0, Math.min(2, name.length())).toUpperCase();
        tvSettingsAvatar.setText(initials);

        // Business section
        tvBizName.setText(bizName);
        tvBizType.setText(bizType);
        tvCurrency.setText(currency);
        tvTaxRate.setText(taxRate + "%");

        // Account section
        tvFullName.setText(name);
        tvPhone.setText(phone);

        // Language label — uses string resources for i18n
        switch (lang) {
            case LocaleHelper.LANG_FR: tvLanguage.setText(getString(R.string.lang_french));      break;
            case LocaleHelper.LANG_RW: tvLanguage.setText(getString(R.string.lang_kinyarwanda)); break;
            default:                   tvLanguage.setText(getString(R.string.lang_english));     break;
        }
    }

    // ── Click listeners (all logic identical to original) ─────────────────────

    private void setupClickListeners() {
        btnBackSettings.setOnClickListener(v -> finish());

        // ── Business ──────────────────────────────────────────────────────────
        rowBusinessName.setOnClickListener(v ->
                showEditDialog(getString(R.string.setting_business_name),
                        "business_name", InputType.TYPE_CLASS_TEXT, null));

        rowBusinessType.setOnClickListener(v -> {
            String[] types = {"Retail", "Grocery", "Pharmacy", "Restaurant",
                    "Salon", "Electronics", "Wholesale", "Other"};
            showPickerDialog(getString(R.string.setting_business_type), types, "business_type");
        });

        rowCurrency.setOnClickListener(v -> {
            String[] currencies = {
                    "RWF - Rwandan Franc",  "USD - US Dollar",
                    "KES - Kenyan Shilling", "UGX - Ugandan Shilling",
                    "TZS - Tanzanian Shilling", "NGN - Nigerian Naira",
                    "GHS - Ghanaian Cedi",  "ZAR - South African Rand"
            };
            showPickerDialog(getString(R.string.setting_currency), currencies, "currency");
        });

        rowTax.setOnClickListener(v ->
                showEditDialog(getString(R.string.setting_tax_rate),
                        "tax_rate",
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
                        null));

        // ── Account ───────────────────────────────────────────────────────────
        rowFullName.setOnClickListener(v ->
                showEditDialog(getString(R.string.setting_full_name),
                        "user_name",
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                        null));

        rowPhone.setOnClickListener(v ->
                showEditDialog(getString(R.string.setting_phone),
                        "user_phone", InputType.TYPE_CLASS_PHONE, null));

        rowPassword.setOnClickListener(v -> showChangePasswordDialog());

        // ── App ───────────────────────────────────────────────────────────────
        rowLanguage.setOnClickListener(v ->
                startActivity(new Intent(this, LanguageActivity.class)));

        rowReceipt.setOnClickListener(v ->
                showEditDialog(getString(R.string.setting_receipt_footer),
                        "receipt_footer", InputType.TYPE_CLASS_TEXT,
                        getString(R.string.receipt_footer_default)));

        // ── Danger zone ───────────────────────────────────────────────────────
        rowSignOut.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sign_out_title))
                        .setMessage(getString(R.string.sign_out_msg))
                        .setPositiveButton(getString(R.string.sign_out), (d, w) -> {
                            prefs.edit().putBoolean("is_logged_in", false).apply();
                            startActivity(new Intent(this, SignInActivity.class));
                            finishAffinity();
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show());

        rowClearData.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.clear_data_title))
                        .setMessage(getString(R.string.clear_data_msg))
                        .setPositiveButton(getString(R.string.clear_data_confirm),
                                (d, w) -> clearSalesData())
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show());
    }

    // ── Edit dialog — single text field (identical logic) ─────────────────────

    private void showEditDialog(String title, String prefKey,
                                int inputType, String defaultHint) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_setting);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle   = dialog.findViewById(R.id.tvEditSettingTitle);
        EditText etValue   = dialog.findViewById(R.id.etSettingValue);
        TextView btnSave   = dialog.findViewById(R.id.btnSaveSetting);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelSetting);

        tvTitle.setText(title);
        etValue.setInputType(inputType);

        // Pre-fill current value
        String current = prefKey.equals("receipt_footer")
                ? getSharedPreferences("SwiftSalesSettings", MODE_PRIVATE)
                .getString(prefKey, defaultHint != null ? defaultHint : "")
                : prefs.getString(prefKey, "");
        etValue.setText(current);
        etValue.setSelection(current.length());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String val = etValue.getText().toString().trim();
            if (TextUtils.isEmpty(val)) {
                etValue.setError(getString(R.string.cannot_be_empty));
                return;
            }
            if (prefKey.equals("receipt_footer")) {
                getSharedPreferences("SwiftSalesSettings", MODE_PRIVATE)
                        .edit().putString(prefKey, val).apply();
            } else {
                prefs.edit().putString(prefKey, val).apply();
            }
            Toast.makeText(this,
                    title + " " + getString(R.string.updated_success),
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadCurrentValues();
        });

        dialog.show();
    }

    // ── Picker dialog — list of choices (identical logic) ────────────────────

    private void showPickerDialog(String title, String[] options, String prefKey) {
        String current = prefs.getString(prefKey, "");
        int    checked = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].startsWith(current)) { checked = i; break; }
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(options, checked, null)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    int    selected = ((AlertDialog) d).getListView().getCheckedItemPosition();
                    String value    = options[selected];
                    if (prefKey.equals("currency")) value = value.substring(0, 3);
                    prefs.edit().putString(prefKey, value).apply();
                    Toast.makeText(this,
                            title + " " + getString(R.string.updated_success),
                            Toast.LENGTH_SHORT).show();
                    loadCurrentValues();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    // ── Change password dialog (identical logic) ──────────────────────────────

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etCurrent = dialog.findViewById(R.id.etCurrentPassword);
        EditText etNew     = dialog.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialog.findViewById(R.id.etConfirmNewPassword);
        TextView btnSave   = dialog.findViewById(R.id.btnSavePassword);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelPassword);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String current = etCurrent.getText().toString().trim();
            String newPass = etNew.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();
            String saved   = prefs.getString("user_password", "");

            if (!current.equals(saved)) {
                etCurrent.setError(getString(R.string.incorrect_password));
                etCurrent.requestFocus(); return;
            }
            if (newPass.length() < 8) {
                etNew.setError(getString(R.string.password_min_length));
                etNew.requestFocus(); return;
            }
            if (!newPass.equals(confirm)) {
                etConfirm.setError(getString(R.string.passwords_no_match));
                etConfirm.requestFocus(); return;
            }

            prefs.edit().putString("user_password", newPass).apply();
            Toast.makeText(this,
                    getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // ── Clear sales data — off main thread ────────────────────────────────────

    private void clearSalesData() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... v) {
                AppDatabase.getInstance(SettingsActivity.this)
                        .getWritableDatabase()
                        .execSQL("DELETE FROM " + AppDatabase.TABLE_SALES);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.sales_data_cleared),
                        Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}