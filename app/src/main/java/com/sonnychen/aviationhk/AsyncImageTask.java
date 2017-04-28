package com.sonnychen.aviationhk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.sonnychen.aviationhk.utils.SimpleCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sonnychen.aviationhk.parsers.HKOData.CACHE_EXPIRY_SECONDS;

/**
 * Created by Sonny on 4/24/2017.
 */

public class AsyncImageTask extends AsyncTask<URL, Integer, Bitmap[]> {
    //define callback interface
    public interface AsyncDownloadCallbackInterface {
        void onDownloadStarting();

        void onDownloadProgress(int bytesReceived, int bytesTotal);

        void onDownloadFinished(Bitmap[] data);
    }

    protected Bitmap[] doInBackground(URL... urls) {
        int urlCount = urls.length;
        Bitmap[] downloadedData = new Bitmap[urlCount];
        for (int i = 0; i < urlCount; i++) {
            // try to get file from cache
            try {
                SimpleCache.BitmapEntry cache = BaseApplication.Cache.getBitmap(urls[i].toString());
                Date expiry = new Date(System.currentTimeMillis() - CACHE_EXPIRY_SECONDS * 1000); // 10 minutes
                if (cache != null && cache.getMetadata() != null && cache.getMetadata().containsKey("Date") && ((Date) cache.getMetadata().get("Date")).compareTo(expiry) >= 0) {
                    System.out.println("Cache Found: " + urls[i].toString());
                    downloadedData[i] = cache.getBitmap();
                    continue;
                }

                System.out.println("Downloading " + urls[i].toString());
                URLConnection connection = urls[i].openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();
                InputStream input = connection.getInputStream();
                byte data[] = new byte[4096];
                byte fileData[] = new byte[lengthOfFile];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    System.arraycopy(data, 0, fileData, total, count);
                    total += count;
                    // publishing the progress....
                    if (lengthOfFile > 0) // only if total length is known
                        publishProgress(total, lengthOfFile);
                }

                //downloadedData[i] = BitmapFactory.decodeStream(connection.getInputStream());
                downloadedData[i] = BitmapFactory.decodeByteArray(fileData, 0, lengthOfFile);

                Map<String, Date> meta = new HashMap<>();
                meta.put("Date", new Date());
                OutputStream stream = BaseApplication.Cache.openStream(urls[i].toString(), meta);
                //downloadedData[i].compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.write(fileData);
                stream.flush();
                stream.close();
                System.out.println("Cached " + urls[i].toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        return downloadedData;
    }

    //your method slightly modified to take callback into account
    public static void DownloadFile(String Url, final AsyncDownloadCallbackInterface callback) throws MalformedURLException {
        new AsyncImageTask() {
            @Override
            protected void onPreExecute() {
                //super.onPreExecute();
                callback.onDownloadStarting();
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                callback.onDownloadProgress(progress[0].intValue(), progress[1].intValue());
            }

            @Override
            protected void onPostExecute(Bitmap[] data) {
                //super.onPostExecute(data);
                callback.onDownloadFinished(data);
            }
        }.execute(new URL(Url));
    }

}
