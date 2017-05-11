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

package com.sonnychen.aviationhk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sonnychen.aviationhk.parsers.BasicSyncCallback;
import com.sonnychen.aviationhk.utils.GenericViewPagerAdapter;
import com.sonnychen.aviationhk.views.BookingFragment;
import com.sonnychen.aviationhk.views.CustomFragmentBase;
import com.sonnychen.aviationhk.views.HomeFragment;
import com.sonnychen.aviationhk.views.LocalFragment;
import com.sonnychen.aviationhk.views.MetarFragment;
import com.sonnychen.aviationhk.views.RadarFragment;
import com.sonnychen.aviationhk.views.VHSKFragment;

import java.util.ArrayList;
import java.util.List;

import static com.sonnychen.aviationhk.BaseApplication.PREFS_NAME;

public class MainActivity extends AppCompatActivity implements
        CustomFragmentBase.OnFragmentInteractionListener {

    ViewPager mViewPager;
    BottomNavigationView mNavigation;
    GenericViewPagerAdapter mAdapter;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.main_menu_view_booking:
                showBookings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void bindUI() {

        List<CustomFragmentBase> fragments = new ArrayList<>();
        fragments.add(HomeFragment.newInstance(this));
        fragments.add(RadarFragment.newInstance(this));
        fragments.add(VHSKFragment.newInstance(this));
        fragments.add(MetarFragment.newInstance(this));
        fragments.add(LocalFragment.newInstance(this));

        mAdapter = new GenericViewPagerAdapter(fragments, this.getSupportFragmentManager());
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
                        mViewPager.setCurrentItem(3);
                        return true;
                    case R.id.navigation_radar:
                        mViewPager.setCurrentItem(1);
                        return true;
                    case R.id.navigation_vhsk:
                        mViewPager.setCurrentItem(2);
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
            if (intent.hasExtra(BaseApplication.SYNC_EVENT_PARAM) && intent.getStringExtra(BaseApplication.SYNC_EVENT_PARAM).equals(BasicSyncCallback.DataType.METAR.toString()))
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

    public void showBookings() {
        if (!TextUtils.isEmpty(BaseApplication.HKACPassword)) {
            launchBookingViewer();
            return;
        }

        View promptsView = LayoutInflater.from(this).inflate(R.layout.simple_password_prompt, null);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton(R.string.go,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                String password = (userInput.getText()).toString();

                                /** CHECK FOR USER'S INPUT **/
                                if (!TextUtils.isEmpty(password)) {
                                    BaseApplication.HKACPassword = password;
                                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString("HKACPassword", BaseApplication.HKACPassword);
                                    editor.apply();

                                    launchBookingViewer();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle(R.string.error);
                                    builder.setMessage(R.string.pdf_password_is_required);
                                    builder.create().show();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }

                        }

                );

        alertDialogBuilder.create().show();
    }

    private void launchBookingViewer() {
        Intent intent = new Intent(MainActivity.this, GenericFragmentHostActivity.class);
        intent.putExtra(GenericFragmentHostActivity.FRAGMENT_NAME_PARAM, BookingFragment.class.getName());
        startActivity(intent);
    }

}
