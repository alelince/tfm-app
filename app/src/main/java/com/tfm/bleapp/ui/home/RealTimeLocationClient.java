package com.tfm.bleapp.ui.home;

import androidx.annotation.NonNull;

import com.tfm.bleapp.rest.IServiceAPI;
import com.tfm.bleapp.rest.LocationRequest;
import com.tfm.bleapp.rest.LocationResult;
import com.tfm.bleapp.rest.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RealTimeLocationClient {

    public static long INTEGRATION_TIME_MS = 500L;

    private final String scenario;
    private int requestId;
    private int responseId;
    private long lastTimeInMs;
    private final Map<String, List<Integer>> rssiBuffer;
    private final IServiceAPI api;

    private int x;
    private int y;
    private String semanticLocation;

    public RealTimeLocationClient(String host, int port, String scenario) {
        this.scenario = scenario;
        rssiBuffer = new HashMap<>();
        api = new RestClient(host, port).createServiceApiHandler();
        requestId = 0;
        responseId = 0;
        lastTimeInMs = System.currentTimeMillis();
    }

    public void addRssiSample(String beacon, int rssiValue) {
        List<Integer> rssiList = rssiBuffer.get(beacon);

        if (rssiList == null) {
            rssiList = new ArrayList<>();
            rssiBuffer.put(beacon, rssiList);
        }

        rssiList.add(rssiValue);

        if (System.currentTimeMillis() - lastTimeInMs >= INTEGRATION_TIME_MS) {
            sendLocationRequest();
        }
    }

    private void sendLocationRequest() {
        requestId++;
        Map<String, List<Integer>> rssi = new HashMap<>(rssiBuffer);
        rssiBuffer.clear();

        LocationRequest locReq = new LocationRequest();
        locReq.setRequestId(requestId);
        locReq.setRssi(rssi);

        long currentTimeInMs = System.currentTimeMillis();

        if (lastTimeInMs > 0) {
            locReq.setElapsedTimeInMs((int) (currentTimeInMs - lastTimeInMs));
        } else {
            locReq.setElapsedTimeInMs(0);
        }

        lastTimeInMs = currentTimeInMs;

        Call<LocationResult> locationCall = api.locate(scenario, locReq);

        locationCall.enqueue(new Callback<LocationResult>() {
            @Override
            public void onResponse(@NonNull Call<LocationResult> call,
                                   @NonNull Response<LocationResult> response) {
                if (response.code() == 200 && response.body() != null) {
                    LocationResult result = response.body();

                    if (result.getRequestId() >= responseId) {
                        responseId = result.getRequestId();
                        x = result.getX();
                        y = result.getY();
                        semanticLocation = result.getSemanticLocation();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LocationResult> call, @NonNull Throwable t) {
            }
        });
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getSemanticLocation() {
        return semanticLocation;
    }
}
