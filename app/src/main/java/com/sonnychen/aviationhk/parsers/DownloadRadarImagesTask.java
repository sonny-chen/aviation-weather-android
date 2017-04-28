/**
 * This file is part of AviationHK - companion app for local pilots
 * that provides at-a-glance weather information.
 * <p>
 * Project site: https://github.com/sonny-chen/aviation-weather-android
 * <p>
 * AviationHK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * AviationHK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with AviationHK.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Created by Sonny Chen on 4/28/2017.
 **/

package com.sonnychen.aviationhk.parsers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.utils.SimpleCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sonnychen.aviationhk.BaseApplication.CACHE_EXPIRY_SECONDS;
import static com.sonnychen.aviationhk.BaseApplication.RADAR_FRAME_DURATION;

public class DownloadRadarImagesTask extends AsyncTask<Void, Integer, Boolean> {
    private Context context;
    private HKOData.DataType dataType;
    HKOData.BasicSyncCallback callback;
    private HKOData data;

    public DownloadRadarImagesTask(Context context, HKOData.DataType dataType, HKOData data, final HKOData.BasicSyncCallback callback) {
        this.context = context;
        this.dataType = dataType;
        this.data = data;
        this.callback = callback;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (callback != null) callback.onProgressUpdate(dataType, values[0], values[1]);
    }

    protected Boolean doInBackground(Void... params) {
        try {
            int count = 0;
            // build animation
            if (dataType == HKOData.DataType.RADAR64) {
                data.Radar_Animation64 = new AnimationDrawable();
                for (String url : data.Radar_Animation64URLs) {
                    Bitmap bitmap = DownloadImage(url);
                    if (bitmap != null)
                        data.Radar_Animation64.addFrame(new BitmapDrawable(context.getResources(), bitmap), RADAR_FRAME_DURATION);
                    publishProgress(++count, data.Radar_Animation64URLs.size());
                }
            }
//                Radar_Animation64.setOneShot(false);
            if (dataType == HKOData.DataType.RADAR128) {
                data.Radar_Animation128 = new AnimationDrawable();
                for (String url : data.Radar_Animation128URLs) {
                    Bitmap bitmap = DownloadImage(url);
                    if (bitmap != null)
                        data.Radar_Animation128.addFrame(new BitmapDrawable(context.getResources(), bitmap), RADAR_FRAME_DURATION);
                    publishProgress(++count, data.Radar_Animation128URLs.size());
                }
            }
//                Radar_Animation128.setOneShot(false);
            if (dataType == HKOData.DataType.RADAR256) {
                data.Radar_Animation256 = new AnimationDrawable();
                for (String url : data.Radar_Animation256URLs) {
                    Bitmap bitmap = DownloadImage(url);
                    if (bitmap != null)
                        data.Radar_Animation256.addFrame(new BitmapDrawable(context.getResources(), bitmap), RADAR_FRAME_DURATION);
                    publishProgress(++count, data.Radar_Animation256URLs.size());
                }
//                Radar_Animation256.setOneShot(false);

                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //publishProgress((int) ((i / (float) count) * 100));
        // Escape early if cancel() is called
        return false;
    }

    private Bitmap DownloadImage(String url) throws IOException {
        SimpleCache.BitmapEntry cache = BaseApplication.Cache.getBitmap(url);
        Date expiry = new Date(System.currentTimeMillis() - CACHE_EXPIRY_SECONDS * 1000); // 10 minutes
        if (cache != null && cache.getMetadata() != null && cache.getMetadata().containsKey("Date") && ((Date) cache.getMetadata().get("Date")).compareTo(expiry) >= 0) {
            System.out.println("Cache Found: " + url);
            return cache.getBitmap();
        }

        URL link = new URL(url);
        URLConnection connection = link.openConnection();
        connection.connect();
        // getting file length
        int lengthOfFile = connection.getContentLength();
        InputStream input = connection.getInputStream();
        byte data[] = new byte[4096];
        byte fileData[] = new byte[lengthOfFile];
        int total = 0;
        int count;
        while ((count = input.read(data)) != -1) {
            System.arraycopy(data, 0, fileData, total, count);
            total += count;
        }

        Bitmap image = BitmapFactory.decodeByteArray(fileData, 0, lengthOfFile);

        if (image == null) {
            Log.wtf("DownloadImage", "Decode ERROR: " + url);
            return null;
        }

        Map<String, Date> meta = new HashMap<>();
        meta.put("Date", new Date());
        OutputStream stream = BaseApplication.Cache.openStream(url, meta);
        //image.compress(Bitmap.CompressFormat.JPEG, 100, stream); // causes getBitmap() to fail
        stream.write(fileData);
        stream.flush();
        stream.close();
        System.out.println("Cached " + url);

        return image;
    }
}