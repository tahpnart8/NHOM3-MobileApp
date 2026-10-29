package com.nhom3.pubgapp.ui.trade;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.nhom3.pubgapp.databinding.ItemPaymentProviderBinding;

import java.util.ArrayList;
import java.util.List;

// Grid of banks or e-wallets on the C07 checkout. One tile can be selected at a time.
public class PaymentProviderAdapter extends RecyclerView.Adapter<PaymentProviderAdapter.ProviderViewHolder> {

    public interface OnProviderClickListener {
        void onProviderClick(PaymentProvider provider);
    }

    // Monogram tiles take one of the three app container colours, so no tile imitates a real brand colour.
    private static final int[] TILE_BACKGROUNDS = {
            com.google.android.material.R.attr.colorPrimaryContainer,
            com.google.android.material.R.attr.colorSecondaryContainer,
            com.google.android.material.R.attr.colorTertiaryContainer
    };
    private static final int[] TILE_FOREGROUNDS = {
            com.google.android.material.R.attr.colorOnPrimaryContainer,
            com.google.android.material.R.attr.colorOnSecondaryContainer,
            com.google.android.material.R.attr.colorOnTertiaryContainer
    };

    private final List<PaymentProvider> providers = new ArrayList<PaymentProvider>();
    private final OnProviderClickListener listener;
    private String selectedCode;

    public PaymentProviderAdapter(@NonNull OnProviderClickListener listener) {
        this.listener = listener;
    }

    public void submitProviders(@NonNull List<PaymentProvider> newProviders) {
        providers.clear();
        providers.addAll(newProviders);
        notifyDataSetChanged();
    }

    public void setSelectedCode(@Nullable String code) {
        selectedCode = code;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPaymentProviderBinding binding = ItemPaymentProviderBinding.inflate(inflater, parent, false);
        return new ProviderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        holder.bind(providers.get(position));
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    class ProviderViewHolder extends RecyclerView.ViewHolder {

        private final ItemPaymentProviderBinding binding;

        ProviderViewHolder(@NonNull ItemPaymentProviderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull final PaymentProvider provider) {
            binding.textMonogram.setText(provider.getCode());
            binding.textProviderName.setText(provider.getName());
            paintMonogram(provider.getCode());

            boolean selected = provider.getCode().equals(selectedCode);
            binding.cardProvider.setChecked(selected);
            int strokeAttr = selected ? androidx.appcompat.R.attr.colorPrimary
                    : com.google.android.material.R.attr.colorOutlineVariant;
            binding.cardProvider.setStrokeColor(MaterialColors.getColor(binding.cardProvider, strokeAttr));

            binding.cardProvider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onProviderClick(provider);
                }
            });
        }

        // The colour depends on the code only, so a tile keeps its colour while the list is filtered.
        private void paintMonogram(@NonNull String code) {
            int index = Math.abs(code.hashCode() % TILE_BACKGROUNDS.length);
            int background = MaterialColors.getColor(binding.textMonogram, TILE_BACKGROUNDS[index]);
            int foreground = MaterialColors.getColor(binding.textMonogram, TILE_FOREGROUNDS[index]);
            binding.textMonogram.setBackgroundTintList(ColorStateList.valueOf(background));
            binding.textMonogram.setTextColor(foreground);
        }
    }
}
