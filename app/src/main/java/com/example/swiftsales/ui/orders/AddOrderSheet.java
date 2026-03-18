package com.example.swiftsales.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.swiftsales.R;
import com.example.swiftsales.data.repository.ProductRepository;
import com.example.swiftsales.model.CartItem;
import com.example.swiftsales.model.Order;
import com.example.swiftsales.model.Product;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddOrderSheet extends BottomSheetDialogFragment {

    public interface OnSavedListener { void onSaved(Order order); }
    private OnSavedListener listener;
    public void setOnSavedListener(OnSavedListener l) { this.listener = l; }

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText  etCustomerName, etCustomerContact, etNotes;
    private Spinner   spinnerChannel;
    private LinearLayout itemsContainer;
    private TextView  btnAddItem, btnSaveOrder, btnCancelOrder;

    // ── Data ──────────────────────────────────────────────────────────────────
    private ProductRepository    productRepo;
    private List<Product>        allProducts = new ArrayList<>();
    private List<CartItem>       orderItems  = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productRepo = new ProductRepository(requireContext());
        allProducts = productRepo.getAll();

        bindViews(view);
        setupChannelSpinner();
        setupButtons();
    }

    private void bindViews(View v) {
        etCustomerName    = v.findViewById(R.id.etOrderCustomerName);
        etCustomerContact = v.findViewById(R.id.etOrderCustomerContact);
        etNotes           = v.findViewById(R.id.etOrderNotes);
        spinnerChannel    = v.findViewById(R.id.spinnerOrderChannel);
        itemsContainer    = v.findViewById(R.id.orderItemsContainer);
        btnAddItem        = v.findViewById(R.id.btnAddOrderItem);
        btnSaveOrder      = v.findViewById(R.id.btnSaveOrder);
        btnCancelOrder    = v.findViewById(R.id.btnCancelOrder);
    }

    private void setupChannelSpinner() {
        String[] channels = {
                Order.CHANNEL_PHONE,
                Order.CHANNEL_WHATSAPP,
                Order.CHANNEL_DELIVERY,
                Order.CHANNEL_WALKIN,
                Order.CHANNEL_OTHER
        };
        spinnerChannel.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, channels));
    }

    private void setupButtons() {
        btnCancelOrder.setOnClickListener(v -> dismiss());
        btnAddItem.setOnClickListener(v -> showProductPicker());
        btnSaveOrder.setOnClickListener(v -> saveOrder());
    }

    // ── Product picker ────────────────────────────────────────────────────────

    private void showProductPicker() {
        if (allProducts.isEmpty()) {
            Toast.makeText(requireContext(),
                    getString(R.string.no_products_found), Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[allProducts.size()];
        for (int i = 0; i < allProducts.size(); i++)
            names[i] = allProducts.get(i).emoji + "  " + allProducts.get(i).name;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_product))
                .setItems(names, (d, which) -> {
                    Product selected = allProducts.get(which);
                    addItemRow(selected);
                })
                .show();
    }

    private void addItemRow(Product product) {
        // Check if already in list — if so increment quantity
        for (CartItem item : orderItems) {
            if (item.product.id.equals(product.id)) {
                item.quantity++;
                refreshItemsUI();
                return;
            }
        }
        orderItems.add(new CartItem(product, 1));
        refreshItemsUI();
    }

    private void refreshItemsUI() {
        itemsContainer.removeAllViews();
        for (CartItem item : orderItems) {
            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_order_row, itemsContainer, false);

            TextView tvName  = row.findViewById(R.id.tvOrderRowName);
            TextView tvQty   = row.findViewById(R.id.tvOrderRowQty);
            TextView btnPlus = row.findViewById(R.id.btnOrderRowPlus);
            TextView btnMinus = row.findViewById(R.id.btnOrderRowMinus);
            TextView btnRemove = row.findViewById(R.id.btnOrderRowRemove);

            tvName.setText(item.product.emoji + "  " + item.product.name);
            tvQty.setText(String.valueOf(item.quantity));

            btnPlus.setOnClickListener(v -> { item.quantity++; refreshItemsUI(); });
            btnMinus.setOnClickListener(v -> {
                if (item.quantity > 1) { item.quantity--; refreshItemsUI(); }
            });
            btnRemove.setOnClickListener(v -> {
                orderItems.remove(item);
                refreshItemsUI();
            });

            itemsContainer.addView(row);
        }
    }

    // ── Save order ────────────────────────────────────────────────────────────

    private void saveOrder() {
        String name = etCustomerName.getText().toString().trim();
        if (name.isEmpty()) {
            etCustomerName.setError(getString(R.string.error_full_name_required));
            etCustomerName.requestFocus(); return;
        }
        if (orderItems.isEmpty()) {
            Toast.makeText(requireContext(),
                    getString(R.string.order_no_items), Toast.LENGTH_SHORT).show();
            return;
        }

        Order order          = new Order();
        order.customerName   = name;
        order.customerContact = etCustomerContact.getText().toString().trim();
        order.channel        = spinnerChannel.getSelectedItem().toString();
        order.notes          = etNotes.getText().toString().trim();
        order.items          = new ArrayList<>(orderItems);
        order.total          = 0;
        for (CartItem item : orderItems)
            order.total += item.getLineTotal();

        if (listener != null) listener.onSaved(order);
        dismiss();
    }
}