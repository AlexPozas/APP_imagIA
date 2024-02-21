package com.example.imageai.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<Boolean> isCameraEnabled;

    public HomeViewModel() {
        isCameraEnabled = new MutableLiveData<>();
        isCameraEnabled.setValue(false); // Inicialmente la cámara está deshabilitada
    }

    public LiveData<Boolean> getIsCameraEnabled() {
        return isCameraEnabled;
    }

    public void setCameraEnabled(boolean enabled) {
        isCameraEnabled.setValue(enabled);
    }
}
