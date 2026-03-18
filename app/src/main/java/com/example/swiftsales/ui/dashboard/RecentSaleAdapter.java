package com.example.swiftsales.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.model.Sale;

import java.util.ArrayList;
import java.util.List;

public class RecentSaleAdapter extends RecyclerView.Adapter<RecentSaleAdapter.VH> {

    private final Context   context;
    private List<Sale>      sales        = new ArrayList<>();
    private String          currencyCode = "RWF";

    public RecentSaleAdapter(Context context, String currencyCode) {
        this.context      = context;
        this.currencyCode = currencyCode;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setSales(List<Sale> newSales) {
        this.sales = newSales != null ? newSales : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrency(String code) {
        this.currencyCode = code;
        notifyDataSetChanged();
    }

    // ── ViewHolder — same IDs as item_sale.xml ────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        TextView tvPayIcon, tvSaleId, tvSaleTime, tvSaleTotal;

        VH(View v) {
            super(v);
            tvPayIcon  = v.findViewById(R.id.tvSalePayIcon);
            tvSaleId   = v.findViewById(R.id.tvSaleId);
            tvSaleTime = v.findViewById(R.id.tvSaleTime);
            tvSaleTotal = v.findViewById(R.id.tvSaleTotal);
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Sale s = sales.get(pos);

        // ── Payment icon ──────────────────────────────────────────────────────
        String method = s.paymentMethod != null ? s.paymentMethod : "";
        switch (method) {
            case "Mobile Money": h.tvPayIcon.setText("📱"); break;
            case "Card":         h.tvPayIcon.setText("💳"); break;
            default:             h.tvPayIcon.setText("💵"); break;
        }

        // ── Sale ID — localised prefix ────────────────────────────────────────
        String shortId = (s.id != null && s.id.length() >= 6)
                ? s.id.substring(0, 6).toUpperCase() : (s.id != null ? s.id : "------");
        h.tvSaleId.setText(context.getString(R.string.sale_hash) + shortId);

        // ── Time + payment method ─────────────────────────────────────────────
        String time = (s.date != null && s.date.length() >= 16)
                ? s.date.substring(11, 16) : (s.date != null ? s.date : "--:--");
        h.tvSaleTime.setText(time + "  ·  " + method);

        // ── Total ─────────────────────────────────────────────────────────────
        h.tvSaleTotal.setText(currencyCode + " " + String.format("%,.0f", s.total));
    }

    @Override
    public int getItemCount() { return sales.size(); }
}