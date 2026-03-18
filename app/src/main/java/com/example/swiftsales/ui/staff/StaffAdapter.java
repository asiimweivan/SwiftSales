package com.example.swiftsales.ui.staff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.model.Staff;

import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.VH> {

    public interface OnStaffActionListener {
        void onEdit(Staff s);
        void onToggle(Staff s);
        void onDelete(Staff s);
    }

    private final List<Staff>           items;
    private final OnStaffActionListener listener;

    private static final int[] AVATAR_COLORS = {
            0xFF0EA5E9, 0xFFF97316, 0xFF10B981,
            0xFF8B5CF6, 0xFFEC4899, 0xFF14B8A6
    };

    public StaffAdapter(List<Staff> items, OnStaffActionListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvPhone, tvRole, tvJoinDate;
        TextView btnEdit, btnToggle, btnDelete;
        View     dotActive;

        VH(View v) {
            super(v);
            tvAvatar   = v.findViewById(R.id.tvStaffAvatar);
            tvName     = v.findViewById(R.id.tvStaffName);
            tvPhone    = v.findViewById(R.id.tvStaffPhone);
            tvRole     = v.findViewById(R.id.tvStaffRoleBadge);
            tvJoinDate = v.findViewById(R.id.tvStaffJoinDate);
            btnEdit    = v.findViewById(R.id.btnEditStaff);
            btnToggle  = v.findViewById(R.id.btnToggleStaff);
            dotActive  = v.findViewById(R.id.dotActive);

            try {
                int delId = v.getResources().getIdentifier(
                        "btnDeleteStaff", "id", v.getContext().getPackageName());
                if (delId != 0) btnDelete = v.findViewById(delId);
            } catch (Exception ignored) {}
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Staff   s   = items.get(pos);
        Context ctx = h.itemView.getContext();

        h.tvAvatar.setText(s.getInitials());
        h.tvName.setText(s.name);
        h.tvPhone.setText(s.phone != null ? s.phone : "");
        h.tvRole.setText(s.getRoleEmoji() + " " + s.role);

        // ── "Joined:" label from string resource ─────────────────────────────
        String dateStr = (s.joinDate != null && s.joinDate.length() >= 10)
                ? s.joinDate.substring(0, 10) : "—";
        h.tvJoinDate.setText(ctx.getString(R.string.joined) + " " + dateStr);

        h.tvAvatar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        AVATAR_COLORS[pos % AVATAR_COLORS.length]));

        h.dotActive.setVisibility(s.isActive ? View.VISIBLE : View.INVISIBLE);
        h.btnToggle.setText(s.isActive ? "⏸" : "▶");

        h.btnEdit.setOnClickListener(v   -> { if (listener != null) listener.onEdit(s);   });
        h.btnToggle.setOnClickListener(v -> { if (listener != null) listener.onToggle(s); });

        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(s); });
        }

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(s);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void updateList(List<Staff> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }
}