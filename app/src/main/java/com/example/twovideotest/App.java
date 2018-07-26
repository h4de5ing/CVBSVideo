package com.example.twovideotest;

import android.app.Application;
import android.content.Intent;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(Intent.ACTION_RUN);
        intent.setClass(this, WatchdogService.class);
        //startService(intent);
    }
}
