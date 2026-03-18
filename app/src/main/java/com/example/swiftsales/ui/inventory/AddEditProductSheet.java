package com.example.swiftsales.ui.inventory;

import android.content.Intent;
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
import com.example.swiftsales.model.Product;
import com.example.swiftsales.utils.BarcodeHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.UUID;

public class AddEditProductSheet extends BottomSheetDialogFragment {

    public interface OnSavedListener {
        void onSaved(Product product, boolean isEdit);
    }

    private OnSavedListener savedListener;
    public void setOnSavedListener(OnSavedListener l) { this.savedListener = l; }

    private static final String KEY_ID       = "id";
    private static final String KEY_NAME     = "name";
    private static final String KEY_CAT      = "cat";
    private static final String KEY_EMOJI    = "emoji";
    private static final String KEY_SELL     = "sell";
    private static final String KEY_COST     = "cost";
    private static final String KEY_STOCK    = "stock";
    private static final String KEY_ALERT    = "alert";
    private static final String KEY_BARCODE  = "barcode";
    private static final String KEY_CURRENCY = "currency";

    private TextView tvTitle, tvSelectedEmoji;
    private EditText etProductName, etSellingPrice, etCostPrice;
    private EditText etStock, etLowStockAlert, etBarcode;
    private Spinner  spinnerCategory;
    private TextView btnSaveProduct, btnCancelProduct, btnScanBarcode;

    // ── Factory ───────────────────────────────────────────────────────────────

    public static AddEditProductSheet newInstance(@Nullable Product p, String currency) {
        AddEditProductSheet f = new AddEditProductSheet();
        Bundle b = new Bundle();
        b.putString(KEY_CURRENCY, currency);
        if (p != null) {
            b.putString(KEY_ID,      p.id);
            b.putString(KEY_NAME,    p.name     != null ? p.name     : "");
            b.putString(KEY_CAT,     p.category != null ? p.category : "");
            b.putString(KEY_EMOJI,   p.emoji    != null ? p.emoji    : "📦");
            b.putDouble(KEY_SELL,    p.sellingPrice);
            b.putDouble(KEY_COST,    p.costPrice);
            b.putInt   (KEY_STOCK,   p.stock);
            b.putInt   (KEY_ALERT,   p.lowStockAlert);
            b.putString(KEY_BARCODE, p.barcode  != null ? p.barcode  : "");
        }
        f.setArguments(b);
        return f;
    }

    // ── Inflate ───────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupEmojiPicker(view);
        setupCategorySpinner();
        populateFields();
        wireButtons();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews(View v) {
        tvTitle         = v.findViewById(R.id.tvDialogTitle);
        tvSelectedEmoji = v.findViewById(R.id.tvSelectedEmoji);
        etProductName   = v.findViewById(R.id.etProductName);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        etSellingPrice  = v.findViewById(R.id.etSellingPrice);
        etCostPrice     = v.findViewById(R.id.etCostPrice);
        etStock         = v.findViewById(R.id.etStock);
        etLowStockAlert = v.findViewById(R.id.etLowStockAlert);
        etBarcode       = v.findViewById(R.id.etBarcode);
        btnSaveProduct  = v.findViewById(R.id.btnSaveProduct);
        btnCancelProduct = v.findViewById(R.id.btnCancelProduct);
        btnScanBarcode  = v.findViewById(R.id.btnScanBarcode);   // NEW
    }

    // ── Emoji picker ──────────────────────────────────────────────────────────

    private void setupEmojiPicker(View v) {
        String[] emojis   = {"📦","🍎","🥤","🧴","🍞","🧃","📱","🧹"};
        int[]    emojiIds = {R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4,
                R.id.emoji5, R.id.emoji6, R.id.emoji7, R.id.emoji8};
        for (int i = 0; i < emojiIds.length; i++) {
            final String e = emojis[i];
            View btn = v.findViewById(emojiIds[i]);
            if (btn != null) btn.setOnClickListener(ev -> tvSelectedEmoji.setText(e));
        }
    }

    // ── Spinner ───────────────────────────────────────────────────────────────

