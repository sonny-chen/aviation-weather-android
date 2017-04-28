package com.sonnychen.metarhk.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.sonnychen.metarhk.BaseApplication;
import com.sonnychen.metarhk.R;

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
    // TODO: Rename and change types and number of parameters
    public static MetarFragment newInstance() {
        return new MetarFragment();
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
