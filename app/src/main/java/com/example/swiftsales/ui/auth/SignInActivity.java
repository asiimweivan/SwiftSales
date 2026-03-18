package com.example.swiftsales.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.dashboard.DashboardActivity;

public class SignInActivity extends BaseActivity {

    EditText etPhoneEmail, etPassword;
    TextView btnSignIn, tvSignUp, tvForgotPassword, tvShowPassword;
    boolean  passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etPhoneEmail     = findViewById(R.id.etPhoneEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnSignIn        = findViewById(R.id.btnSignIn);
        tvSignUp         = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvShowPassword   = findViewById(R.id.tvShowPassword);

        // Show/hide password
        tvShowPassword.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                etPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance());
                tvShowPassword.setText(getString(R.string.hide));
            } else {
                etPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance());
                tvShowPassword.setText(getString(R.string.show));
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptSignIn() {
        String phoneEmail = etPhoneEmail.getText().toString().trim();
        String password   = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phoneEmail)) {
            etPhoneEmail.setError(getString(R.string.error_phone_email_required));
            etPhoneEmail.requestFocus(); return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus(); return;
        }

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        String savedPhone    = prefs.getString("user_phone",    "");
        String savedEmail    = prefs.getString("user_email",    "");
        String savedPassword = prefs.getString("user_password", "");

        boolean phoneMatch = phoneEmail.equals(savedPhone);
        boolean emailMatch = phoneEmail.equals(savedEmail);

        if ((phoneMatch || emailMatch) && password.equals(savedPassword)) {
            prefs.edit().putBoolean("is_logged_in", true).apply();
            Toast.makeText(this,
                    getString(R.string.welcome_back_toast), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finishAffinity();
        } else {
            Toast.makeText(this,
                    getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
        }
    }
}