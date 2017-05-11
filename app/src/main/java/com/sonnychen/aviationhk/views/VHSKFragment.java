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
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
 * Use the {@link VHSKFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VHSKFragment extends CustomFragmentBase {
    public VHSKFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VHSKFragment.
     */
    public static VHSKFragment newInstance(Context context) {
        VHSKFragment fragment = new VHSKFragment();
        fragment.FragmentTitle = context.getString(R.string.title_vhsk);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_vhsk, container, false);
        RecyclerView mListView = (RecyclerView) view.findViewById(R.id.listView);

        cardList.clear();
        for (Pair<String, String> cam : BaseApplication.Data.VHSKChartURLs)
            cardList.add(new GenericCardItem(cam.first, cam.second, cam.first));

        mListView.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.VERTICAL, false));
        mListView.setAdapter(new GenericRecyclerViewAdapter(getContext(), cardList, LinearLayout.VERTICAL));
        return view;
    }
}
