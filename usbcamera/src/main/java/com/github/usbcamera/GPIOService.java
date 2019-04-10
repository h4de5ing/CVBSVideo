package com.github.usbcamera;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.unistrong.e9631sdk.Command;
import com.unistrong.e9631sdk.CommunicationService;
import com.unistrong.e9631sdk.DataType;

import java.util.Timer;
import java.util.TimerTask;

public class GPIOService extends Service {
    private CommunicationService mService;

    public GPIOService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initTimer();
    }

    private void initTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                write(Command.Send.Gpio().get(2));
            }
        }, 1000, 1000);
    }

    private void initData() {
        try {
            mService = CommunicationService.getInstance(this);
            mService.setShutdownCountTime(13);
            mService.bind();
            mService.getData(new CommunicationService.IProcessData() {
                @Override
                public void process(byte[] data, DataType type) {
                    Log.e("gh0st FactorySuite", "type:" + type.name() + ",data:" + saveHex2String(data));
                    if (type == DataType.TGPIO) {//GPIO
                        if (0x12 == data[0]) {
                            if (0x01 == data[1]) {//高
                                Log.i("gh0st", "高");
                                startA();
                            } else {//低
                                Log.i("gh0st", "低");
                                stopServiceActivity();
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(final byte[] data) {
        try {
            if (mService != null && data != null) {
                if (mService.isBindSuccess()) {
                    mService.send(data);
                    Log.i("gh0st", "write:" + saveHex2String(data));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String saveHex2String(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        for (byte aData : data) {
            int value = aData & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            try {
                mService.unbind();
                mService = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startA() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void stopServiceActivity() {
        Intent intent = new Intent("android.intent.action.FINISH_SERVICE_ACTIVITY");
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