    private void setupCategorySpinner() {
        String[] cats = {"Food", "Drinks", "Household", "Electronics", "Other"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, cats));
    }

    // ── Pre-fill fields (edit mode) ───────────────────────────────────────────

    private void populateFields() {
        Bundle b = getArguments();
        if (b == null || !b.containsKey(KEY_ID)) {
            tvTitle.setText(getString(R.string.add_product_title));
            return;
        }
        tvTitle.setText(getString(R.string.edit_product_title));
        tvSelectedEmoji.setText(b.getString(KEY_EMOJI, "📦"));
        setET(etProductName,  b.getString(KEY_NAME, ""));
        double sell = b.getDouble(KEY_SELL);
        double cost = b.getDouble(KEY_COST);
        setET(etSellingPrice,  sell > 0 ? String.valueOf((long) sell) : "");
        setET(etCostPrice,     cost > 0 ? String.valueOf((long) cost) : "");
        setET(etStock,         String.valueOf(b.getInt(KEY_STOCK)));
        setET(etLowStockAlert, String.valueOf(b.getInt(KEY_ALERT)));
        setET(etBarcode,       b.getString(KEY_BARCODE, ""));

        String   cat  = b.getString(KEY_CAT, "");
        String[] cats = {"Food", "Drinks", "Household", "Electronics", "Other"};
        for (int i = 0; i < cats.length; i++) {
            if (cats[i].equalsIgnoreCase(cat)) { spinnerCategory.setSelection(i); break; }
        }
    }

    // ── Button listeners ──────────────────────────────────────────────────────

    private void wireButtons() {
        btnCancelProduct.setOnClickListener(v -> dismiss());

        // ── Scan barcode button — launches ZXing from this Fragment ───────────
        if (btnScanBarcode != null) {
            btnScanBarcode.setOnClickListener(v ->
                    BarcodeHelper.startScanFromFragment(
                            this, BarcodeHelper.REQUEST_BARCODE));
        }

        btnSaveProduct.setOnClickListener(v -> {
            String name     = text(etProductName);
            String sellStr  = text(etSellingPrice);
            String stockStr = text(etStock);

            if (name.isEmpty()) {
                etProductName.setError(getString(R.string.product_name_required));
                etProductName.requestFocus(); return;
            }
            if (sellStr.isEmpty()) {
                etSellingPrice.setError(getString(R.string.selling_price_required));
                etSellingPrice.requestFocus(); return;
            }
            if (stockStr.isEmpty()) {
                etStock.setError(getString(R.string.stock_qty_required));
                etStock.requestFocus(); return;
            }

            Bundle b      = getArguments();
            boolean isEdit = (b != null && b.containsKey(KEY_ID));

            Product p        = new Product();
            p.id             = isEdit ? b.getString(KEY_ID) : UUID.randomUUID().toString();
            p.name           = name;
            p.emoji          = tvSelectedEmoji.getText().toString();
            p.category       = spinnerCategory.getSelectedItem().toString();
            p.sellingPrice   = parseDouble(etSellingPrice);
            p.costPrice      = parseDouble(etCostPrice);
            p.stock          = parseInt(etStock);
            p.lowStockAlert  = parseInt(etLowStockAlert) == 0 ? 5 : parseInt(etLowStockAlert);
            p.barcode        = text(etBarcode);

            if (savedListener != null) savedListener.onSaved(p, isEdit);
            dismiss();
        });
    }

    // ── Handle barcode scan result ────────────────────────────────────────────

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == BarcodeHelper.REQUEST_BARCODE && data != null) {
            String barcode = BarcodeHelper.parseResult(data);
            if (barcode != null && etBarcode != null) {
                etBarcode.setText(barcode);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setET(EditText et, String value) {
        if (et != null) et.setText(value);
    }

    private String text(EditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString().trim() : "";
    }

    private double parseDouble(EditText et) {
        try { return Double.parseDouble(text(et)); } catch (Exception e) { return 0; }
    }

    private int parseInt(EditText et) {
        try { return Integer.parseInt(text(et)); } catch (Exception e) { return 0; }
    }
}