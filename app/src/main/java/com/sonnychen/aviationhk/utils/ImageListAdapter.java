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

package com.sonnychen.aviationhk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.sonnychen.aviationhk.AsyncImageTask;
import com.sonnychen.aviationhk.R;

import java.net.MalformedURLException;

    public class ImageListAdapter extends ArrayAdapter<String> {

        private String[] urls;

        public ImageListAdapter(Context context, String[] urls) {
            super(context, 0, urls);
            this.urls = urls;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.card_item_vertical, null);
            }

            String url = urls[position];
            if (url != null) {
                final ImageView iv = (ImageView) v.findViewById(R.id.imageView);
                final ProgressBar pb = (ProgressBar) v.findViewById(R.id.progress);
                try {
                    AsyncImageTask.DownloadFile(url, new AsyncImageTask.AsyncDownloadCallbackInterface() {

                        @Override
                        public void onDownloadStarting() {
                            pb.setIndeterminate(true);
                        }

                        @Override
                        public void onDownloadProgress(int bytesReceived, int bytesTotal) {
                            pb.setIndeterminate(false);
                            pb.setMax(bytesTotal);
                            pb.setProgress(bytesReceived);
                        }

                        @Override
                        public void onDownloadFinished(Bitmap[] data) {
                            pb.setVisibility(View.GONE);
                            iv.setImageBitmap(data[0]);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }

            return v;
        }
    }

