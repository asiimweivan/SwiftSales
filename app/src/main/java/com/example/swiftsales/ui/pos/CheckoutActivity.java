package com.example.swiftsales.ui.pos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.SaleRepository;
import com.example.swiftsales.model.CartItem;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.dashboard.DashboardActivity;

import java.util.List;

public class CheckoutActivity extends BaseActivity {

    // ── Views (same IDs as before) ───────────────────────────────────────────
    RecyclerView  recyclerCart;
    TextView      tvCheckoutItems, tvSubtotal, tvTaxLabel, tvTax,
            tvTotal, tvChange, btnConfirm, btnBackCheckout;
    TextView      btnPayCash, btnPayMobile, btnPayCard;
    LinearLayout  cashTenderedLayout;
    EditText      etCashTendered;

    // ── Data ─────────────────────────────────────────────────────────────────
    List<CartItem> cart;
    String         currencyCode;
    SaleRepository saleRepo;
    double         taxRate       = 0;
    String         paymentMethod = "Cash";
    double         totalAmount   = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cart         = CartHolder.cart;
        currencyCode = CartHolder.currency;
        saleRepo     = new SaleRepository(this);

        // Guard — if cart is null (e.g. process death) just go back
        if (cart == null || cart.isEmpty()) { finish(); return; }

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        String taxStr = prefs.getString("tax_rate", "0");
        try { taxRate = Double.parseDouble(taxStr); } catch (Exception e) { taxRate = 0; }

        bindViews();
        setupCartRecycler();
        calculateTotals();
        setupPaymentButtons();
        setupCashInput();

