package com.sonnychen.aviationhk.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.R;
import com.sonnychen.aviationhk.utils.Utils;

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

    TextView mWind;
    TextView mQNH;
    TextView mWeather;
    TextView mCloudBase;
    TextView mVisibility;

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

        // set label colors
        mWind = ((TextView) view.findViewById(R.id.txtWind));
        mWind.setText(BaseApplication.Data.METAR_Wind);
        String[] data = BaseApplication.Data.METAR_Wind.split(" ");
        if (Utils.isInteger(data[0], 10)) {
            // minima: 2000 ft SEC, 1200 ft SKARA
            int value = Integer.parseInt(data[0]);
            if (value > 20) mWind.setTextColor(Color.RED);
            else mWind.setTextColor(Color.BLACK);
        } else mWind.setTextColor(Color.BLACK);

        mCloudBase = ((TextView) view.findViewById(R.id.txtCloud));
        mCloudBase.setText(BaseApplication.Data.METAR_CloudBase);
        data = BaseApplication.Data.METAR_CloudBase.split(" ");
        if (Utils.isInteger(data[0], 10)) {
            // minima: 2000 ft SEC, 1200 ft SKARA
            int value = Integer.parseInt(data[0]);
            if (value > 2000) mCloudBase.setTextColor(Color.parseColor("#006600"));
            else if (value > 1200) mCloudBase.setTextColor(Color.parseColor("#e67300"));
            else mCloudBase.setTextColor(Color.RED);
        } else mCloudBase.setTextColor(Color.BLACK);

        mVisibility = ((TextView) view.findViewById(R.id.txtVisibility));
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

        return view;
    }

}
