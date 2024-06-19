package com.tfm.bleapp;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private final AppState state = new AppState();

    public AppState getState() {
        return state;
    }
}