        btnBackCheckout.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> confirmSale());
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerCart       = findViewById(R.id.recyclerCartItems);
        tvCheckoutItems    = findViewById(R.id.tvCheckoutItems);
        tvSubtotal         = findViewById(R.id.tvSubtotal);
        tvTaxLabel         = findViewById(R.id.tvTaxLabel);
        tvTax              = findViewById(R.id.tvTax);
        tvTotal            = findViewById(R.id.tvTotal);
        tvChange           = findViewById(R.id.tvChange);
        btnConfirm         = findViewById(R.id.btnConfirmSale);
        btnBackCheckout    = findViewById(R.id.btnBackCheckout);
        btnPayCash         = findViewById(R.id.btnPayCash);
        btnPayMobile       = findViewById(R.id.btnPayMobile);
        btnPayCard         = findViewById(R.id.btnPayCard);
        cashTenderedLayout = findViewById(R.id.cashTenderedLayout);
        etCashTendered     = findViewById(R.id.etCashTendered);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupCartRecycler() {
        int totalQty = 0;
        for (CartItem item : cart) totalQty += item.quantity;
        tvCheckoutItems.setText(totalQty + " " + getString(R.string.items));
        recyclerCart.setAdapter(new CartAdapter());
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cart) subtotal += item.getLineTotal();
        double tax = subtotal * (taxRate / 100);
        totalAmount = subtotal + tax;

        tvSubtotal.setText(currencyCode + " " + String.format("%,.0f", subtotal));
        tvTaxLabel.setText(getString(R.string.tax_label) + " (" + (int) taxRate + "%)");
        tvTax.setText(currencyCode + " " + String.format("%,.0f", tax));
        tvTotal.setText(currencyCode + " " + String.format("%,.0f", totalAmount));
    }

    // ── Payment buttons (identical logic) ────────────────────────────────────

    private void setupPaymentButtons() {
        selectPayment("Cash");
        btnPayCash.setOnClickListener(v   -> selectPayment("Cash"));
        btnPayMobile.setOnClickListener(v -> { selectPayment("Mobile Money"); cashTenderedLayout.setVisibility(View.GONE); });
        btnPayCard.setOnClickListener(v   -> { selectPayment("Card");         cashTenderedLayout.setVisibility(View.GONE); });
    }

    private void selectPayment(String method) {
        paymentMethod = method;

        // Reset all to outline style
        btnPayCash.setBackgroundResource(R.drawable.btn_outline);
        btnPayCash.setTextColor(getResources().getColor(R.color.primary, null));
        btnPayMobile.setBackgroundResource(R.drawable.btn_outline);
        btnPayMobile.setTextColor(getResources().getColor(R.color.primary, null));
        btnPayCard.setBackgroundResource(R.drawable.btn_outline);
        btnPayCard.setTextColor(getResources().getColor(R.color.primary, null));

        switch (method) {
            case "Cash":
                btnPayCash.setBackgroundResource(R.drawable.btn_primary);
                btnPayCash.setTextColor(0xFFFFFFFF);
                cashTenderedLayout.setVisibility(View.VISIBLE);
                break;
            case "Mobile Money":
                btnPayMobile.setBackgroundResource(R.drawable.btn_primary);
                btnPayMobile.setTextColor(0xFFFFFFFF);
                break;
            case "Card":
                btnPayCard.setBackgroundResource(R.drawable.btn_primary);
                btnPayCard.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    private void setupCashInput() {
        etCashTendered.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                try {
                    double tendered = Double.parseDouble(s.toString().trim());
                    double change   = tendered - totalAmount;
                    tvChange.setText(currencyCode + " "
                            + String.format("%,.0f", Math.max(0, change)));
                    tvChange.setTextColor(getResources().getColor(
                            change >= 0 ? R.color.success : R.color.danger, null));
                } catch (Exception e) {
                    tvChange.setText(currencyCode + " 0");
                }
            }
        });
    }

    // ── Confirm sale — off main thread ────────────────────────────────────────

    private void confirmSale() {
        // Validate cash tendered first (on UI thread — no DB needed)
        if ("Cash".equals(paymentMethod)) {
            String tenderedStr = etCashTendered.getText().toString().trim();
            if (tenderedStr.isEmpty()) {
                etCashTendered.setError(getString(R.string.enter_amount_received));
                etCashTendered.requestFocus();
                return;
            }
            try {
                double tendered = Double.parseDouble(tenderedStr);
                if (tendered < totalAmount) {
                    Toast.makeText(this,
                            getString(R.string.amount_less_than_total),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                etCashTendered.setError(getString(R.string.invalid_amount));
                return;
            }
        }

        // Disable confirm button while saving
        btnConfirm.setEnabled(false);
        btnConfirm.setText(getString(R.string.saving));

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... v) {
                return saleRepo.saveSale(cart, taxRate, paymentMethod);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    CartHolder.cart = null;
                    Toast.makeText(CheckoutActivity.this,
                            getString(R.string.sale_completed), Toast.LENGTH_SHORT).show();

                    // Navigate cleanly back to Dashboard — no double finish()
                    Intent intent = new Intent(CheckoutActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText(getString(R.string.confirm_sale));
                    Toast.makeText(CheckoutActivity.this,
                            getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    // ── Cart RecyclerView Adapter (same logic, same IDs) ─────────────────────

    class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            CartItem item = cart.get(pos);
            h.tvEmoji.setText(item.product.emoji != null ? item.product.emoji : "📦");
            h.tvName.setText(item.product.name);
            h.tvUnitPrice.setText(currencyCode + " "
                    + String.format("%,.0f", item.product.sellingPrice) + " "
                    + getString(R.string.each));
            h.tvQty.setText(String.valueOf(item.quantity));
            h.tvLineTotal.setText(item.formattedTotal(currencyCode));

            h.btnIncrease.setOnClickListener(v -> {
                if (item.quantity < item.product.stock) {
                    item.quantity++;
                    notifyItemChanged(pos);
                    calculateTotals();
                } else {
                    Toast.makeText(CheckoutActivity.this,
                            getString(R.string.not_enough_stock), Toast.LENGTH_SHORT).show();
                }
            });

            h.btnDecrease.setOnClickListener(v -> {
                if (item.quantity > 1) {
                    item.quantity--;
                    notifyItemChanged(pos);
                    calculateTotals();
                } else {
                    cart.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, cart.size());
                    calculateTotals();
                    setupCartRecycler();
                    if (cart.isEmpty()) finish();
                }
            });
        }

        @Override public int getItemCount() { return cart.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvUnitPrice, tvQty, tvLineTotal, btnIncrease, btnDecrease;
            VH(View v) {
                super(v);
                tvEmoji     = v.findViewById(R.id.tvCartEmoji);
                tvName      = v.findViewById(R.id.tvCartName);
                tvUnitPrice = v.findViewById(R.id.tvCartUnitPrice);
                tvQty       = v.findViewById(R.id.tvQty);
                tvLineTotal = v.findViewById(R.id.tvCartLineTotal);
                btnIncrease = v.findViewById(R.id.btnIncrease);
                btnDecrease = v.findViewById(R.id.btnDecrease);
            }
        }
    }
}