package com.example.swiftsales.ui.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onAdjustStock(Product product);
    }

    private final List<Product>           items;
    private final String                  currencyCode;
    private final OnProductActionListener listener;
    private final Context                 context;

    public ProductAdapter(Context context, List<Product> items,
                          String currencyCode, OnProductActionListener listener) {
        this.context      = context;
        this.items        = items;
        this.currencyCode = currencyCode;
        this.listener     = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvCategory, tvPrice, tvStock;
        TextView btnEdit, btnDelete, btnAdjustStock;

        VH(View v) {
            super(v);
            tvEmoji    = v.findViewById(R.id.tvProductEmoji);
            tvName     = v.findViewById(R.id.tvProductName);
            tvCategory = v.findViewById(R.id.tvProductCategory);
            tvPrice    = v.findViewById(R.id.tvProductPrice);
            tvStock    = v.findViewById(R.id.tvStockBadge);
            btnEdit    = v.findViewById(R.id.btnEdit);
            btnDelete  = v.findViewById(R.id.btnDelete);
            try {
                int adjId = v.getResources().getIdentifier(
                        "btnAdjustStock", "id", v.getContext().getPackageName());
                if (adjId != 0) btnAdjustStock = v.findViewById(adjId);
            } catch (Exception ignored) {}
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Product p = items.get(pos);

        h.tvEmoji.setText(p.emoji != null && !p.emoji.isEmpty() ? p.emoji : "📦");
        h.tvName.setText(p.name);
        h.tvCategory.setText(p.category != null ? p.category
                : context.getString(R.string.category_other));
        h.tvPrice.setText(p.formattedPrice(currencyCode));

        // ── Stock badge — labels from string resources ────────────────────────
        if (p.isOutOfStock()) {
            h.tvStock.setText(context.getString(R.string.out_of_stock));
            h.tvStock.setTextColor(context.getResources().getColor(R.color.danger, null));
            h.tvStock.setBackgroundResource(R.drawable.badge_danger);
        } else if (p.isLowStock()) {
            h.tvStock.setText(context.getString(R.string.low_stock_badge, p.stock));
            h.tvStock.setTextColor(context.getResources().getColor(R.color.warning, null));
            h.tvStock.setBackgroundResource(R.drawable.badge_warning);
        } else {
            h.tvStock.setText(context.getString(R.string.in_stock_badge, p.stock));
            h.tvStock.setTextColor(context.getResources().getColor(R.color.success, null));
            h.tvStock.setBackgroundResource(R.drawable.badge_success);
        }

        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(p); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(p); });
        if (h.btnAdjustStock != null) {
            h.btnAdjustStock.setOnClickListener(v -> {
                if (listener != null) listener.onAdjustStock(p);
            });
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void updateList(List<Product> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }
}