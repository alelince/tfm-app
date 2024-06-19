package com.tfm.bleapp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AcquisitionSample {
    @SerializedName("beacon")
    @Expose
    private String beacon;

    @SerializedName("rx_timestamp")
    @Expose
    private String rxTimestamp;

    @SerializedName("rssi")
    @Expose
    private int rssi;

    @SerializedName("tx_time_ns")
    @Expose
    private int txTimeInNanos;

    public String getBeacon() {
        return beacon;
    }

    public void setBeacon(String beacon) {
        this.beacon = beacon;
    }

    public String getRxTimestamp() {
        return rxTimestamp;
    }

    public void setRxTimestamp(String rxTimestamp) {
        this.rxTimestamp = rxTimestamp;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getTxTimeInNanos() {
        return txTimeInNanos;
    }

    public void setTxTimeInNanos(int txTimeInNanos) {
        this.txTimeInNanos = txTimeInNanos;
    }
}
