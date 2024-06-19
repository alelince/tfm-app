package com.tfm.bleapp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AcquisitionPoint {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("x")
    @Expose
    private int x;

    @SerializedName("y")
    @Expose
    private int y;

    @SerializedName("preamble_time")
    @Expose
    private int preambleTime;

    @SerializedName("acquisition_time")
    @Expose
    private int acquisitionTime;

    @SerializedName("campaign_name")
    @Expose
    private String campaignName;

    public int getId() { return id; }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPreambleTime() {
        return preambleTime;
    }

    public int getAcquisitionTime() {
        return acquisitionTime;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setId(int id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPreambleTime(int preambleTime) {
        this.preambleTime = preambleTime;
    }

    public void setAcquisitionTime(int acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }
}
