package com.example.swiftsales.ui.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface OnOrderActionListener {
        void onFulfil(Order order);
        void onCancel(Order order);
        void onDelete(Order order);
    }

    private final List<Order>           items;
    private final String                currencyCode;
    private final OnOrderActionListener listener;

    public OrderAdapter(List<Order> items, String currencyCode,
                        OnOrderActionListener listener) {
        this.items        = items;
        this.currencyCode = currencyCode;
        this.listener     = listener;
    }

    // ── ViewHolder — IDs match item_order.xml ─────────────────────────────────
    static class VH extends RecyclerView.ViewHolder {
        TextView tvChannel, tvCustomerName, tvContact, tvStatus,
                tvTotal, tvDate, tvItemCount;
        TextView btnFulfil, btnCancel, btnDelete;

        VH(View v) {
            super(v);
            tvChannel      = v.findViewById(R.id.tvOrderChannel);
            tvCustomerName = v.findViewById(R.id.tvOrderCustomerName);
            tvContact      = v.findViewById(R.id.tvOrderContact);
            tvStatus       = v.findViewById(R.id.tvOrderStatus);
            tvTotal        = v.findViewById(R.id.tvOrderTotal);
            tvDate         = v.findViewById(R.id.tvOrderDate);
            tvItemCount    = v.findViewById(R.id.tvOrderItemCount);
            btnFulfil      = v.findViewById(R.id.btnFulfilOrder);
            btnCancel      = v.findViewById(R.id.btnCancelOrder);
            btnDelete      = v.findViewById(R.id.btnDeleteOrder);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Order  o   = items.get(pos);
        Context ctx = h.itemView.getContext();

        h.tvChannel.setText(o.getChannelEmoji() + "  " + (o.channel != null ? o.channel : ""));
        h.tvCustomerName.setText(o.customerName != null ? o.customerName : "—");
        h.tvContact.setText(o.customerContact  != null ? o.customerContact : "—");
        h.tvTotal.setText(currencyCode + " " + String.format("%,.0f", o.total));

        // Date — show time if today, date otherwise
        String date = o.createdAt != null && o.createdAt.length() >= 16
                ? o.createdAt.substring(0, 16) : (o.createdAt != null ? o.createdAt : "—");
        h.tvDate.setText(date);

        // Item count
        int itemCount = o.items != null ? o.items.size() : 0;
        h.tvItemCount.setText(itemCount + " " + ctx.getString(R.string.items));

        // Status badge
        h.tvStatus.setText(o.status != null ? o.status : Order.STATUS_PENDING);
        int badgeRes = ctx.getResources().getIdentifier(
                o.getStatusBadgeDrawable(), "drawable", ctx.getPackageName());
        if (badgeRes != 0) h.tvStatus.setBackgroundResource(badgeRes);

        // Button visibility — only Pending can be fulfilled/cancelled
        boolean isPending = Order.STATUS_PENDING.equals(o.status);
        h.btnFulfil.setVisibility(isPending ? View.VISIBLE : View.GONE);
        h.btnCancel.setVisibility(isPending ? View.VISIBLE : View.GONE);

        h.btnFulfil.setOnClickListener(v -> { if (listener != null) listener.onFulfil(o); });
        h.btnCancel.setOnClickListener(v -> { if (listener != null) listener.onCancel(o); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(o); });
    }

    @Override public int getItemCount() { return items.size(); }

    public void updateList(List<Order> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }
}