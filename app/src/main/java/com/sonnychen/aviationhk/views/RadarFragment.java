package com.sonnychen.aviationhk.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sonnychen.aviationhk.BaseApplication;
import com.sonnychen.aviationhk.utils.GenericCardItem;
import com.sonnychen.aviationhk.utils.GenericRecyclerViewAdapter;
import com.sonnychen.aviationhk.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomFragmentBase.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RadarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadarFragment extends CustomFragmentBase {
    public RadarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RadarFragment.
     */
    public static RadarFragment newInstance() {
        return new RadarFragment();
    }

    private ArrayList<GenericCardItem> cardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radar, container, false);
        RecyclerView mListView = (RecyclerView) view.findViewById(R.id.listView);

        cardList.clear();
        for (Pair<String, String> cam : BaseApplication.Data.WeatherPhotoURLs) {
            cardList.add(new GenericCardItem(cam.first, cam.second, cam.first));
        }

        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setAutoMeasureEnabled(true);
        mLayoutManager.setMeasurementCacheEnabled(false);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setHasFixedSize(false);
        mListView.setAdapter(new GenericRecyclerViewAdapter(getContext(), cardList));
        return view;
    }
}
