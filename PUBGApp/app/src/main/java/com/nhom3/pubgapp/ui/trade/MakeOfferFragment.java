package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nhom3.pubgapp.databinding.FragmentMakeOfferBinding;

/** Empty bottom sheet shell for sending a price offer; opened from B03. */
public class MakeOfferFragment extends BottomSheetDialogFragment {

    private FragmentMakeOfferBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMakeOfferBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this point, so drop the reference to avoid a leak.
        binding = null;
    }
}
