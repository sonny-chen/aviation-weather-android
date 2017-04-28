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

 Created by Sonny Chen on 4/27/2017.
 **/

package com.sonnychen.aviationhk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;

import com.sonnychen.aviationhk.utils.ExtendedViewPager;
import com.sonnychen.aviationhk.utils.TouchImageView;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class LightboxActivity extends Activity {
    public static final String INTENT_URLS_PARAM ="URLS";
    public static final String INTENT_INDEX_PARAM ="INDEX";

    ExtendedViewPager mLightBox;
    ImagesAdapter mImagesAdapter;
    ArrayList<String> mURLs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // go full screen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        if (intent == null || (!intent.hasExtra(INTENT_URLS_PARAM))) {
            finish();
            return;
        }

        setContentView(R.layout.activity_lightbox);

        // load images from intent
        mURLs = intent.getStringArrayListExtra(INTENT_URLS_PARAM);
        mLightBox = (ExtendedViewPager) findViewById(R.id.lightbox);
        mImagesAdapter = new ImagesAdapter();
        mLightBox.setAdapter(mImagesAdapter);
        mLightBox.setCurrentItem(intent.getIntExtra(INTENT_INDEX_PARAM, 0));

        Log.v(INTENT_URLS_PARAM, mURLs.toString());
        Log.v(INTENT_INDEX_PARAM, mURLs.toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mImagesAdapter.notifyDataSetChanged();
    }

    private class ImagesAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mURLs.size();
        }

        // this is required for notifyDataSetChanged to work
        // http://stackoverflow.com/questions/7263291/viewpager-pageradapter-not-updating-the-view
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (position >= mURLs.size())
                return null;

            String url = mURLs.get(position);
            Log.d("LightBoxActivity", "init #" + position + " url: " + url);
            final TouchImageView imgView = new TouchImageView(LightboxActivity.this);
            imgView.setLayoutParams(new LayoutParams(960, 520));
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imgView.setTag(url);

            imgView.setOnClickListener(null);
            if (url == null) return null;
            if (url.contains("/radar/R1") && BaseApplication.Data.Radar_Animation64.getNumberOfFrames() > 0) {
                imgView.setImageDrawable(BaseApplication.Data.Radar_Animation64);
                BaseApplication.Data.Radar_Animation64.start();
                imgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (imgView.getDrawable() == BaseApplication.Data.Radar_Animation64 && BaseApplication.Data.Radar_Animation128.getNumberOfFrames() > 0) {
                            imgView.setImageDrawable(BaseApplication.Data.Radar_Animation128);
                            BaseApplication.Data.Radar_Animation128.start();
                        } else if (imgView.getDrawable() == BaseApplication.Data.Radar_Animation128 && BaseApplication.Data.Radar_Animation256.getNumberOfFrames() > 0) {
                            imgView.setImageDrawable(BaseApplication.Data.Radar_Animation256);
                            BaseApplication.Data.Radar_Animation256.start();
                        } else {
                            imgView.setImageDrawable(BaseApplication.Data.Radar_Animation64);
                            BaseApplication.Data.Radar_Animation64.start();
                        }
                    }
                });
            } else if (url.startsWith("base64:")) {
                imgView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                // base64 (embedded image)
                final byte[] decodedBytes = Base64.decode(url.substring(7), Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgView.setImageBitmap(decodedImage);
            } else if (url.startsWith("/")) {
                // local path
                Bitmap image = BitmapFactory.decodeFile(url);
                imgView.setImageBitmap(image);
            } else {
                // download url
                try {
                    AsyncImageTask.DownloadFile(url, new AsyncImageTask.AsyncDownloadCallbackInterface() {

                        @Override
                        public void onDownloadStarting() {
//                            customViewHolder.progressBar.setVisibility(View.VISIBLE);
//                            customViewHolder.progressBar.setIndeterminate(true);
                        }

                        @Override
                        public void onDownloadProgress(int bytesReceived, int bytesTotal) {
//                            customViewHolder.progressBar.setIndeterminate(false);
//                            customViewHolder.progressBar.setMax(bytesTotal);
//                            customViewHolder.progressBar.setProgress(bytesReceived);
                        }

                        @Override
                        public void onDownloadFinished(Bitmap[] data) {
//                            customViewHolder.progressBar.setVisibility(View.GONE);
                            imgView.setImageBitmap(data[0]);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            container.addView(imgView);

            return imgView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            Log.d("LightBoxActivity", "destory #" + position);
            container.removeView((ImageView) view);
            try {
                if (view instanceof TouchImageView)
                    ((TouchImageView) view).setImageBitmap(null);
                view = null; // for gc
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
