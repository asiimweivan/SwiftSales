package com.example.swiftsales.ui.reports;

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
import com.example.swiftsales.data.repository.SaleRepository;
import com.example.swiftsales.model.Sale;
import com.example.swiftsales.ui.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends BaseActivity {

    // ── Views (same IDs as before) ───────────────────────────────────────────
    TextView     btnBackReports, tvReportDate;
    TextView     tabToday, tabWeek, tabMonth, tabAll;
    TextView     tvKpiRevenue, tvKpiProfit, tvKpiOrders, tvKpiAvg;
    View         barCash, barMobile, barCard;
    TextView     tvCashPct, tvMobilePct, tvCardPct;
    RecyclerView recyclerSales;
    LinearLayout emptySales;
    TextView     tvSaleCount;

    // ── Data ─────────────────────────────────────────────────────────────────
    SaleRepository saleRepo;
    String         currencyCode  = "RWF";
    String         currentPeriod = "Today";

    // ── Result holder for background load ────────────────────────────────────
    private static class ReportData {
        double     revenue, profit, avg;
        int        orders;
        int        cashCount, mobileCount, cardCount;
        List<Sale> sales;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        saleRepo = new SaleRepository(this);

        SharedPreferences prefs = getSharedPreferences("SwiftSalesAuth", MODE_PRIVATE);
        String currency = prefs.getString("currency", "RWF");
        currencyCode = currency.length() >= 3 ? currency.substring(0, 3) : currency;

        bindViews();
        setupPeriodTabs();
        loadReport("Today");

        btnBackReports.setOnClickListener(v -> finish());

        tvReportDate.setText(new SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault()).format(new Date()));

        recyclerSales.setLayoutManager(new LinearLayoutManager(this));
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        btnBackReports = findViewById(R.id.btnBackReports);
        tvReportDate   = findViewById(R.id.tvReportDate);
        tabToday       = findViewById(R.id.tabToday);
        tabWeek        = findViewById(R.id.tabWeek);
        tabMonth       = findViewById(R.id.tabMonth);
        tabAll         = findViewById(R.id.tabAll);
        tvKpiRevenue   = findViewById(R.id.tvKpiRevenue);
        tvKpiProfit    = findViewById(R.id.tvKpiProfit);
        tvKpiOrders    = findViewById(R.id.tvKpiOrders);
        tvKpiAvg       = findViewById(R.id.tvKpiAvg);
        barCash        = findViewById(R.id.barCash);
        barMobile      = findViewById(R.id.barMobile);
        barCard        = findViewById(R.id.barCard);
        tvCashPct      = findViewById(R.id.tvCashPct);
        tvMobilePct    = findViewById(R.id.tvMobilePct);
        tvCardPct      = findViewById(R.id.tvCardPct);
        recyclerSales  = findViewById(R.id.recyclerSales);
        emptySales     = findViewById(R.id.emptySales);
        tvSaleCount    = findViewById(R.id.tvSaleCount);
    }

    // ── Period tabs (identical logic) ─────────────────────────────────────────

    private void setupPeriodTabs() {
        TextView[] tabs    = {tabToday, tabWeek, tabMonth, tabAll};
        String[]   periods = {"Today", "Week", "Month", "All"};

        for (int i = 0; i < tabs.length; i++) {
            final String   period = periods[i];
            final TextView tab    = tabs[i];
            tab.setOnClickListener(v -> {
                currentPeriod = period;
                for (TextView t : tabs) {
                    t.setBackgroundResource(android.R.color.transparent);
                    t.setTextColor(0xFFBAE6FD);
                    t.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                tab.setBackgroundResource(R.drawable.tab_active_bg);
                tab.setTextColor(0xFFFFFFFF);
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
                loadReport(period);
            });
        }
    }

    // ── Load report — ALL 7 DB calls off main thread in one AsyncTask ─────────

    private void loadReport(String period) {
        new AsyncTask<Void, Void, ReportData>() {
            @Override
            protected ReportData doInBackground(Void... v) {
                ReportData d    = new ReportData();
                d.revenue       = saleRepo.getRevenue(period);
                d.profit        = saleRepo.getProfit(period);
                d.orders        = saleRepo.getOrderCount(period);
                d.avg           = d.orders > 0 ? d.revenue / d.orders : 0;
                d.cashCount     = saleRepo.getPaymentCount(period, "Cash");
                d.mobileCount   = saleRepo.getPaymentCount(period, "Mobile Money");
                d.cardCount     = saleRepo.getPaymentCount(period, "Card");
                d.sales         = saleRepo.getSales(period);
                return d;
            }

            @Override
            protected void onPostExecute(ReportData d) {
                // ── KPIs ──────────────────────────────────────────────────────
                tvKpiRevenue.setText(formatAmount(d.revenue));
                tvKpiProfit.setText(formatAmount(d.profit));
                tvKpiOrders.setText(String.valueOf(d.orders));
                tvKpiAvg.setText(formatAmount(d.avg));

                // ── Payment breakdown ─────────────────────────────────────────
                int totalPay = d.cashCount + d.mobileCount + d.cardCount;
                updatePaymentBar(barCash,   tvCashPct,   d.cashCount,   totalPay);
                updatePaymentBar(barMobile, tvMobilePct, d.mobileCount, totalPay);
                updatePaymentBar(barCard,   tvCardPct,   d.cardCount,   totalPay);

                // ── Sales list ────────────────────────────────────────────────
                int count = d.sales.size();
                tvSaleCount.setText(count + " " +
                        getString(count != 1 ? R.string.sales_plural : R.string.sales_singular));

                if (d.sales.isEmpty()) {
                    recyclerSales.setVisibility(View.GONE);
                    emptySales.setVisibility(View.VISIBLE);
                } else {
                    recyclerSales.setVisibility(View.VISIBLE);
                    emptySales.setVisibility(View.GONE);
                    recyclerSales.setAdapter(new SaleAdapter(d.sales));
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updatePaymentBar(View bar, TextView pctView, int count, int total) {
        float weight = total > 0 ? (float) count / total : 0f;
        int   pct    = Math.round(weight * 100);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = weight > 0 ? weight : 0.01f;
        bar.setLayoutParams(params);
        pctView.setText(pct + "%");
    }

    private String formatAmount(double amount) {
        if (amount >= 1_000_000)
            return currencyCode + " " + String.format("%.1fM", amount / 1_000_000);
        if (amount >= 1_000)
            return currencyCode + " " + String.format("%.0fK", amount / 1_000);
        return currencyCode + " " + String.format("%,.0f", amount);
    }

    // ── Sale list adapter (same logic, same IDs) ──────────────────────────────

    class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.VH> {

        private final List<Sale> sales;
        SaleAdapter(List<Sale> sales) { this.sales = sales; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sale, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Sale s = sales.get(pos);

            h.tvSaleId.setText(getString(R.string.sale_hash)
                    + s.id.substring(0, 6).toUpperCase());

            String time = s.date != null && s.date.length() >= 16
                    ? s.date.substring(11, 16) : (s.date != null ? s.date : "");
            h.tvSaleTime.setText(time + "  ·  " + s.paymentMethod);

            switch (s.paymentMethod != null ? s.paymentMethod : "") {
                case "Mobile Money": h.tvPayIcon.setText("📱"); break;
                case "Card":         h.tvPayIcon.setText("💳"); break;
                default:             h.tvPayIcon.setText("💵"); break;
            }

            h.tvTotal.setText(formatAmount(s.total));
        }

        @Override public int getItemCount() { return sales.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvPayIcon, tvSaleId, tvSaleTime, tvTotal;
            VH(View v) {
                super(v);
                tvPayIcon  = v.findViewById(R.id.tvSalePayIcon);
                tvSaleId   = v.findViewById(R.id.tvSaleId);
                tvSaleTime = v.findViewById(R.id.tvSaleTime);
                tvTotal    = v.findViewById(R.id.tvSaleTotal);
            }
        }
    }
}