package com.example.pestanyes;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class BlankFragment extends Fragment {

    private TextView textViewViewModel, textViewNoViewModel;
    private Button buttonChangeText;

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        // Initialize views
        textViewViewModel = view.findViewById(R.id.textViewViewModel);
        textViewNoViewModel = view.findViewById(R.id.textViewNoViewModel);
        buttonChangeText = view.findViewById(R.id.buttonChangeText);

        // Set onClickListener for the button
        buttonChangeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change text for textViewViewModel using ViewModel
                // Assuming you have a ViewModel instance accessible here
                // viewModel.updateText("New text with ViewModel");

                // Change text for textViewNoViewModel directly
                textViewNoViewModel.setText("New text without ViewModel");
            }
        });

        return view;
    }
}
