package com.tfm.bleapp.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final BluetoothLeScanner bleScanner;
    private final ScanCallback callback;

    public Scanner(ScanCallback callback) {
        this.callback = callback;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        bleScanner = adapter.getBluetoothLeScanner();
    }

    @SuppressLint("MissingPermission")
    public boolean scan(String[] beaconNames, int scanMode) {
        if (bleScanner == null) {
            return false;
        }

        // prepare scan filters
        List<ScanFilter> filters = (beaconNames.length > 0) ? new ArrayList<>(beaconNames.length) : null;
        for (String beaconName : beaconNames) {
            ScanFilter filter = new ScanFilter.Builder().setDeviceName(beaconName).build();
            filters.add(filter);
        }

        // prepare scan settings
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(scanMode)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        bleScanner.startScan(filters, settings, callback);
        return true;
    }

    public boolean scanBalanced(String[] beaconNames) {
        return scan(beaconNames, ScanSettings.SCAN_MODE_BALANCED);
    }

    public boolean scanBalanced() {
        return scanBalanced(new String[0]);
    }

    public boolean scanLowLatency(String[] beaconNames) {
        return scan(beaconNames, ScanSettings.SCAN_MODE_LOW_LATENCY);
    }

    public boolean scanLowLatency() {
        return scanLowLatency(new String[0]);
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        bleScanner.stopScan(callback);
    }

}
