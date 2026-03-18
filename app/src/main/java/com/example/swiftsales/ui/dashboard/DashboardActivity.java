package com.example.swiftsales.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.OrderRepository;
import com.example.swiftsales.data.repository.ProductRepository;
import com.example.swiftsales.data.repository.SaleRepository;
import com.example.swiftsales.data.repository.StaffRepository;
import com.example.swiftsales.model.Sale;
import com.example.swiftsales.ui.BaseActivity;
import com.example.swiftsales.ui.inventory.InventoryActivity;
import com.example.swiftsales.ui.notifications.NotificationsActivity;
import com.example.swiftsales.ui.orders.OrdersActivity;
import com.example.swiftsales.ui.pos.POSActivity;
import com.example.swiftsales.ui.profile.ProfileActivity;
import com.example.swiftsales.ui.reports.ReportsActivity;
import com.example.swiftsales.ui.settings.SettingsActivity;
import com.example.swiftsales.ui.staff.StaffActivity;

import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    TextView     tvGreeting, tvUserName, tvBusinessName, tvAvatar;
    TextView     tvBell;                      // 🔔 → NotificationsActivity
    TextView     tvRevenue, tvTrend;
    TextView     tvOrdersCount, tvOrdersTrend;
    TextView     tvProductsCount, tvLowStock;
    TextView     tvStaffCount;
    TextView     tvProfitAmount, tvProfitMargin;
    TextView     btnNewSale, btnAddProduct, btnViewReports;
    TextView     btnRegisterOrder;            // 📋 → OrdersActivity
    TextView     tvViewAll, btnStartSelling;
    LinearLayout emptyState, salesContainer;
    LinearLayout tabDashboard, tabPOS, tabInventory, tabReports, tabStaff, tabSettings;

    // ── Recent sales ──────────────────────────────────────────────────────────
    RecyclerView      recyclerRecentSales;
    RecentSaleAdapter recentSaleAdapter;

    // ── Repos ─────────────────────────────────────────────────────────────────
    ProductRepository productRepo;
    SaleRepository    saleRepo;
    StaffRepository   staffRepo;
    OrderRepository   orderRepo;             // NEW — for pending count

    SharedPreferences prefs;
    String            currencyCode = "RWF";

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        prefs       = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        productRepo = new ProductRepository(this);
        saleRepo    = new SaleRepository(this);
        staffRepo   = new StaffRepository(this);
        orderRepo   = new OrderRepository(this);

        bindViews();
        setupRecentSales();
        loadUserData();
        setupGreeting();
        setupQuickActions();
        setupBottomNav();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { moveTaskToBack(true); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLiveStats();
    }

    // ── Bind views ────────────────────────────────────────────────────────────

    private void bindViews() {
        tvGreeting      = findViewById(R.id.tvGreeting);
        tvUserName      = findViewById(R.id.tvUserName);
        tvBusinessName  = findViewById(R.id.tvBusinessName);
        tvAvatar        = findViewById(R.id.tvAvatar);
        tvBell          = findViewById(R.id.tvBell);          // bell TextView
        tvRevenue       = findViewById(R.id.tvRevenue);
        tvTrend         = findViewById(R.id.tvTrend);
        tvOrdersCount   = findViewById(R.id.tvOrdersCount);
        tvOrdersTrend   = findViewById(R.id.tvOrdersTrend);
        tvProductsCount = findViewById(R.id.tvProductsCount);
        tvLowStock      = findViewById(R.id.tvLowStock);
        tvStaffCount    = findViewById(R.id.tvStaffCount);
        tvProfitAmount  = findViewById(R.id.tvProfitAmount);
        tvProfitMargin  = findViewById(R.id.tvProfitMargin);
        btnNewSale      = findViewById(R.id.btnNewSale);
        btnAddProduct   = findViewById(R.id.btnAddProduct);
        btnViewReports  = findViewById(R.id.btnViewReports);
        btnRegisterOrder = findViewById(R.id.btnRegisterOrder); // orders button
        tvViewAll       = findViewById(R.id.tvViewAll);
        btnStartSelling = findViewById(R.id.btnStartSelling);
        emptyState      = findViewById(R.id.emptyState);
        salesContainer  = findViewById(R.id.salesContainer);
        tabDashboard    = findViewById(R.id.tabDashboard);
        tabPOS          = findViewById(R.id.tabPOS);
        tabInventory    = findViewById(R.id.tabInventory);
        tabReports      = findViewById(R.id.tabReports);
        tabStaff        = findViewById(R.id.tabStaff);
        tabSettings     = findViewById(R.id.tabSettings);
    }

    // ── Recent sales RecyclerView ─────────────────────────────────────────────

    private void setupRecentSales() {
        recyclerRecentSales = new RecyclerView(this);
        recyclerRecentSales.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        recyclerRecentSales.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentSales.setNestedScrollingEnabled(false);
        recentSaleAdapter = new RecentSaleAdapter(this, currencyCode);
        recyclerRecentSales.setAdapter(recentSaleAdapter);
        salesContainer.addView(recyclerRecentSales);
    }

    // ── User data ─────────────────────────────────────────────────────────────

    private void loadUserData() {
        String userName     = prefs.getString("user_name",     "User");
        String businessName = prefs.getString("business_name", "My Business");

        tvUserName.setText(userName);
        tvBusinessName.setText(businessName);

        String[] parts  = userName.split(" ");
        String initials = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
                : userName.substring(0, Math.min(2, userName.length())).toUpperCase();
        tvAvatar.setText(initials);

        tvTrend.setText("↑ 0%");
        tvProfitMargin.setText("0% " + getString(R.string.margin));
        tvOrdersTrend.setText(getString(R.string.vs_yesterday));

        emptyState.setVisibility(View.VISIBLE);
        salesContainer.setVisibility(View.GONE);
    }

    // ── Live stats — off main thread ──────────────────────────────────────────

    private void loadLiveStats() {
        String currency = prefs.getString("currency", "RWF");
        currencyCode = currency.length() >= 3 ? currency.substring(0, 3) : currency;

        new AsyncTask<Void, Void, DashboardData>() {
            @Override
            protected DashboardData doInBackground(Void... v) {
                DashboardData d = new DashboardData();
                d.currencyCode  = currencyCode;
                d.revenue       = saleRepo.getTodayTotal();
                d.orders        = saleRepo.getTodayOrderCount();
                d.profit        = saleRepo.getTodayProfit();
                d.products      = productRepo.getTotalCount();
                d.lowStock      = productRepo.getLowStockCount();
                d.activeStaff   = staffRepo.getActiveCount();
                d.pendingOrders = orderRepo.getPendingCount();
                d.recentSales   = saleRepo.getRecentSales(5);
                return d;
            }

            @Override
            protected void onPostExecute(DashboardData d) {
                recentSaleAdapter.setCurrency(d.currencyCode);

                tvRevenue.setText(d.currencyCode + " " + String.format("%,.0f", d.revenue));
                tvOrdersCount.setText(String.valueOf(d.orders));
                tvProductsCount.setText(String.valueOf(d.products));
                tvLowStock.setText(d.lowStock + " " + getString(R.string.low_stock));
                tvProfitAmount.setText(String.format("%,.0f", d.profit));
                tvStaffCount.setText(String.valueOf(d.activeStaff));

                // ── Bell badge — shows pending order count ────────────────────
                if (d.pendingOrders > 0) {
                    tvBell.setText("🔔 " + d.pendingOrders);
                } else {
                    tvBell.setText("🔔");
                }

                if (d.orders > 0 && d.recentSales != null && !d.recentSales.isEmpty()) {
                    recentSaleAdapter.setSales(d.recentSales);
                    emptyState.setVisibility(View.GONE);
                    salesContainer.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.VISIBLE);
                    salesContainer.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    // ── Greeting ──────────────────────────────────────────────────────────────

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if      (hour >= 5  && hour < 12) greeting = getString(R.string.good_morning);
        else if (hour >= 12 && hour < 17) greeting = getString(R.string.good_afternoon);
        else if (hour >= 17 && hour < 21) greeting = getString(R.string.good_evening);
        else                              greeting = getString(R.string.working_late);
        tvGreeting.setText(greeting);
    }

    // ── Quick actions — ALL 5 wired including NEW ones ────────────────────────

    private void setupQuickActions() {
        btnNewSale.setOnClickListener(v ->
                startActivity(new Intent(this, POSActivity.class)));
        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
        btnViewReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));
        btnStartSelling.setOnClickListener(v ->
                startActivity(new Intent(this, POSActivity.class)));
        tvViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        // ── NEW: Orders button ────────────────────────────────────────────────
        if (btnRegisterOrder != null) {
            btnRegisterOrder.setOnClickListener(v ->
                    startActivity(new Intent(this, OrdersActivity.class)));
        }

        // ── NEW: Bell → Notifications ─────────────────────────────────────────
        if (tvBell != null) {
            tvBell.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationsActivity.class)));
        }

        // ── NEW: Avatar → Profile ─────────────────────────────────────────────
        if (tvAvatar != null) {
            tvAvatar.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    // ── Bottom nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        setTabActive(tabDashboard);
        tabDashboard.setOnClickListener(v -> setTabActive(tabDashboard));
        tabPOS.setOnClickListener(v -> {
            setTabActive(tabPOS);
            startActivity(new Intent(this, POSActivity.class));
        });
        tabInventory.setOnClickListener(v -> {
            setTabActive(tabInventory);
            startActivity(new Intent(this, InventoryActivity.class));
        });
        tabReports.setOnClickListener(v -> {
            setTabActive(tabReports);
            startActivity(new Intent(this, ReportsActivity.class));
        });
        tabStaff.setOnClickListener(v -> {
            setTabActive(tabStaff);
            startActivity(new Intent(this, StaffActivity.class));
        });
        tabSettings.setOnClickListener(v -> {
            setTabActive(tabSettings);
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void setTabActive(LinearLayout activeTab) {
        LinearLayout[] allTabs = {
                tabDashboard, tabPOS, tabInventory,
                tabReports,   tabStaff, tabSettings
        };
        for (LinearLayout tab : allTabs) {
            TextView label = (TextView) tab.getChildAt(1);
            if (label != null)
                label.setTextColor(getResources().getColor(R.color.ink_soft, null));
        }
        TextView activeLabel = (TextView) activeTab.getChildAt(1);
        if (activeLabel != null)
            activeLabel.setTextColor(getResources().getColor(R.color.primary, null));
    }

    // ── Data holder ───────────────────────────────────────────────────────────

    private static class DashboardData {
        String     currencyCode;
        double     revenue, profit;
        int        orders, products, lowStock, activeStaff, pendingOrders;
        List<Sale> recentSales;
    }
}