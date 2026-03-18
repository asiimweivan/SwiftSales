package com.example.swiftsales.ui.inventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.ProductRepository;
import com.example.swiftsales.model.Product;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.auth.SignInActivity;
import com.example.swiftsales.ui.auth.SignUpActivity;
import com.example.swiftsales.ui.language.LanguageActivity;
import com.example.swiftsales.utils.BarcodeHelper;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends BaseActivity
        implements ProductAdapter.OnProductActionListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    RecyclerView  recyclerProducts;
    LinearLayout  emptyInventory;
    TextView      tvTotalProducts, tvLowStockCount, tvOutOfStock, tvStockValue;
    EditText      etSearch;
    TextView      btnAddProduct, btnAddFirstProduct, btnBackInv, btnSettings;
    TextView      btnScanSearch;   // NEW — scan barcode to search
    TextView      tabAll, tabFood, tabDrinks, tabHousehold, tabElectronics, tabOther;

    // ── Data ──────────────────────────────────────────────────────────────────
    ProductRepository repo;
    ProductAdapter    adapter;
    List<Product>     productList     = new ArrayList<>();
    String            currentCategory = "All";
    String            currencyCode    = "RWF";

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        repo = new ProductRepository(this);

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        String currency = prefs.getString("currency", "RWF");
        currencyCode = currency.length() >= 3 ? currency.substring(0, 3) : currency;

        bindViews();
        setupAdapter();
        setupCategoryTabs();
        setupSearch();
        setupClickListeners();
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerProducts   = findViewById(R.id.recyclerProducts);
        emptyInventory     = findViewById(R.id.emptyInventory);
        tvTotalProducts    = findViewById(R.id.tvTotalProducts);
        tvLowStockCount    = findViewById(R.id.tvLowStockCount);
        tvOutOfStock       = findViewById(R.id.tvOutOfStock);
        tvStockValue       = findViewById(R.id.tvStockValue);
        etSearch           = findViewById(R.id.etSearch);
        btnAddProduct      = findViewById(R.id.btnAddProduct);
        btnAddFirstProduct = findViewById(R.id.btnAddFirstProduct);
        btnBackInv         = findViewById(R.id.btnBackInv);
        btnSettings        = findViewById(R.id.btnSettings);
        btnScanSearch      = findViewById(R.id.btnScanSearch);   // NEW
        tabAll             = findViewById(R.id.tabAll);
        tabFood            = findViewById(R.id.tabFood);
        tabDrinks          = findViewById(R.id.tabDrinks);
        tabHousehold       = findViewById(R.id.tabHousehold);
        tabElectronics     = findViewById(R.id.tabElectronics);
        tabOther           = findViewById(R.id.tabOther);

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapter() {
        adapter = new ProductAdapter(this, productList, currencyCode, this);
        recyclerProducts.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBackInv.setOnClickListener(v -> finish());
        btnAddProduct.setOnClickListener(v -> openProductSheet(null));
        btnAddFirstProduct.setOnClickListener(v -> openProductSheet(null));
        btnSettings.setOnClickListener(v -> showSettingsMenu());

        // ── Scan to search — launches ZXing, result fills etSearch ────────────
        if (btnScanSearch != null) {
            btnScanSearch.setOnClickListener(v ->
                    BarcodeHelper.startScan(this, BarcodeHelper.REQUEST_BARCODE_SEARCH));
        }
    }

    // ── Handle scan results (both search scan and any launched by sheet) ──────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BarcodeHelper.REQUEST_BARCODE_SEARCH && data != null) {
            // Scan from search bar — fill etSearch and trigger search
            String barcode = BarcodeHelper.parseResult(data);
            if (barcode != null) {
                etSearch.setText(barcode);
                etSearch.setSelection(barcode.length());
                new SearchTask(barcode).execute();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ── Category tabs ─────────────────────────────────────────────────────────

    private void setupCategoryTabs() {
        TextView[] tabs  = {tabAll, tabFood, tabDrinks, tabHousehold, tabElectronics, tabOther};
        String[]   names = {"All", "Food", "Drinks", "Household", "Electronics", "Other"};

        for (int i = 0; i < tabs.length; i++) {
            final String   cat = names[i];
            final TextView tab = tabs[i];
            tab.setOnClickListener(v -> {
                currentCategory = cat;
                for (TextView t : tabs) {
                    t.setBackgroundResource(R.drawable.tab_inactive_bg);
                    t.setTextColor(getResources().getColor(R.color.ink_medium, null));
                }
                tab.setBackgroundResource(R.drawable.tab_active_bg);
                tab.setTextColor(0xFFFFFFFF);
                loadProducts();
            });
        }
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                new SearchTask(s.toString().trim()).execute();
            }
        });
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    void loadProducts() {
        new LoadTask().execute();
    }

    private class LoadTask extends AsyncTask<Void, Void, List<Product>> {
        @Override
        protected List<Product> doInBackground(Void... v) {
            return currentCategory.equals("All")
                    ? repo.getAll() : repo.getByCategory(currentCategory);
        }
        @Override
        protected void onPostExecute(List<Product> result) {
            productList.clear();
            productList.addAll(result);
            adapter.updateList(productList);
            updateStats();
            updateEmptyState();
        }
    }

    private class SearchTask extends AsyncTask<Void, Void, List<Product>> {
        private final String query;
        SearchTask(String q) { this.query = q; }
        @Override
        protected List<Product> doInBackground(Void... v) {
            return query.isEmpty() ? repo.getAll() : repo.search(query);
        }
        @Override
        protected void onPostExecute(List<Product> result) {
            productList.clear();
            productList.addAll(result);
            adapter.updateList(productList);
            updateEmptyState();
        }
    }

    // ── Stats & empty state ───────────────────────────────────────────────────

    private void updateStats() {
        new AsyncTask<Void, Void, int[]>() {
            double stockVal;
            @Override
            protected int[] doInBackground(Void... v) {
                stockVal = repo.getTotalStockValue();
                return new int[]{
                        repo.getTotalCount(),
                        repo.getLowStockCount(),
                        repo.getOutOfStockCount()
                };
            }
            @Override
            protected void onPostExecute(int[] counts) {
                tvTotalProducts.setText(String.valueOf(counts[0]));
                tvLowStockCount.setText(String.valueOf(counts[1]));
                tvOutOfStock.setText(String.valueOf(counts[2]));
                tvStockValue.setText(stockVal >= 1_000_000
                        ? String.format("%.1fM", stockVal / 1_000_000)
                        : stockVal >= 1_000
                        ? String.format("%.0fK", stockVal / 1_000)
                        : String.format("%.0f", stockVal));
            }
        }.execute();
    }

    private void updateEmptyState() {
        boolean empty = productList.isEmpty();
        recyclerProducts.setVisibility(empty ? View.GONE    : View.VISIBLE);
        emptyInventory.setVisibility(empty   ? View.VISIBLE : View.GONE);
    }

    // ── ProductAdapter.OnProductActionListener ────────────────────────────────

    @Override
    public void onEdit(Product product) { openProductSheet(product); }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_product_title))
                .setMessage("\"" + product.name + "\" "
                        + getString(R.string.delete_product_msg))
                .setPositiveButton(getString(R.string.delete), (d, w) ->
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override protected Boolean doInBackground(Void... v) {
                                return repo.delete(product.id);
                            }
                            @Override protected void onPostExecute(Boolean ok) {
                                Toast.makeText(InventoryActivity.this,
                                        ok ? getString(R.string.product_deleted)
                                                : getString(R.string.delete_failed),
                                        Toast.LENGTH_SHORT).show();
                                loadProducts();
                            }
                        }.execute())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onAdjustStock(Product product) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_adjust_stock);
        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvProductName  = dialog.findViewById(R.id.tvAdjustProductName);
        TextView tvCurrentStock = dialog.findViewById(R.id.tvCurrentStock);
        EditText etNewStock     = dialog.findViewById(R.id.etNewStock);
        TextView btnSaveStock   = dialog.findViewById(R.id.btnSaveStock);
        TextView btnCancelStock = dialog.findViewById(R.id.btnCancelStock);

        tvProductName.setText(product.name);
        tvCurrentStock.setText(getString(R.string.current_stock_label) + " " + product.stock);
        etNewStock.setText(String.valueOf(product.stock));
        etNewStock.selectAll();

        btnCancelStock.setOnClickListener(v -> dialog.dismiss());
        btnSaveStock.setOnClickListener(v -> {
            String raw = etNewStock.getText().toString().trim();
            if (raw.isEmpty()) { etNewStock.setError(getString(R.string.enter_a_number)); return; }
            int newStock;
            try { newStock = Integer.parseInt(raw); }
            catch (Exception e) { etNewStock.setError(getString(R.string.invalid_number)); return; }
            if (newStock < 0) { etNewStock.setError(getString(R.string.cannot_be_negative)); return; }

            new AsyncTask<Void, Void, Boolean>() {
                @Override protected Boolean doInBackground(Void... v) {
                    return repo.updateStock(product.id, newStock);
                }
                @Override protected void onPostExecute(Boolean ok) {
                    Toast.makeText(InventoryActivity.this,
                            ok ? getString(R.string.stock_updated)
                                    : getString(R.string.update_failed),
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadProducts();
                }
            }.execute();
        });

        dialog.show();
    }

    // ── Add / Edit product sheet ──────────────────────────────────────────────

    private void openProductSheet(Product editProduct) {
        AddEditProductSheet sheet = AddEditProductSheet.newInstance(editProduct, currencyCode);
        sheet.setOnSavedListener((product, isEdit) ->
                new AsyncTask<Void, Void, Boolean>() {
                    @Override protected Boolean doInBackground(Void... v) {
                        return isEdit ? repo.update(product) : repo.add(product);
                    }
                    @Override protected void onPostExecute(Boolean ok) {
                        Toast.makeText(InventoryActivity.this,
                                ok ? (isEdit ? getString(R.string.product_updated)
                                        : getString(R.string.product_added))
                                        : getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT).show();
                        if (ok) loadProducts();
                    }
                }.execute()
        );
        sheet.show(getSupportFragmentManager(), "product_sheet");
    }

    // ── Settings menu ─────────────────────────────────────────────────────────

    private void showSettingsMenu() {
        String[] options = {
                getString(R.string.menu_change_language),
                getString(R.string.menu_sign_in_different),
                getString(R.string.menu_create_account),
                getString(R.string.menu_go_dashboard),
                getString(R.string.menu_cancel)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.settings_nav_title))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startActivity(new Intent(this, LanguageActivity.class));
                            break;
                        case 1:
                            new AlertDialog.Builder(this)
                                    .setTitle(getString(R.string.sign_out_title))
                                    .setMessage(getString(R.string.sign_out_msg))
                                    .setPositiveButton(getString(R.string.sign_out), (d, w) -> {
                                        getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE)
                                                .edit().putBoolean("is_logged_in", false).apply();
                                        startActivity(new Intent(this, SignInActivity.class));
                                        finishAffinity();
                                    })
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .show();
                            break;
                        case 2:
                            new AlertDialog.Builder(this)
                                    .setTitle(getString(R.string.create_account_title))
                                    .setMessage(getString(R.string.create_account_msg))
                                    .setPositiveButton(getString(R.string.yes_continue), (d, w) -> {
                                        getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE)
                                                .edit().clear().apply();
                                        startActivity(new Intent(this, SignUpActivity.class));
                                        finishAffinity();
                                    })
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .show();
                            break;
                        case 3:
                            finish();
                            break;
                        case 4:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }
}