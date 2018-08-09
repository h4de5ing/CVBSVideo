package com.example.twovideotest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.twovideotest.utils.DeleteUtils;

import java.util.Timer;
import java.util.TimerTask;

public class WatchdogService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        initTimer();
        Log.i("gh0st", "WatchdogService running ");
    }

    private void initTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //Log.i("gh0st", "WatchdogService:" + VideoStorage.getStorageSpaceBytes());
                if (VideoStorage.getStorageSpaceBytes() <= 100 * 1024 * 1024) {
                    WatchdogService.this.sendBroadcast(new Intent("com.android.cvbs.finish"));
                    ////android.os.Process.killProcess(android.os.Process.myPid());
                    //Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    //startActivity(LaunchIntent);
                    //stopSelf();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //DeleteUtils.deleteFile(VideoStorage.getSaveVideoFilePath());
                        }
                    }).start();
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    public WatchdogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
