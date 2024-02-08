package com.example.imageai.ui.home;
// CamaraXActivity.java

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.lifecycle.ViewModelProvider;

import com.example.imageai.databinding.ActivityCamaraXBinding;

public class CamaraXActivity extends AppCompatActivity {

    private ActivityCamaraXBinding binding;
    private CamaraXViewModel viewModel;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCamaraXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CamaraXViewModel.class);

        binding.takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.takePhoto();
            }
        });

        viewModel.initCamera(binding.previewView);
    }
}
