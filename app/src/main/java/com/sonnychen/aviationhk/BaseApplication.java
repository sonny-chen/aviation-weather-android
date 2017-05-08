/**
 This file is part of AviationHK - companion app for local pilots
 that provides at-a-glance weather information.

 Project site: https://github.com/sonny-chen/aviation-weather-android

 AviationHK is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 AviationHK is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with AviationHK.  If not, see <http://www.gnu.org/licenses/>.

 Created by Sonny Chen on 4/25/2017.
 **/

package com.sonnychen.aviationhk;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sonnychen.aviationhk.parsers.BasicSyncCallback;
import com.sonnychen.aviationhk.parsers.HKOData;
import com.sonnychen.aviationhk.parsers.HKORss;
import com.sonnychen.aviationhk.utils.SimpleCache;

import java.io.IOException;

public class BaseApplication extends Application {
    public static final int RADAR_FRAME_DURATION = 300;
    public static final int CACHE_EXPIRY_SECONDS = 600;

    public static final String SYNC_EVENT = "SYNC";
    public static final String SYNC_EVENT_PARAM = "EVENT";

    // RSS data
    public static HKORss RssData;
    // HTML parsed data
    public static HKOData Data;
    public static SimpleCache Cache;

    // user settings
    public static String HKACPassword;

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

        BaseApplication.Data = new HKOData(this, new BasicSyncCallback() {
            @Override
            public void onProgressUpdate(DataType dataType, int progress, int max) {

            }

            @Override
            public void onSyncFinished(DataType dataType, boolean success) {
                if (!success) return;

                Intent intent = new Intent(SYNC_EVENT);
                intent.putExtra(SYNC_EVENT_PARAM, dataType.toString());
                LocalBroadcastManager.getInstance(BaseApplication.this).sendBroadcast(intent);
            }
        });

        BaseApplication.RssData = new HKORss(this, new BasicSyncCallback() {
            @Override
            public void onProgressUpdate(DataType dataType, int progress, int max) {

            }

            @Override
            public void onSyncFinished(DataType dataType, boolean success) {
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
