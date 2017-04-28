package com.sonnychen.metarhk;

/**
 * Created by Sonny on 4/25/2017.
 */

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sonnychen.metarhk.parsers.HKOData;
import com.sonnychen.metarhk.utils.SimpleCache;

import java.io.IOException;

public class BaseApplication extends Application {
    public static final String SYNC_EVENT = "SYNC";
    public static final String SYNC_EVENT_PARAM = "EVENT";
    public static HKOData Data;
    public static SimpleCache Cache;
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!
        Log.v("APP", "Starting");
        try {
            Cache = SimpleCache.open(this.getFilesDir(), 1, 10000000); // 10MB
        } catch (IOException e) {
            e.printStackTrace();
        }

        BaseApplication.Data = new HKOData(this, new HKOData.BasicSyncCallback() {
            @Override
            public void onSyncFinished(HKOData.DataType dataType, boolean success) {
                Intent intent = new Intent(SYNC_EVENT);
                intent.putExtra(SYNC_EVENT_PARAM, dataType.toString());
                LocalBroadcastManager.getInstance(BaseApplication.this).sendBroadcast(intent);
            }
        });
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
