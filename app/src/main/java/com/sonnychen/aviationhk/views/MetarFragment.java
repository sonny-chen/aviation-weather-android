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

package com.sonnychen.aviationhk.views;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomFragmentBase.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MetarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MetarFragment extends CustomFragmentBase {
    public MetarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MetarFragment.
     */
    public static MetarFragment newInstance(Context context) {
        MetarFragment fragment = new MetarFragment();
        fragment.FragmentTitle = context.getString(R.string.title_metartaf);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("METAR-UI", "Starting");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_metar, container, false);
//        ((TextView) view.findViewById(R.id.metar)).setText(BaseApplication.Data.METAR_Code);
        WebView metar = ((WebView) view.findViewById(R.id.metar_html));
        metar.setInitialScale(1);
        metar.getSettings().setJavaScriptEnabled(false);
        metar.getSettings().setLoadWithOverviewMode(true);
        metar.getSettings().setUseWideViewPort(true);
        metar.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        metar.setScrollbarFadingEnabled(false);
        metar.loadDataWithBaseURL("", BaseApplication.Data.METAR_HTML + "<p><p>" +
                        BaseApplication.Data.TAF_HTML + "<p><pre>" + BaseApplication.Data.SIGMET_Code + "</pre>",
                "text/html", "UTF-8", "");
//        ((TextView) view.findViewById(R.id.taf)).setText(BaseApplication.Data.TAF_Code);
//        ((TextView) view.findViewById(R.id.sigmet)).setText(BaseApplication.Data.SIGMET_Code);
        return view;
    }
}
