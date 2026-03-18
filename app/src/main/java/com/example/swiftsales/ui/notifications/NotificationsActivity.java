package com.example.swiftsales.ui.notifications;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.OrderRepository;
import com.example.swiftsales.data.repository.ProductRepository;
import com.example.swiftsales.data.repository.SaleRepository;
import com.example.swiftsales.data.repository.StaffRepository;
import com.example.swiftsales.model.Product;
import com.example.swiftsales.model.Staff;
import com.example.swiftsales.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends BaseActivity {

    TextView     btnBackNotifications, tvNotifCount;
    RecyclerView recyclerNotifications;
    LinearLayout emptyNotifications;

    ProductRepository productRepo;
    StaffRepository   staffRepo;
    OrderRepository   orderRepo;
    SaleRepository    saleRepo;
    SharedPreferences prefs;
    String            currencyCode = "RWF";

    public static class NotifItem {
        public static final int TYPE_LOW_STOCK      = 0;
        public static final int TYPE_ORDER_PENDING  = 1;
        public static final int TYPE_STAFF_INACTIVE = 2;
        public static final int TYPE_DAILY_SUMMARY  = 3;

        public int    type;
        public String emoji;
        public String title;
        public String subtitle;
        public String badgeText;
        public int    badgeDrawable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        prefs       = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        productRepo = new ProductRepository(this);
        staffRepo   = new StaffRepository(this);
        orderRepo   = new OrderRepository(this);
        saleRepo    = new SaleRepository(this);

        String currency = prefs.getString("currency", "RWF");
        currencyCode = currency.length() >= 3 ? currency.substring(0, 3) : currency;

        btnBackNotifications  = findViewById(R.id.btnBackNotifications);
        tvNotifCount          = findViewById(R.id.tvNotifCount);
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        emptyNotifications    = findViewById(R.id.emptyNotifications);

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        btnBackNotifications.setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        new AsyncTask<Void, Void, List<NotifItem>>() {
            @Override
            protected List<NotifItem> doInBackground(Void... v) {
                List<NotifItem> list = new ArrayList<>();

                // ── 1. Daily sales summary ────────────────────────────────────
                double todayRevenue = saleRepo.getTodayTotal();
                int    todayOrders  = saleRepo.getTodayOrderCount();
                double todayProfit  = saleRepo.getTodayProfit();

                NotifItem summary   = new NotifItem();
                summary.type        = NotifItem.TYPE_DAILY_SUMMARY;
                summary.emoji       = "📊";
                summary.title       = getString(R.string.notif_daily_summary_title);
                summary.subtitle    = getString(R.string.notif_daily_summary_sub,
                        todayOrders,
                        currencyCode + " " + String.format("%,.0f", todayRevenue),
                        currencyCode + " " + String.format("%,.0f", todayProfit));
                summary.badgeText   = getString(R.string.notif_today);
                summary.badgeDrawable = R.drawable.badge_primary;
                list.add(summary);

                // ── 2. Pending orders ─────────────────────────────────────────
                int pendingOrders = orderRepo.getPendingCount();
                if (pendingOrders > 0) {
                    NotifItem item    = new NotifItem();
                    item.type         = NotifItem.TYPE_ORDER_PENDING;
                    item.emoji        = "📋";
                    item.title        = getString(R.string.notif_pending_orders_title);
                    item.subtitle     = getString(R.string.notif_pending_orders_sub, pendingOrders);
                    item.badgeText    = String.valueOf(pendingOrders);
                    item.badgeDrawable = R.drawable.badge_warning;
                    list.add(item);
                }

                // ── 3. Low stock — uses getLowStock() from ProductRepository ──
                List<Product> lowStock = productRepo.getLowStock();  // ← FIXED
                for (Product p : lowStock) {
                    NotifItem item    = new NotifItem();
                    item.type         = NotifItem.TYPE_LOW_STOCK;
                    item.emoji        = p.emoji != null ? p.emoji : "📦";
                    item.title        = getString(R.string.notif_low_stock_title, p.name);
                    item.subtitle     = getString(R.string.notif_low_stock_sub, p.stock);
                    item.badgeText    = getString(R.string.low_stock);
                    item.badgeDrawable = R.drawable.badge_warning;
                    list.add(item);
                }

                // ── 4. Inactive staff ─────────────────────────────────────────
                List<Staff> allStaff = staffRepo.getAll();
                for (Staff s : allStaff) {
                    if (!s.isActive) {
                        NotifItem item    = new NotifItem();
                        item.type         = NotifItem.TYPE_STAFF_INACTIVE;
                        item.emoji        = "👤";
                        item.title        = getString(R.string.notif_staff_inactive_title, s.name);
                        item.subtitle     = getString(R.string.notif_staff_inactive_sub);
                        item.badgeText    = getString(R.string.tab_inactive);
                        item.badgeDrawable = R.drawable.badge_danger;
                        list.add(item);
                    }
                }

                return list;
            }

            @Override
            protected void onPostExecute(List<NotifItem> items) {
                tvNotifCount.setText(items.size() + " " +
                        getString(items.size() == 1
                                ? R.string.notif_singular
                                : R.string.notif_plural));

                if (items.isEmpty()) {
                    recyclerNotifications.setVisibility(View.GONE);
                    emptyNotifications.setVisibility(View.VISIBLE);
                } else {
                    recyclerNotifications.setVisibility(View.VISIBLE);
                    emptyNotifications.setVisibility(View.GONE);
                    recyclerNotifications.setAdapter(new NotifAdapter(items));
                }
            }
        }.execute();
    }

    class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {
        private final List<NotifItem> items;
        NotifAdapter(List<NotifItem> items) { this.items = items; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            NotifItem item = items.get(pos);
            h.tvEmoji.setText(item.emoji);
            h.tvTitle.setText(item.title);
            h.tvSubtitle.setText(item.subtitle);
            h.tvBadge.setText(item.badgeText);
            if (item.badgeDrawable != 0)
                h.tvBadge.setBackgroundResource(item.badgeDrawable);
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvSubtitle, tvBadge;
            VH(View v) {
                super(v);
                tvEmoji    = v.findViewById(R.id.tvNotifEmoji);
                tvTitle    = v.findViewById(R.id.tvNotifTitle);
                tvSubtitle = v.findViewById(R.id.tvNotifSubtitle);
                tvBadge    = v.findViewById(R.id.tvNotifBadge);
            }
        }
    }
}