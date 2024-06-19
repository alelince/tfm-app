package com.tfm.bleapp.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private final Retrofit retrofit;

    public RestClient(String host, int port) {
        Gson gson = new GsonBuilder().setLenient().create();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + host + ":" + port)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public IServiceAPI createServiceApiHandler() {
        return retrofit.create(IServiceAPI.class);
    }
}
