package com.example.swiftsales.ui.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.swiftsales.R;
import com.example.swiftsales.model.Staff;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.UUID;

public class AddEditStaffSheet extends BottomSheetDialogFragment {

    public interface OnSavedListener {
        void onSaved(Staff staff, boolean isEdit);
    }

    private OnSavedListener savedListener;
    public void setOnSavedListener(OnSavedListener l) { this.savedListener = l; }

    private static final String KEY_ID    = "id";
    private static final String KEY_NAME  = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE  = "role";
    private static final String KEY_PIN   = "pin";

    // ── Views — plain EditText to match dialog_add_staff.xml ─────────────────
    private TextView tvTitle;
    private EditText etStaffName, etStaffPhone, etStaffEmail, etStaffPin;
    private Spinner  spinnerRole;
    private TextView btnSaveStaff, btnCancelStaff;

    // ── Factory — pass existing Staff for edit mode, null for add ─────────────
    public static AddEditStaffSheet newInstance(@Nullable Staff s) {
        AddEditStaffSheet f = new AddEditStaffSheet();
        if (s != null) {
            Bundle b = new Bundle();
            b.putString(KEY_ID,    s.id);
            b.putString(KEY_NAME,  s.name    != null ? s.name    : "");
            b.putString(KEY_PHONE, s.phone   != null ? s.phone   : "");
            b.putString(KEY_EMAIL, s.email   != null ? s.email   : "");
            b.putString(KEY_ROLE,  s.role    != null ? s.role    : "Cashier");
            b.putString(KEY_PIN,   s.pin     != null ? s.pin     : "");
            f.setArguments(b);
        }
        return f;
    }

    // ── Inflate ───────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_staff, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRoleSpinner();
        populateFields();
        wireButtons();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews(View v) {
        tvTitle        = v.findViewById(R.id.tvStaffDialogTitle);
        etStaffName    = v.findViewById(R.id.etStaffName);
        etStaffPhone   = v.findViewById(R.id.etStaffPhone);
        etStaffEmail   = v.findViewById(R.id.etStaffEmail);
        spinnerRole    = v.findViewById(R.id.spinnerRole);
        etStaffPin     = v.findViewById(R.id.etStaffPin);
        btnSaveStaff   = v.findViewById(R.id.btnSaveStaff);
        btnCancelStaff = v.findViewById(R.id.btnCancelStaff);
    }

    // ── Spinner ───────────────────────────────────────────────────────────────

    private void setupRoleSpinner() {
        String[] roles = {"Cashier", "Manager", "Supervisor"};
        spinnerRole.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, roles));
    }

    // ── Pre-fill fields in edit mode ──────────────────────────────────────────

    private void populateFields() {
        Bundle b = getArguments();
        if (b == null || !b.containsKey(KEY_ID)) {
            // Add mode
            tvTitle.setText(getString(R.string.add_staff_member));
            return;
        }
        // Edit mode
        tvTitle.setText(getString(R.string.edit_staff_member));
        etStaffName.setText(b.getString(KEY_NAME,  ""));
        etStaffPhone.setText(b.getString(KEY_PHONE, ""));
        etStaffEmail.setText(b.getString(KEY_EMAIL, ""));
        etStaffPin.setText(b.getString(KEY_PIN,   ""));

        String savedRole = b.getString(KEY_ROLE, "Cashier");
        String[] roles   = {"Cashier", "Manager", "Supervisor"};
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(savedRole)) {
                spinnerRole.setSelection(i);
                break;
            }
        }
    }

    // ── Button listeners ──────────────────────────────────────────────────────

    private void wireButtons() {
        btnCancelStaff.setOnClickListener(v -> dismiss());

        btnSaveStaff.setOnClickListener(v -> {
            String name  = text(etStaffName);
            String phone = text(etStaffPhone);
            String pin   = text(etStaffPin);

            // Validate
            if (name.isEmpty()) {
                etStaffName.setError(getString(R.string.name_required));
                etStaffName.requestFocus();
                return;
            }
            if (phone.isEmpty()) {
                etStaffPhone.setError(getString(R.string.phone_required));
                etStaffPhone.requestFocus();
                return;
            }
            if (pin.isEmpty() || pin.length() != 4) {
                etStaffPin.setError(getString(R.string.pin_error));
                etStaffPin.requestFocus();
                return;
            }

            // Build Staff object
            Bundle b      = getArguments();
            boolean isEdit = (b != null && b.containsKey(KEY_ID));

            Staff s    = new Staff();
            s.id       = isEdit ? b.getString(KEY_ID) : UUID.randomUUID().toString();
            s.name     = name;
            s.phone    = phone;
            s.email    = text(etStaffEmail);
            s.role     = spinnerRole.getSelectedItem().toString();
            s.pin      = pin;
            s.isActive = true;

            if (savedListener != null) savedListener.onSaved(s, isEdit);
            dismiss();
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String text(EditText et) {
        return (et != null && et.getText() != null)
                ? et.getText().toString().trim() : "";
    }
}