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
 * Created by Sonny Chen on 4/25/2017.
 **/

package com.sonnychen.aviationhk.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.R;
import com.sonnychen.aviationhk.parsers.BasicSyncCallback;
import com.sonnychen.aviationhk.parsers.BasicSyncCallback.DataType;
import com.sonnychen.aviationhk.parsers.HKOData;
import com.sonnychen.aviationhk.parsers.HKORss;
import com.sonnychen.aviationhk.utils.GenericCardItem;
import com.sonnychen.aviationhk.utils.GenericRecyclerViewAdapter;
import com.sonnychen.aviationhk.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static com.sonnychen.aviationhk.parsers.BasicSyncCallback.DataType.*;
import static com.sonnychen.aviationhk.parsers.BasicSyncCallback.DataType.FORECASTS;
import static com.sonnychen.aviationhk.utils.Utils.getMaxNumberOfFittedColumns;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomFragmentBase.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends CustomFragmentBase {
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // HTML parsed
    TextView mWind;
    TextView mQNH;
    TextView mWeather;
    TextView mCloudBase;
    TextView mVisibility;
    TextView mVisibilityLocal;

    TextView mVHSKTemperature;
    TextView mVHSKWind;
    TextView mVHSKPressure;

    // RSS
    TextView mLocalForecast;
    TextView mGeneralSituation;
    RecyclerView mExtendedForecasts;
    TextView mWeatherWarnings;

    // weather forecast
    ArrayList<GenericCardItem> cardList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Log.v("HOME-UI", "Starting");
        mQNH = ((TextView) view.findViewById(R.id.txtQNH));
        mQNH.setText(BaseApplication.Data.METAR_QNH);
        mWeather = ((TextView) view.findViewById(R.id.txtWeather));
        mWeather.setText(BaseApplication.Data.METAR_Weather);
        mCloudBase = ((TextView) view.findViewById(R.id.txtCloud));
        if (BaseApplication.Data.METAR_Weather.startsWith("No significant"))
            mCloudBase.setTextColor(Color.parseColor("#006600"));

        mWeatherWarnings = ((TextView) view.findViewById(R.id.txtWeatherWarnings));
        mWeatherWarnings.setText(Html.fromHtml(Arrays.toString(BaseApplication.RssData.WeatherWarnings)));
        mLocalForecast = ((TextView) view.findViewById(R.id.txtLocalForecast));
        if (BaseApplication.RssData.LocalWeatherForecastDescription != null)
            mLocalForecast.setText(Html.fromHtml(BaseApplication.RssData.LocalWeatherForecastDescription));
        mGeneralSituation = ((TextView) view.findViewById(R.id.txtLocalForecast));
        if (BaseApplication.RssData.GeneralSituation != null)
            mLocalForecast.setText(Html.fromHtml(BaseApplication.RssData.GeneralSituation));

        String[] data;
        // set label colors
        mWind = ((TextView) view.findViewById(R.id.txtWind));
        if (!TextUtils.isEmpty(BaseApplication.Data.METAR_Wind)) {
            mWind.setText(BaseApplication.Data.METAR_Wind);
            data = BaseApplication.Data.METAR_Wind.split(" ");
            if (Utils.isInteger(data[0], 10)) {
                // minima: 2000 ft SEC, 1200 ft SKARA
                int value = Integer.parseInt(data[0]);
                if (value > 20) mWind.setTextColor(Color.RED);
                else mWind.setTextColor(Color.BLACK);
            } else mWind.setTextColor(Color.BLACK);
        }

        mCloudBase = ((TextView) view.findViewById(R.id.txtCloud));
        if (!TextUtils.isEmpty(BaseApplication.Data.METAR_CloudBase)) {
            mCloudBase.setText(BaseApplication.Data.METAR_CloudBase);
            data = BaseApplication.Data.METAR_CloudBase.split(" ");
            if (Utils.isInteger(data[0], 10)) {
                // minima: 2000 ft SEC, 1200 ft SKARA
                int value = Integer.parseInt(data[0]);
                if (value > 2000) mCloudBase.setTextColor(Color.parseColor("#006600"));
                else if (value > 1200) mCloudBase.setTextColor(Color.parseColor("#e67300"));
                else mCloudBase.setTextColor(Color.RED);
            } else mCloudBase.setTextColor(Color.BLACK);
        }

        mVisibility = ((TextView) view.findViewById(R.id.txtVisibility));
        if (!TextUtils.isEmpty(BaseApplication.Data.METAR_Visibility)) {
            mVisibility.setText(BaseApplication.Data.METAR_Visibility);
            data = BaseApplication.Data.METAR_Visibility.split(" ");
            if (Utils.isInteger(data[0], 10)) {
                // data format: 7 km 3000 m
                // minima: 5 km SKARA/SEC
                int value = Integer.parseInt(data[0]);
                if (value > 3 && value <= 10) mVisibility.setTextColor(Color.parseColor("#006600"));
                else if (value == 3000) mVisibility.setTextColor(Color.parseColor("#e67300"));
                else mVisibility.setTextColor(Color.RED);
            } else mVisibility.setTextColor(Color.BLACK);
        }

        mVisibilityLocal = ((TextView) view.findViewById(R.id.txtVisibilityLocal));
        mVHSKTemperature = ((TextView) view.findViewById(R.id.txtVHSKTemperature));
        mVHSKWind = ((TextView) view.findViewById(R.id.txtVHSKWind));
        mVHSKPressure = ((TextView) view.findViewById(R.id.txtVHSKPressure));
        mExtendedForecasts = ((RecyclerView) view.findViewById(R.id.extendedForecasts));

        if (BaseApplication.Data.VHSK_Temperature_Celsius > 0)
            bindVHSKReadings(VHSK);
        if (BaseApplication.RssData.WeatherForecasts != null)
            bindVHSKReadings(FORECASTS);


        return view;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("HomeFragment", "Broadcast received: " + intent.toString());

            // wait for METAR to finish before loading UI
            if (intent.hasExtra(BaseApplication.SYNC_EVENT_PARAM))
                bindVHSKReadings(valueOf(intent.getStringExtra(BaseApplication.SYNC_EVENT_PARAM)));
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // register for sync event broadcasts
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                new IntentFilter(BaseApplication.SYNC_EVENT));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }


    private void bindVHSKReadings(DataType dataType) {
        Log.v("HomeFragment", "bindVHSKReadings: " + dataType.toString());
        switch (dataType)
        {
            case FORECASTS:
                StringBuilder sb = new StringBuilder();
                for (HKOData.Visibility visibility : BaseApplication.Data.VisibilityReadings)
                    sb.append(String.format(Locale.ENGLISH, "%s: %.0f km\n", visibility.Location, visibility.Visibility_10min_KM));
                mVisibilityLocal.setText(sb.toString());

                if (BaseApplication.RssData.WeatherForecasts != null) {
                    cardList = new ArrayList<>();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM", Locale.ENGLISH);
                    for (HKORss.WeatherForecast forecast : BaseApplication.RssData.WeatherForecasts) {
                        cardList.add(new GenericCardItem(forecast.Date != null ? forecast.Date.toString() : "", forecast.WeatherCartoonURL, String.format(Locale.ENGLISH, "%s<br />%s<br />%s<br />%s", forecast.Date != null ? dateFormat.format(forecast.Date) : "", forecast.Weather, forecast.TemperatureRange, forecast.Wind)));
                    }
                    GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                            getMaxNumberOfFittedColumns(getActivity(), 100), LinearLayoutManager.VERTICAL, false);
                    mLayoutManager.setAutoMeasureEnabled(true);
                    mExtendedForecasts.setLayoutManager(mLayoutManager);
                    mExtendedForecasts.setHasFixedSize(false);
                    mExtendedForecasts.setNestedScrollingEnabled(false);
                    mExtendedForecasts.setAdapter(new GenericRecyclerViewAdapter(getContext(), cardList));
                }
                break;
            case VHSK:
                mVHSKTemperature.setText(String.format(Locale.ENGLISH, "%.1f°C (%.1f°C ~ %.1f°C)",
                        BaseApplication.Data.VHSK_Temperature_Celsius,
                        BaseApplication.Data.VHSK_TemperatureMin_Celsius,
                        BaseApplication.Data.VHSK_TemperatureMax_Celsius));
                mVHSKWind.setText(String.format(Locale.ENGLISH, "%s %d kts (c/w %d kts)",
                        BaseApplication.Data.VHSK_WindDirection,
                        Math.round(BaseApplication.Data.VHSK_Wind_Knots),
                        Math.round(BaseApplication.Data.VHSK_CrossWind_Knots)));
                mVHSKPressure.setText(String.format(Locale.ENGLISH, "%.0f hPa", BaseApplication.Data.VHSK_Pressure_hPa));

                break;
        }
    }


}
