package com.unistrong.rearvideo;

import android.app.Application;
import android.content.Intent;

/**
 * Created by John on 2018/2/28.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, CameraService.class));
    }
}
