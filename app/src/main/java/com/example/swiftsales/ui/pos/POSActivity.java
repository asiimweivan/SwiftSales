package com.example.swiftsales.ui.pos;

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

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.ProductRepository;
import com.example.swiftsales.model.CartItem;
import com.example.swiftsales.model.Product;
import com.example.swiftsales.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class POSActivity extends BaseActivity {

    // ── Views (same IDs as before) ───────────────────────────────────────────
    RecyclerView  recyclerProducts;
    LinearLayout  posEmptyState, cartBar;
    TextView      tvCartCount, tvCartItems, tvCartTotal, btnCheckout, btnBackPOS;
    EditText      etSearchProduct;
    TextView      posCatAll, posCatFood, posCatDrinks,
            posCatHousehold, posCatElectronics, posCatOther;

    // ── Data ─────────────────────────────────────────────────────────────────
    ProductRepository productRepo;
    POSProductAdapter productAdapter;
    List<Product>     productList     = new ArrayList<>();
    List<CartItem>    cart            = new ArrayList<>();
    String            currencyCode    = "RWF";
    String            currentCategory = "All";

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        productRepo = new ProductRepository(this);

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        String currency = prefs.getString("currency", "RWF");
        currencyCode = currency.length() >= 3 ? currency.substring(0, 3) : currency;

        bindViews();
        setupProductAdapter();
        setupCategoryTabs();
        setupSearch();
        loadProducts();

        btnBackPOS.setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> openCheckout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // refresh stock after returning from checkout
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerProducts  = findViewById(R.id.recyclerPOSProducts);
        posEmptyState     = findViewById(R.id.posEmptyState);
        cartBar           = findViewById(R.id.cartBar);
        tvCartCount       = findViewById(R.id.tvCartCount);
        tvCartItems       = findViewById(R.id.tvCartItems);
        tvCartTotal       = findViewById(R.id.tvCartTotal);
        btnCheckout       = findViewById(R.id.btnCheckout);
        btnBackPOS        = findViewById(R.id.btnBackPOS);
        etSearchProduct   = findViewById(R.id.etSearchProduct);
        posCatAll         = findViewById(R.id.posCatAll);
        posCatFood        = findViewById(R.id.posCatFood);
        posCatDrinks      = findViewById(R.id.posCatDrinks);
        posCatHousehold   = findViewById(R.id.posCatHousehold);
        posCatElectronics = findViewById(R.id.posCatElectronics);
        posCatOther       = findViewById(R.id.posCatOther);

        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupProductAdapter() {
        productAdapter = new POSProductAdapter(this, productList, currencyCode,
                product -> addToCart(product));
        recyclerProducts.setAdapter(productAdapter);
    }

    // ── Category tabs (identical logic) ──────────────────────────────────────

    private void setupCategoryTabs() {
        TextView[] tabs  = {posCatAll, posCatFood, posCatDrinks,
                posCatHousehold, posCatElectronics, posCatOther};
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

    // ── Search — off main thread ──────────────────────────────────────────────

    private void setupSearch() {
        etSearchProduct.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                String q = s.toString().trim();
                new AsyncTask<Void, Void, List<Product>>() {
                    @Override
                    protected List<Product> doInBackground(Void... v) {
                        List<Product> raw = q.isEmpty()
                                ? productRepo.getAll()
                                : productRepo.search(q);
                        List<Product> result = new ArrayList<>();
                        for (Product p : raw) if (!p.isOutOfStock()) result.add(p);
                        return result;
                    }
                    @Override
                    protected void onPostExecute(List<Product> result) {
                        productList.clear();
                        productList.addAll(result);
                        productAdapter.updateList(productList);
                        updateEmptyState();
                    }
                }.execute();
            }
        });
    }

    // ── Load products — off main thread ───────────────────────────────────────

    private void loadProducts() {
        new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... v) {
                List<Product> raw = currentCategory.equals("All")
                        ? productRepo.getAll()
                        : productRepo.getByCategory(currentCategory);
                List<Product> result = new ArrayList<>();
                for (Product p : raw) if (!p.isOutOfStock()) result.add(p);
                return result;
            }
            @Override
            protected void onPostExecute(List<Product> result) {
                productList.clear();
                productList.addAll(result);
                productAdapter.updateList(productList);
                updateEmptyState();
            }
        }.execute();
    }

    private void updateEmptyState() {
        boolean empty = productList.isEmpty();
        recyclerProducts.setVisibility(empty ? View.GONE    : View.VISIBLE);
        posEmptyState.setVisibility(empty    ? View.VISIBLE : View.GONE);
    }

    // ── Cart logic (unchanged) ────────────────────────────────────────────────

    private void addToCart(Product product) {
        for (CartItem item : cart) {
            if (item.product.id.equals(product.id)) {
                if (item.quantity < product.stock) {
                    item.quantity++;
                    updateCartBar();
                    Toast.makeText(this,
                            product.name + " qty updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            getString(R.string.not_enough_stock), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        cart.add(new CartItem(product, 1));
        updateCartBar();
        Toast.makeText(this, product.name + " " + getString(R.string.added_to_cart),
                Toast.LENGTH_SHORT).show();
    }

    private void updateCartBar() {
        if (cart.isEmpty()) {
            cartBar.setVisibility(View.GONE);
            tvCartCount.setText("0");
            return;
        }

        int    totalQty   = 0;
        double totalPrice = 0;
        for (CartItem item : cart) {
            totalQty   += item.quantity;
            totalPrice += item.getLineTotal();
        }

        tvCartCount.setText(String.valueOf(cart.size()));
        tvCartItems.setText(totalQty + " " + getString(R.string.items));
        tvCartTotal.setText(currencyCode + " " + String.format("%,.0f", totalPrice));
        cartBar.setVisibility(View.VISIBLE);
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    private void openCheckout() {
        if (cart.isEmpty()) {
            Toast.makeText(this, getString(R.string.cart_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        CartHolder.cart     = new ArrayList<>(cart);
        CartHolder.currency = currencyCode;
        startActivity(new Intent(this, CheckoutActivity.class));
    }
}