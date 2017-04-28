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

/**
 * Created by Sonny on 4/24/2017.
 */

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
                v = vi.inflate(R.layout.image_list_item, null);
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

