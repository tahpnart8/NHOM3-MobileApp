package com.nhom3.pubgapp.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nhom3.pubgapp.databinding.FragmentConversationListBinding;

/** Empty screen shell; the module owner fills in the content. */
public class ConversationListFragment extends Fragment {

    private FragmentConversationListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentConversationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Views are gone after this point, so drop the reference to avoid a leak.
        binding = null;
    }
}
