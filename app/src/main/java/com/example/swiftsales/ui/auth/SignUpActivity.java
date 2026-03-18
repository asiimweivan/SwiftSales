package com.example.swiftsales.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;

public class SignUpActivity extends BaseActivity {

    EditText etFullName, etPhone, etEmail, etBusinessName, etPassword, etConfirmPassword;
    TextView btnCreateAccount, tvSignIn, tvShowPassword, tvShowConfirm;
    CheckBox cbTerms;
    boolean  passwordVisible = false;
    boolean  confirmVisible  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etFullName        = findViewById(R.id.etFullName);
        etPhone           = findViewById(R.id.etPhone);
        etEmail           = findViewById(R.id.etEmail);
        etBusinessName    = findViewById(R.id.etBusinessName);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount  = findViewById(R.id.btnCreateAccount);
        tvSignIn          = findViewById(R.id.tvSignIn);
        tvShowPassword    = findViewById(R.id.tvShowPassword);
        tvShowConfirm     = findViewById(R.id.tvShowConfirm);
        cbTerms           = findViewById(R.id.cbTerms);

        // Show / hide password
        tvShowPassword.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            etPassword.setTransformationMethod(passwordVisible
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            tvShowPassword.setText(getString(passwordVisible ? R.string.hide : R.string.show));
            etPassword.setSelection(etPassword.getText().length());
        });

        tvShowConfirm.setOnClickListener(v -> {
            confirmVisible = !confirmVisible;
            etConfirmPassword.setTransformationMethod(confirmVisible
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            tvShowConfirm.setText(getString(confirmVisible ? R.string.hide : R.string.show));
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        btnCreateAccount.setOnClickListener(v -> attemptSignUp());

        tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void attemptSignUp() {
        String fullName        = etFullName.getText().toString().trim();
        String phone           = etPhone.getText().toString().trim();
        String email           = etEmail.getText().toString().trim();
        String businessName    = etBusinessName.getText().toString().trim();
        String password        = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.error_full_name_required));
            etFullName.requestFocus(); return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_phone_required));
            etPhone.requestFocus(); return;
        }
        if (TextUtils.isEmpty(businessName)) {
            etBusinessName.setError(getString(R.string.error_business_name_required));
            etBusinessName.requestFocus(); return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus(); return;
        }
        if (password.length() < 8) {
            etPassword.setError(getString(R.string.error_password_min_length));
            etPassword.requestFocus(); return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_no_match));
            etConfirmPassword.requestFocus(); return;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this,
                    getString(R.string.error_terms_not_agreed), Toast.LENGTH_SHORT).show();
            return;
        }

        // Save credentials
        getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE).edit()
                .putString("user_name",      fullName)
                .putString("user_phone",     phone)
                .putString("user_email",     email)
                .putString("business_name",  businessName)
                .putString("user_password",  password)
                .putBoolean("is_logged_in",  true)
                .apply();

        Toast.makeText(this,
                getString(R.string.account_created_toast, fullName),
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, BusinessSetupActivity.class));
        finishAffinity();
    }
}