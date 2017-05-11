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
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sonnychen.aviationhk.AsyncImageTask;
import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.LightboxActivity;
import com.sonnychen.aviationhk.R;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class GenericRecyclerViewAdapter extends RecyclerView.Adapter<GenericRecyclerViewAdapter.CustomViewHolder> {
    private List<GenericCardItem> feedItemList;
    private Context mContext;
    private ArrayList<String> URLs;
    private int CellLayoutOrientation;

    public GenericRecyclerViewAdapter(Context context, List<GenericCardItem> feedItemList, int CellLayoutOrientation) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        URLs = new ArrayList<>();
        for (int i = 0; i < feedItemList.size(); i++)
            URLs.add(feedItemList.get(i).ImageURL);
        this.CellLayoutOrientation = CellLayoutOrientation;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                CellLayoutOrientation == LinearLayout.VERTICAL ? R.layout.card_item_vertical :
                        R.layout.card_item_horizontal, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, final int i) {
        final GenericCardItem feedItem = feedItemList.get(i);

        //Setting text view title
        customViewHolder.textView.setText(Html.fromHtml(feedItem.Text));

        customViewHolder.imageView.setImageBitmap(null);
        //Render image using Picasso library
        if (!TextUtils.isEmpty(feedItem.ImageURL)) {
            // special cases: animations
            if (feedItem.ImageURL.contains("/radar/R1")
                    && BaseApplication.Data.Radar_Animation64 != null
                    && BaseApplication.Data.Radar_Animation64.getNumberOfFrames() > 0) {
                customViewHolder.progressBar.setVisibility(View.GONE);
                customViewHolder.imageView.setImageDrawable(BaseApplication.Data.Radar_Animation64);
                BaseApplication.Data.Radar_Animation64.start();
            } else { // regular images
                try {
                    customViewHolder.imageView.setTag(feedItem.ImageURL);
                    AsyncImageTask.DownloadFile(feedItem.ImageURL, new AsyncImageTask.AsyncDownloadCallbackInterface() {

                        @Override
                        public void onDownloadStarting() {
                            if (customViewHolder.imageView.getTag() == null || !customViewHolder.imageView.getTag().equals(feedItem.ImageURL))
                                return;

                            customViewHolder.progressBar.setVisibility(View.VISIBLE);
                            customViewHolder.progressBar.setIndeterminate(true);
                        }

                        @Override
                        public void onDownloadProgress(int bytesReceived, int bytesTotal) {
                            if (customViewHolder.imageView.getTag() == null || !customViewHolder.imageView.getTag().equals(feedItem.ImageURL))
                                return;

                            customViewHolder.progressBar.setIndeterminate(false);
                            customViewHolder.progressBar.setMax(bytesTotal);
                            customViewHolder.progressBar.setProgress(bytesReceived);
                        }

                        @Override
                        public void onDownloadFinished(Bitmap[] data) {
                            // don't applying image if cell is already out of view
                            if (customViewHolder.imageView.getTag() == null || !customViewHolder.imageView.getTag().equals(feedItem.ImageURL))
                                return;

                            customViewHolder.progressBar.setVisibility(View.GONE);
                            customViewHolder.imageView.setImageBitmap(data[0]);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            customViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, LightboxActivity.class);
                    intent.putStringArrayListExtra(LightboxActivity.INTENT_URLS_PARAM, URLs);
                    intent.putExtra(LightboxActivity.INTENT_INDEX_PARAM, i);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        ProgressBar progressBar;

        CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.imageView);
            this.textView = (TextView) view.findViewById(R.id.title);
            this.progressBar = (ProgressBar) view.findViewById(R.id.progress);
        }
    }
}