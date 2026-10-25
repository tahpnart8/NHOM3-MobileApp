package com.nhom3.pubgapp.ui.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom3.pubgapp.databinding.ItemAccountAddressBinding;
import com.nhom3.pubgapp.model.UserAddress;

import java.util.ArrayList;
import java.util.List;

// Adapter for displaying user addresses in A06 AddressListFragment.
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface AddressActionListener {
        void onEditAddress(UserAddress address);
        void onDeleteAddress(UserAddress address);
    }

    private final List<UserAddress> items = new ArrayList<UserAddress>();
    private AddressActionListener listener;

    public void setListener(AddressActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<UserAddress> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAccountAddressBinding binding = ItemAccountAddressBinding.inflate(inflater, parent, false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        final UserAddress address = items.get(position);
        holder.bind(address, listener);
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {

        private final ItemAccountAddressBinding binding;

        AddressViewHolder(@NonNull ItemAccountAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final UserAddress address, final AddressActionListener listener) {
            String label = address.getLabel();
            if (label == null || label.trim().isEmpty()) {
                label = address.getReceiverName();
            }
            binding.textAddressLabel.setText(label != null ? label : "");

            if (address.isDefault()) {
                binding.badgeDefault.setVisibility(View.VISIBLE);
            } else {
                binding.badgeDefault.setVisibility(View.GONE);
            }

            String name = address.getReceiverName() != null ? address.getReceiverName() : "";
            String phone = address.getPhone() != null ? address.getPhone() : "";
            StringBuilder contact = new StringBuilder();
            if (!name.isEmpty()) {
                contact.append(name);
            }
            if (!phone.isEmpty()) {
                if (contact.length() > 0) {
                    contact.append(" • ");
                }
                contact.append(phone);
            }
            binding.textReceiverAndPhone.setText(contact.toString());

            String fullText = address.getFullText();
            binding.textAddressDetail.setText(fullText != null ? fullText : "");

            binding.btnEditAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onEditAddress(address);
                    }
                }
            });

            binding.btnDeleteAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteAddress(address);
                    }
                }
            });
        }
    }
}
