package com.example.twovideotest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SettingStandardActivity extends AppCompatActivity {

    private boolean hasPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard);
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
        Button open = (Button) findViewById(R.id.btn_open_camera);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startMainActivity();
               if ("E9631".equalsIgnoreCase(Build.DEVICE) || "E9638".equalsIgnoreCase(Build.DEVICE)) {
                    startActivity(new Intent(SettingStandardActivity.this, MainActivity.class));
                } else if ("E9635".equalsIgnoreCase(Build.DEVICE)) {
                    String value = FileUtils.getNodeValue();
                    if ("0".equals(value)) {
                        startActivity(new Intent(SettingStandardActivity.this, MainActivity7in.class));
                    } else {
                        startActivity(new Intent(SettingStandardActivity.this, FourVideoActivity.class));
                    }
                } else {
                    startActivity(new Intent(SettingStandardActivity.this, FourVideoActivity.class));
                }
            }
        });
        findViewById(R.id.btn_setting_standard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        hasPermission = grantResults.length > 0 && requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            Toast.makeText(SettingStandardActivity.this, "no permissions", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startMainActivity() {
        if (hasPermission) {
            startActivity(new Intent(SettingStandardActivity.this, FourVideoActivity.class));
        } else requestPermission();
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

    int tempStandard = 1;

    public void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_standard));
        final String[] zhis = new String[]{"NTSC", "PAL"};
        builder.setSingleChoiceItems(zhis, Constants.zhi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                tempStandard = i;
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SPUtils.setSP(SettingStandardActivity.this, Constants.STANDARD_KEY, tempStandard);
                Toast.makeText(SettingStandardActivity.this, getString(R.string.dialog_tips) + zhis[tempStandard], Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

}
