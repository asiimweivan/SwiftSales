package com.example.swiftsales.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swiftsales.R;
import com.example.swiftsales.ui.BaseActivity;

public class ForgotPasswordActivity extends BaseActivity {

    EditText etPhone;
    TextView btnSendCode, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etPhone     = findViewById(R.id.etPhone);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnBack     = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSendCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                etPhone.setError(getString(R.string.error_phone_required));
                etPhone.requestFocus();
                return;
            }
            Toast.makeText(this,
                    getString(R.string.reset_code_sent_toast, phone),
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }
}