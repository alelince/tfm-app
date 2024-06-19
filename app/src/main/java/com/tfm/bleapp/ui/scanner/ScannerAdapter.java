package com.tfm.bleapp.ui.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tfm.bleapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class ScannerAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    protected HashMap<String, ScanResult> scanMap;
    protected ArrayList<String> beaconList;
    protected ArrayList<View> viewList;

    public ScannerAdapter(Context appContext) {
        inflater = LayoutInflater.from(appContext);
        scanMap = new HashMap<>();
        beaconList = new ArrayList<>();
        viewList = new ArrayList<>();
    }

    public void reset() {
        scanMap.clear();
        beaconList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    public void update(ScanResult result) {
        String beaconId;

        if (result.getDevice().getName() == null) {
            beaconId = result.getDevice().getAddress();
        } else {
            beaconId = result.getDevice().getName();
        }

        if (!scanMap.containsKey(beaconId)) {
            beaconList.add(beaconId);
        }

        scanMap.put(beaconId, result);

        // sort by RSSI
        Collections.sort(beaconList, (n1, n2) -> {
            if (scanMap.containsKey(n1) && scanMap.containsKey(n2)) {
                try {
                    int v1 = Objects.requireNonNull(scanMap.get(n1)).getRssi();
                    int v2 = Objects.requireNonNull(scanMap.get(n2)).getRssi();
                    return v1 > v2 ? -1 : 1;
                } catch (NullPointerException e) {
                    // this should never happen
                }
            }

            return 0;
        });

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return beaconList.size();
    }

    @Override
    public ScanResult getItem(int position) {
        if (beaconList.size() <= position) {
            return null;
        }

        return scanMap.get(beaconList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (viewList.size() <= position) {
            View view = inflater.inflate(R.layout.scanner_row, parent, false);
            viewList.add(view);
        }

        View view = viewList.get(position);
        TextView beaconDataView = view.findViewById(R.id.beaconDataView);

        String beaconId = beaconList.get(position);
        ScanResult result = scanMap.get(beaconId);

        if (result == null) {
            beaconDataView.setText(beaconId);
        } else {
            beaconDataView.setText(String.format("%d dBm\t\t\t %s", result.getRssi(), beaconId));
        }

        return view;
    }
}
