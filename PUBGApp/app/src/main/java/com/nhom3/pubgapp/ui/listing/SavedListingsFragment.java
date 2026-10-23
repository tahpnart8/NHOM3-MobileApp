package com.nhom3.pubgapp.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nhom3.pubgapp.databinding.FragmentSavedListingsBinding;

/** Screen B06, empty shell: the filling issue adds the ViewModel and the events. */
public class SavedListingsFragment extends Fragment {

    private FragmentSavedListingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedListingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views die with the view hierarchy; holding the binding would leak them.
        binding = null;
    }
}
