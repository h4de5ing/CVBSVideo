package com.example.twovideotest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class ZhiActivity extends AppCompatActivity {

    private boolean hasPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhi);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, 0);
            hasPermission = false;
        } else {
            hasPermission = true;
        }
        findViewById(R.id.btn_open_p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SPUtils.setSP(ZhiActivity.this, "zhi", 1);
                Constacts.zhi = 1;
                startMainActivity();
            }
        });
        findViewById(R.id.btn_open_n).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SPUtils.setSP(ZhiActivity.this, "zhi", 0);
                Constacts.zhi = 0;
                startMainActivity();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        hasPermission = grantResults.length > 0 && requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            Toast.makeText(ZhiActivity.this, "没有权限,程序无法运行", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startMainActivity() {
        if (hasPermission) startActivity(new Intent(ZhiActivity.this, MainActivity.class));
        else requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, 0);
            hasPermission = false;
        }
    }
}
