package com.tfm.bleapp.ui.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.tfm.bleapp.R;
import com.tfm.bleapp.databinding.FragmentScannerBinding;
import com.tfm.bleapp.scanner.Scanner;

import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ScannerFragment extends Fragment {

    private FragmentScannerBinding binding;
    private Switch filterSwitch;
    private Scanner scanner;
    private ScannerAdapter scannerAdapter;
    private String[] beaconNames;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconNames = getResources().getStringArray(com.tfm.bleapp.R.array.beacon_names);
        scannerAdapter = new ScannerAdapter(getContext());
        scanner = new Scanner(new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                scannerAdapter.update(result);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(scannerAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            ScanResult result = scannerAdapter.getItem(position);
            if (result != null) {
                String title;

                if (result.getDevice().getName() == null) {
                    title = result.getDevice().getAddress();
                } else {
                    title = result.getDevice().getName();
                }

                String content = Objects.requireNonNull(result.getScanRecord()).toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(title).setMessage(content)
                        .setPositiveButton("OK", (dialog, id2) -> dialog.dismiss())
                        .show();
            }
        });

        filterSwitch = view.findViewById(R.id.switch_filter);
        filterSwitch.setOnClickListener(v -> {
            scanner.stop();
            scannerAdapter.reset();
            startScanning();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        startScanning();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanner.stop();
    }

    private void startScanning() {
        boolean success;

        if (filterSwitch.isChecked()) {
            success = scanner.scanBalanced(beaconNames);
        } else {
            success = scanner.scanBalanced();
        }

        if (!success) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Error")
                    .setMessage("Bluetooth scan could not be started!")
                    .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                    .show();
        }
    }
}