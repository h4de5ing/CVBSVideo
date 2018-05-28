package com.unistrong.dvr.camera2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.unistrong.dvr.R;

public class GoogleSamplesCamera2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_samples_camera2);
        if (null == savedInstanceState) {
            //getFragmentManager().beginTransaction().replace(R.id.fl0, Camera2VideoFragment.newInstance()).commit();
            //getFragmentManager().beginTransaction().replace(R.id.fl1, Camera2VideoFragment7.newInstance()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.fl0, Camera2BasicFragment.newInstance()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.fl1, Camera2BasicFragment7.newInstance()).commit();
        }
    }
}
