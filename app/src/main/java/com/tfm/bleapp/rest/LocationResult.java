package com.tfm.bleapp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResult {

    @SerializedName("request_id")
    @Expose
    private int requestId;

    @SerializedName("x")
    @Expose
    private int x;

    @SerializedName("y")
    @Expose
    private int y;

    @SerializedName("semantic_location")
    @Expose
    private String semanticLocation;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getSemanticLocation() {
        return semanticLocation;
    }

    public void setSemanticLocation(String semanticLocation) {
        this.semanticLocation = semanticLocation;
    }
}
