package com.tfm.bleapp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Campaign {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("scenario")
    @Expose
    private String scenario;

    @SerializedName("description")
    @Expose
    private String description;

    public String getName() { return name; }

    public String getScenario() { return scenario; }

    public String getDescription() { return description; }

    public void setName(String name) { this.name = name; }

    public void setScenario(String scenario) { this.scenario = scenario; }

    public void setDescription(String description) { this.description = description; }
}
