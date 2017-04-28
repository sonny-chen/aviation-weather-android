package com.sonnychen.metarhk.utils;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sonnychen.metarhk.AsyncImageTask;
import com.sonnychen.metarhk.BaseApplication;
import com.sonnychen.metarhk.LightboxActivity;
import com.sonnychen.metarhk.R;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sonny on 4/25/2017.
 */

public class GenericRecyclerViewAdapter extends RecyclerView.Adapter<GenericRecyclerViewAdapter.CustomViewHolder> {
    private List<GenericCardItem> feedItemList;
    private Context mContext;
    private ArrayList<String> URLs;

    public GenericRecyclerViewAdapter(Context context, List<GenericCardItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        URLs = new ArrayList<>();
        for (int i = 0; i < feedItemList.size(); i++)
            URLs.add(feedItemList.get(i).ImageURL);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_list_item, viewGroup, false);
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
            if (feedItem.ImageURL.contains("/radar/R1") && BaseApplication.Data.Radar_Animation64 != null &&
                    BaseApplication.Data.Radar_Animation64.getNumberOfFrames() > 0) {
                customViewHolder.progressBar.setVisibility(View.GONE);
                customViewHolder.imageView.setImageDrawable(BaseApplication.Data.Radar_Animation64);
                BaseApplication.Data.Radar_Animation64.start();
            } else { // regular images
                try {
                    customViewHolder.imageView.setTag(feedItem.ImageURL);
                    AsyncImageTask.DownloadFile(feedItem.ImageURL, new AsyncImageTask.AsyncDownloadCallbackInterface() {

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
        protected ImageView imageView;
        protected TextView textView;
        protected ProgressBar progressBar;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.imageView);
            this.textView = (TextView) view.findViewById(R.id.title);
            this.progressBar = (ProgressBar) view.findViewById(R.id.progress);
        }
    }
}