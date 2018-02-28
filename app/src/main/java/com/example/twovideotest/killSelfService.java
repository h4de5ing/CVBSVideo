package com.example.twovideotest;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by John on 2018/2/28.
 */

public class killSelfService extends Service {
    /**
     * 关闭应用后多久重新启动
     */
    private static long stopDelayed = 1000;
    private Handler handler;
    private String PackageName;

    public killSelfService() {
        handler = new Handler();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        stopDelayed = intent.getLongExtra("Delayed", 0);
        PackageName = intent.getStringExtra("PackageName");
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PackageName);
        startActivity(LaunchIntent);
        killSelfService.this.stopSelf();
/*        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PackageName);
                startActivity(LaunchIntent);
                killSelfService.this.stopSelf();
            }
        }, stopDelayed);*/
        Log.i("gh0st", "重启结束");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}