package com.soapp.SoappApi;

/* Created by jayyong on 03/04/2018. */

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAPIClient {
    public static final String BASE_URL = "https://api2.soappchat.com/";
//    private final static OkHttpClient okHttpClient = new OkHttpClient.Builder()
//            .connectTimeout(20, TimeUnit.SECONDS)
//            .writeTimeout(20, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .build();
    private static Retrofit retrofit = null;

    public synchronized static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
