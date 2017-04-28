package com.sonnychen.metarhk;

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
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.sonnychen.metarhk.parsers.HKOData;
import com.sonnychen.metarhk.utils.BottomNavigationViewHelper;
import com.sonnychen.metarhk.views.CustomFragmentBase;
import com.sonnychen.metarhk.views.HomeFragment;
import com.sonnychen.metarhk.views.LocalFragment;
import com.sonnychen.metarhk.views.MetarFragment;
import com.sonnychen.metarhk.views.RadarFragment;
import com.sonnychen.metarhk.views.VHSKFragment;

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
