package com.github.usbcamera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private var mReceiver: Receiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit()
        }
        val filter = IntentFilter("android.intent.action.FINISH_SERVICE_ACTIVITY")
        mReceiver = Receiver()
        registerReceiver(mReceiver, filter)
    }

    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if ("android.intent.action.FINISH_SERVICE_ACTIVITY" == action) {
                finish();
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) unregisterReceiver(mReceiver)
    }
}