package com.example.swiftsales.ui.pos;

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

public class POSProductAdapter extends RecyclerView.Adapter<POSProductAdapter.VH> {

    public interface OnAddToCartListener {
        void onAdd(Product product);
    }

    private final List<Product>       items;
    private final String              currencyCode;
    private final OnAddToCartListener listener;
    private final Context             context;

    public POSProductAdapter(Context context, List<Product> items,
                             String currencyCode, OnAddToCartListener listener) {
        this.context      = context;
        this.items        = items;
        this.currencyCode = currencyCode;
        this.listener     = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pos_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Product p = items.get(pos);

        h.tvEmoji.setText(p.emoji != null ? p.emoji : "📦");
        h.tvName.setText(p.name);
        h.tvPrice.setText(p.formattedPrice(currencyCode));

        // ── Stock label from string resource ──────────────────────────────────
        h.tvStock.setText(context.getString(R.string.pos_stock_label, p.stock));

        // ── Out-of-stock / Add button from string resources ───────────────────
        if (p.isOutOfStock()) {
            h.btnAdd.setText(context.getString(R.string.out_of_stock));
            h.btnAdd.setAlpha(0.5f);
            h.btnAdd.setOnClickListener(null);
        } else {
            h.btnAdd.setText(context.getString(R.string.pos_add_btn));
            h.btnAdd.setAlpha(1f);
            h.btnAdd.setOnClickListener(v -> listener.onAdd(p));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void updateList(List<Product> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvPrice, tvStock, btnAdd;

        VH(View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvPosEmoji);
            tvName  = v.findViewById(R.id.tvPosProductName);
            tvPrice = v.findViewById(R.id.tvPosPrice);
            tvStock = v.findViewById(R.id.tvPosStock);
            btnAdd  = v.findViewById(R.id.btnAddToCart);
        }
    }
}