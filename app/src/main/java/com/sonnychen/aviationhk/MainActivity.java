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

package com.sonnychen.aviationhk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.sonnychen.aviationhk.parsers.HKOData;
import com.sonnychen.aviationhk.views.CustomFragmentBase;
import com.sonnychen.aviationhk.views.HomeFragment;
import com.sonnychen.aviationhk.views.LocalFragment;
import com.sonnychen.aviationhk.views.MetarFragment;
import com.sonnychen.aviationhk.views.RadarFragment;
import com.sonnychen.aviationhk.views.VHSKFragment;

public class MainActivity extends AppCompatActivity implements
        CustomFragmentBase.OnFragmentInteractionListener {

    ViewPager mViewPager;
    BottomNavigationView mNavigation;
    MainViewAdapter mAdapter;
    LinearLayout mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("MainActivity", "Starting");

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mLoading = (LinearLayout) findViewById(R.id.main_activity_loading);

        mLoading.setVisibility(View.VISIBLE);
        mNavigation.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);

        if (BaseApplication.Data.METAR_Code != null && !BaseApplication.Data.METAR_Code.isEmpty())
            bindUI();
    }

    private void bindUI() {

        mAdapter = new MainViewAdapter(this.getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // sync Bottom Navigation View
                Menu menu = mNavigation.getMenu();
//                for (int i = 0; i < MainViewAdapter.NUM_ITEMS; i++) {
//                    menu.getItem(i).setChecked(false);
//                }
                menu.getItem(position).setChecked(true);

                // set page title
                MainActivity.this.setTitle(mAdapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //BottomNavigationViewHelper.disableShiftMode(mNavigation);
        mNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mViewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_localaviation:
                        mViewPager.setCurrentItem(4);
                        return true;
                    case R.id.navigation_metartaf:
                        mViewPager.setCurrentItem(2);
                        return true;
                    case R.id.navigation_radar:
                        mViewPager.setCurrentItem(1);
                        return true;
                    case R.id.navigation_vhsk:
                        mViewPager.setCurrentItem(3);
                        return true;
                }
                return false;
            }

        });

        mLoading.setVisibility(View.GONE);
        mNavigation.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // wait for METAR to finish before loading UI
            if (intent.hasExtra(BaseApplication.SYNC_EVENT_PARAM) && intent.getStringExtra(BaseApplication.SYNC_EVENT_PARAM).equals(HKOData.DataType.METAR.toString()))
                bindUI();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // register for sync event broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BaseApplication.SYNC_EVENT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        // handle inter-fragment navigation here
    }

    private static class MainViewAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 5;

        MainViewAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return HomeFragment.newInstance();
                case 1:
                    return RadarFragment.newInstance();
                case 2:
                    return MetarFragment.newInstance();
                case 3:
                    return VHSKFragment.newInstance();
                case 4:
                    return LocalFragment.newInstance();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Overview";
                case 1:
                    return "Radar/Sat/Lightning";
                case 2:
                    return "Metar/TAF/SIGMET";
                case 3:
                    return "Shek Kong";
                case 4:
                    return "Local Aviation";
                default:
                    return null;
            }
        }

    }
}
