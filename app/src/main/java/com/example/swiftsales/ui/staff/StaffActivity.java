package com.example.swiftsales.ui.staff;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.StaffRepository;
import com.example.swiftsales.model.Staff;
import com.example.swiftsales.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class StaffActivity extends BaseActivity
        implements StaffAdapter.OnStaffActionListener {

    // ── Views (same IDs as before) ───────────────────────────────────────────
    RecyclerView recyclerStaff;
    LinearLayout emptyStaff;
    TextView     tvTotalStaff, tvActiveStaff, tvCashierCount;
    TextView     btnAddStaff, btnAddFirstStaff, btnBackStaff;
    TextView     tabAllStaff, tabCashiers, tabManagers, tabInactive;

    // ── Data ─────────────────────────────────────────────────────────────────
    StaffRepository repo;
    StaffAdapter    adapter;
    List<Staff>     staffList     = new ArrayList<>();
    String          currentFilter = "All";

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        repo = new StaffRepository(this);

        bindViews();
        setupAdapter();
        setupFilterTabs();
        setupClickListeners();
        loadStaff();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaff();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        recyclerStaff    = findViewById(R.id.recyclerStaff);
        emptyStaff       = findViewById(R.id.emptyStaff);
        tvTotalStaff     = findViewById(R.id.tvTotalStaff);
        tvActiveStaff    = findViewById(R.id.tvActiveStaff);
        tvCashierCount   = findViewById(R.id.tvCashierCount);
        btnAddStaff      = findViewById(R.id.btnAddStaff);
        btnAddFirstStaff = findViewById(R.id.btnAddFirstStaff);
        btnBackStaff     = findViewById(R.id.btnBackStaff);
        tabAllStaff      = findViewById(R.id.tabAllStaff);
        tabCashiers      = findViewById(R.id.tabCashiers);
        tabManagers      = findViewById(R.id.tabManagers);
        tabInactive      = findViewById(R.id.tabInactive);

        recyclerStaff.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapter() {
        adapter = new StaffAdapter(staffList, this);
        recyclerStaff.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBackStaff.setOnClickListener(v -> finish());
        btnAddStaff.setOnClickListener(v -> openStaffSheet(null));
        btnAddFirstStaff.setOnClickListener(v -> openStaffSheet(null));
    }

    // ── Filter tabs ───────────────────────────────────────────────────────────

    private void setupFilterTabs() {
        TextView[] tabs    = {tabAllStaff, tabCashiers, tabManagers, tabInactive};
        String[]   filters = {"All", "Cashier", "Manager", "Inactive"};

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
                loadStaff();
            });
        }
    }

    // ── Data loading (off main thread) ────────────────────────────────────────

    void loadStaff() {
        new LoadStaffTask().execute();
    }

    private class LoadStaffTask extends AsyncTask<Void, Void, List<Staff>> {
        @Override
        protected List<Staff> doInBackground(Void... v) {
            List<Staff> all = repo.getAll();
            switch (currentFilter) {
                case "Cashier":  return filterByRole(all, "Cashier");
                case "Manager":  return filterByRole(all, "Manager");
                case "Inactive": return getInactive(all);
                default:         return all;
            }
        }

        @Override
        protected void onPostExecute(List<Staff> result) {
            staffList.clear();
            staffList.addAll(result);
            adapter.updateList(staffList);
            updateEmptyState();
            loadStats();
        }
    }

    private void loadStats() {
        new AsyncTask<Void, Void, int[]>() {
            @Override
            protected int[] doInBackground(Void... v) {
                List<Staff> all = repo.getAll();
                int total = all.size(), active = 0, cashiers = 0;
                for (Staff s : all) {
                    if (s.isActive)               active++;
                    if ("Cashier".equals(s.role)) cashiers++;
                }
                return new int[]{total, active, cashiers};
            }

            @Override
            protected void onPostExecute(int[] counts) {
                tvTotalStaff.setText(String.valueOf(counts[0]));
                tvActiveStaff.setText(String.valueOf(counts[1]));
                tvCashierCount.setText(String.valueOf(counts[2]));
            }
        }.execute();
    }

    private List<Staff> filterByRole(List<Staff> all, String role) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : all) if (role.equals(s.role)) result.add(s);
        return result;
    }

    private List<Staff> getInactive(List<Staff> all) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : all) if (!s.isActive) result.add(s);
        return result;
    }

    private void updateEmptyState() {
        boolean empty = staffList.isEmpty();
        recyclerStaff.setVisibility(empty ? View.GONE    : View.VISIBLE);
        emptyStaff.setVisibility(empty    ? View.VISIBLE : View.GONE);
    }

    // ── StaffAdapter.OnStaffActionListener ────────────────────────────────────

    @Override
    public void onEdit(Staff s) {
        openStaffSheet(s);
    }

    @Override
    public void onToggle(Staff s) {
        String title = s.isActive
                ? getString(R.string.deactivate_staff)
                : getString(R.string.activate_staff);
        String msg = s.isActive
                ? getString(R.string.deactivate_confirm, s.name)
                : getString(R.string.activate_confirm,   s.name);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.yes), (d, w) ->
                        new AsyncTask<Void, Void, Void>() {
                            @Override protected Void doInBackground(Void... v) {
                                repo.toggleActive(s.id, !s.isActive);
                                return null;
                            }
                            @Override protected void onPostExecute(Void unused) {
                                String feedback = s.isActive
                                        ? getString(R.string.staff_deactivated, s.name)
                                        : getString(R.string.staff_activated,   s.name);
                                Toast.makeText(StaffActivity.this,
                                        feedback, Toast.LENGTH_SHORT).show();
                                loadStaff();
                            }
                        }.execute())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onDelete(Staff s) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.remove_staff_title))
                .setMessage(getString(R.string.remove_staff_confirm, s.name))
                .setPositiveButton(getString(R.string.remove), (d, w) ->
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override protected Boolean doInBackground(Void... v) {
                                return repo.delete(s.id);
                            }
                            @Override protected void onPostExecute(Boolean ok) {
                                Toast.makeText(StaffActivity.this,
                                        ok ? getString(R.string.staff_removed)
                                                : getString(R.string.something_went_wrong),
                                        Toast.LENGTH_SHORT).show();
                                loadStaff();
                            }
                        }.execute())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    // ── Add / Edit staff bottom sheet ─────────────────────────────────────────

    private void openStaffSheet(Staff editStaff) {
        AddEditStaffSheet sheet = AddEditStaffSheet.newInstance(editStaff);
        sheet.setOnSavedListener((staff, isEdit) ->
                new AsyncTask<Void, Void, Object[]>() {
                    @Override
                    protected Object[] doInBackground(Void... v) {
                        String excludeId = isEdit ? staff.id : null;
                        if (repo.pinExists(staff.pin, excludeId)) {
                            return new Object[]{false,
                                    getString(R.string.pin_already_used)};
                        }
                        boolean ok  = isEdit ? repo.update(staff) : repo.add(staff);
                        String  msg = ok
                                ? (isEdit
                                ? getString(R.string.staff_updated, staff.name)
                                : getString(R.string.staff_added,   staff.name))
                                : getString(R.string.something_went_wrong);
                        return new Object[]{ok, msg};
                    }

                    @Override
                    protected void onPostExecute(Object[] result) {
                        Toast.makeText(StaffActivity.this,
                                (String) result[1], Toast.LENGTH_SHORT).show();
                        if ((Boolean) result[0]) loadStaff();
                    }
                }.execute()
        );
        sheet.show(getSupportFragmentManager(), "staff_sheet");
    }
}