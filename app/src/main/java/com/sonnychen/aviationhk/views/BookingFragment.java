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
 * Created by Sonny Chen on 5/3/2017.
 **/

package com.sonnychen.aviationhk.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.R;
import com.sonnychen.aviationhk.parsers.HKOData;
import com.sonnychen.aviationhk.utils.SimpleCache;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sonnychen.aviationhk.BaseApplication.CACHE_EXPIRY_SECONDS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomFragmentBase.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BookingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookingFragment extends CustomFragmentBase {
    public BookingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MetarFragment.
     */
    public static BookingFragment newInstance() {
        return new BookingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    WebView mWebView;
    ProgressBar mProgressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("BOOKING-UI", "Starting");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_booking, container, false);
        mProgressBar = ((ProgressBar) view.findViewById(R.id.progress));
        mWebView = ((WebView) view.findViewById(R.id.booking_html));

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBuiltInZoomControls(true);

        mWebView.setWebChromeClient(new WebChromeClient());

        // download PDF async
        new DownloadFileTask() {
            @Override
            protected void onPostExecute(Byte[] data) {
                // save data to file
                try {
                    FileOutputStream fileOutputStream = getActivity().openFileOutput("booking.pdf", Context.MODE_PRIVATE);
                    fileOutputStream.write(getbytes(data));
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                displayPDF();
            }
        }.execute("http://aviationclub.hk/doc/fixwing_booking2.pdf"); // unfortunately HAKC does not have a valid SSL certificate
        return view;
    }

    private void displayPDF() {
        mProgressBar.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);

        Uri path = Uri.parse(getActivity().getFilesDir() + "/booking.pdf");
        try {
            InputStream ims = getActivity().getAssets().open("pdfviewer/index.html");
            String line = getStringFromInputStream(ims);
            line = line.replace("THE_FILE", path.toString());
            line = line.replace("THE_PASSWORD", BaseApplication.HKACPassword);
            FileOutputStream fileOutputStream = getActivity().openFileOutput("pdfviewer.html", Context.MODE_PRIVATE);
            fileOutputStream.write((line.getBytes()));
            fileOutputStream.close();
            mWebView.loadUrl("file://" + getActivity().getFilesDir() + "/pdfviewer.html");
            mProgressBar.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private class DownloadFileTask extends AsyncTask<String, Integer, Byte[]> {
        protected Byte[] doInBackground(String... URLs) {
            String url = URLs[0];
            try {
                SimpleCache.InputStreamEntry cache = BaseApplication.Cache.getInputStream(url);
                Date expiry = new Date(System.currentTimeMillis() - CACHE_EXPIRY_SECONDS * 1000); // 10 minutes
                if (cache != null && cache.getMetadata() != null && cache.getMetadata().containsKey("Date") && ((Date) cache.getMetadata().get("Date")).compareTo(expiry) >= 0) {
                    System.out.println("Cache Found: " + url);
                    byte[] bytes = new byte[(int) cache.getLength()];
                    cache.getInputStream().read(bytes, 0, bytes.length);

                    return getBytes(bytes);
                }

                URL link = new URL(url);
                URLConnection connection = link.openConnection();
                connection.connect();
                // getting file length
                InputStream input = connection.getInputStream();
                byte data[] = new byte[4096];
                byte fileData[] = new byte[connection.getContentLength()];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    System.arraycopy(data, 0, fileData, total, count);
                    total += count;
                }

                // put in cache
                Map<String, Date> meta = new HashMap<>();
                meta.put("Date", new Date());
                OutputStream stream = BaseApplication.Cache.openStream(url, meta);
                stream.write(fileData);
                stream.flush();
                stream.close();
                System.out.println("Cached " + url);

                return getBytes(fileData);

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }

    @NonNull
    private Byte[] getBytes(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];
        int i = 0;
        for (byte b : bytes)
            byteObjects[i++] = b;  // Autoboxing
        return byteObjects;
    }

    @NonNull
    private byte[] getbytes(Byte[] byteObjects) {
        byte[] bytes = new byte[byteObjects.length];
        int i = 0;
        for (Byte b : byteObjects)
            bytes[i++] = b; // Autounboxing
        return bytes;
    }
}
