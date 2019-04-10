package com.github.usbcamera.boot;

import android.app.Application;
import android.content.Intent;

import com.github.usbcamera.GPIOService;

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(Intent.ACTION_RUN);
        intent.setClass(this, GPIOService.class);
        startService(intent);
    }
}
