package com.example.imageai.ui.home;
// CamaraXViewModel.java
import androidx.camera.core.ImageCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;

public class CamaraXViewModel extends ViewModel {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    public void initCamera(PreviewView previewView) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.getContext());
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    // Configurar y vincular la cámara aquí
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(previewView.getContext()));
    }

    public void takePhoto() {
        // Capturar imagen aquí
    }
}
