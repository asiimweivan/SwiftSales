package com.example.swiftsales.ui.orders;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.OrderRepository;
import com.example.swiftsales.model.Order;
import com.example.swiftsales.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends BaseActivity
        implements OrderAdapter.OnOrderActionListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    RecyclerView recyclerOrders;
    LinearLayout emptyOrders;
    TextView     tvTotalOrders, tvPendingOrders, tvFulfilledOrders;
    TextView     btnAddOrder, btnBackOrders;
    TextView     tabAll, tabPending, tabFulfilled, tabCancelled;

    // ── Data ──────────────────────────────────────────────────────────────────
    OrderRepository  repo;
    OrderAdapter     adapter;
    List<Order>      orderList     = new ArrayList<>();
    String           currentFilter = "All";
    String           currencyCode  = "RWF";

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        repo = new OrderRepository(this);

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        String currency = prefs.getString("currency", "RWF");
        currencyCode = currency.length() >= 3 ? currency.substring(0, 3) : currency;

        bindViews();
        setupAdapter();
        setupFilterTabs();
        loadOrders();

        btnBackOrders.setOnClickListener(v -> finish());
        btnAddOrder.setOnClickListener(v -> openAddOrderSheet());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerOrders    = findViewById(R.id.recyclerOrders);
        emptyOrders       = findViewById(R.id.emptyOrders);
        tvTotalOrders     = findViewById(R.id.tvTotalOrders);
        tvPendingOrders   = findViewById(R.id.tvPendingOrders);
        tvFulfilledOrders = findViewById(R.id.tvFulfilledOrders);
        btnAddOrder       = findViewById(R.id.btnAddOrder);
        btnBackOrders     = findViewById(R.id.btnBackOrders);
        tabAll            = findViewById(R.id.tabAllOrders);
        tabPending        = findViewById(R.id.tabPendingOrders);
        tabFulfilled      = findViewById(R.id.tabFulfilledOrders);
        tabCancelled      = findViewById(R.id.tabCancelledOrders);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapter() {
        adapter = new OrderAdapter(orderList, currencyCode, this);
        recyclerOrders.setAdapter(adapter);
    }

    private void setupFilterTabs() {
        TextView[] tabs    = {tabAll, tabPending, tabFulfilled, tabCancelled};
        String[]   filters = {"All", Order.STATUS_PENDING,
                Order.STATUS_FULFILLED, Order.STATUS_CANCELLED};

        for (int i = 0; i < tabs.length; i++) {
            final String   filter = filters[i];
            final TextView tab    = tabs[i];
            tab.setOnClickListener(v -> {
                currentFilter = filter;
                for (TextView t : tabs) {
                    t.setBackgroundResource(R.drawable.tab_inactive_bg);
                    t.setTextColor(getResources().getColor(R.color.ink_medium, null));
                }
                tab.setBackgroundResource(R.drawable.tab_active_bg);
                tab.setTextColor(0xFFFFFFFF);
                loadOrders();
            });
        }
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadOrders() {
        new AsyncTask<Void, Void, List<Order>>() {
            @Override
            protected List<Order> doInBackground(Void... v) {
                return currentFilter.equals("All")
                        ? repo.getAll()
                        : repo.getByStatus(currentFilter);
            }
            @Override
            protected void onPostExecute(List<Order> result) {
                orderList.clear();
                orderList.addAll(result);
                adapter.updateList(orderList);
                updateEmptyState();
                loadStats();
            }
        }.execute();
    }

    private void loadStats() {
        new AsyncTask<Void, Void, int[]>() {
            @Override
            protected int[] doInBackground(Void... v) {
                List<Order> all = repo.getAll();
                int total = all.size(), pending = 0, fulfilled = 0;
                for (Order o : all) {
                    if (Order.STATUS_PENDING.equals(o.status))   pending++;
                    if (Order.STATUS_FULFILLED.equals(o.status)) fulfilled++;
                }
                return new int[]{total, pending, fulfilled};
            }
            @Override
            protected void onPostExecute(int[] counts) {
                tvTotalOrders.setText(String.valueOf(counts[0]));
                tvPendingOrders.setText(String.valueOf(counts[1]));
                tvFulfilledOrders.setText(String.valueOf(counts[2]));
            }
        }.execute();
    }

    private void updateEmptyState() {
        boolean empty = orderList.isEmpty();
        recyclerOrders.setVisibility(empty ? View.GONE    : View.VISIBLE);
        emptyOrders.setVisibility(empty   ? View.VISIBLE : View.GONE);
    }

    // ── OrderAdapter.OnOrderActionListener ───────────────────────────────────

    @Override
    public void onFulfil(Order order) {
        String[] payMethods = {
                getString(R.string.pay_cash),
                getString(R.string.pay_mobile),
                getString(R.string.pay_card)
        };
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.fulfil_order_title))
                .setMessage(getString(R.string.fulfil_order_msg, order.customerName))
                .setSingleChoiceItems(payMethods, 0, null)
                .setPositiveButton(getString(R.string.fulfil_confirm), (d, w) -> {
                    int sel = ((AlertDialog) d).getListView().getCheckedItemPosition();
                    String[] methods = {"Cash", "Mobile Money", "Card"};
                    String method = methods[sel];

                    SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
                    double taxRate;
                    try { taxRate = Double.parseDouble(
                            prefs.getString("tax_rate", "0")); }
                    catch (Exception e) { taxRate = 0; }

                    final double finalTax = taxRate;
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override protected Boolean doInBackground(Void... v) {
                            return repo.fulfil(order, finalTax, method);
                        }
                        @Override protected void onPostExecute(Boolean ok) {
                            Toast.makeText(OrdersActivity.this,
                                    ok ? getString(R.string.order_fulfilled)
                                            : getString(R.string.something_went_wrong),
                                    Toast.LENGTH_SHORT).show();
                            loadOrders();
                        }
                    }.execute();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onCancel(Order order) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cancel_order_title))
                .setMessage(getString(R.string.cancel_order_msg, order.customerName))
                .setPositiveButton(getString(R.string.yes), (d, w) ->
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override protected Boolean doInBackground(Void... v) {
                                return repo.cancel(order.id);
                            }
                            @Override protected void onPostExecute(Boolean ok) {
                                Toast.makeText(OrdersActivity.this,
                                        getString(R.string.order_cancelled),
                                        Toast.LENGTH_SHORT).show();
                                loadOrders();
                            }
                        }.execute())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onDelete(Order order) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_order_title))
                .setMessage(getString(R.string.delete_order_msg, order.customerName))
                .setPositiveButton(getString(R.string.delete), (d, w) ->
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override protected Boolean doInBackground(Void... v) {
                                return repo.delete(order.id);
                            }
                            @Override protected void onPostExecute(Boolean ok) {
                                loadOrders();
                            }
                        }.execute())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    // ── Add order sheet ───────────────────────────────────────────────────────

    private void openAddOrderSheet() {
        AddOrderSheet sheet = new AddOrderSheet();
        sheet.setOnSavedListener(order ->
                new AsyncTask<Void, Void, Boolean>() {
                    @Override protected Boolean doInBackground(Void... v) {
                        return repo.add(order);
                    }
                    @Override protected void onPostExecute(Boolean ok) {
                        Toast.makeText(OrdersActivity.this,
                                ok ? getString(R.string.order_added)
                                        : getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT).show();
                        if (ok) loadOrders();
                    }
                }.execute());
        sheet.show(getSupportFragmentManager(), "add_order");
    }
}