package com.tfm.bleapp;

public class AppState {
    private String scenario;
    private String host;
    private int port;
    private boolean connected;

    public AppState() {
        this.scenario = "ROOM";
        this.host = "192.168.0.14";
        this.port = 9999;
        this.connected = false;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }

    public void setHost(String host) { this.host = host; }

    public void setConnected(boolean connected) { this.connected = connected; }

    public String getHost() { return host; }

    public void setPort(int port) { this.port = port; }

    public int getPort() { return port; }

    public boolean isConnected() { return connected; }
}
