package com.tfm.bleapp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class LocationRequest {

    @SerializedName("request_id")
    @Expose
    private int requestId;

    @SerializedName("rssi")
    @Expose
    private Map<String, List<Integer>> rssi;

    @SerializedName("elapsed_time_ms")
    @Expose
    private int elapsedTimeInMs;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Map<String, List<Integer>> getRssi() {
        return rssi;
    }

    public void setRssi(Map<String, List<Integer>> rssi) {
        this.rssi = rssi;
    }

    public int getElapsedTimeInMs() {
        return elapsedTimeInMs;
    }

    public void setElapsedTimeInMs(int elapsedTimeInMs) {
        this.elapsedTimeInMs = elapsedTimeInMs;
    }
}
