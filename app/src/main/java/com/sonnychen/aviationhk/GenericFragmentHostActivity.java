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
 * Created by Sonny Chen on 5/11/2017.
 **/

package com.sonnychen.aviationhk;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.sonnychen.aviationhk.utils.GenericViewPagerAdapter;
import com.sonnychen.aviationhk.views.BookingFragment;
import com.sonnychen.aviationhk.views.CustomFragmentBase;

import java.util.ArrayList;
import java.util.List;

public class GenericFragmentHostActivity extends AppCompatActivity
        implements CustomFragmentBase.OnFragmentInteractionListener {
    public static final String FRAGMENT_NAME_PARAM = "NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment_host);

        if (getIntent() != null && getIntent().getStringExtra(FRAGMENT_NAME_PARAM) != null) {
            if (getIntent().getStringExtra(FRAGMENT_NAME_PARAM).equals(BookingFragment.class.getName())) {
                List<CustomFragmentBase> fragments = new ArrayList<>();
                CustomFragmentBase fragment1 = BookingFragment.newInstance(this, BookingFragment.BookingType.FIXED_WING);
                Bundle args1 = new Bundle();
                args1.putString(BookingFragment.BOOKING_TYPE_PARAM, BookingFragment.BookingType.FIXED_WING.toString());
                fragment1.setArguments(args1);
                fragments.add(fragment1);

                CustomFragmentBase fragment2 = BookingFragment.newInstance(this, BookingFragment.BookingType.HELICOPTER);
                Bundle args2 = new Bundle();
                args2.putString(BookingFragment.BOOKING_TYPE_PARAM, BookingFragment.BookingType.HELICOPTER.toString());
                fragment2.setArguments(args2);
                fragments.add(fragment2);

                ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
                //BottomNavigationView mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
                final GenericViewPagerAdapter mAdapter = new GenericViewPagerAdapter(fragments, this.getSupportFragmentManager());
                mViewPager.setAdapter(mAdapter);
                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        // sync Bottom Navigation View
//                        Menu menu = mNavigation.getMenu();
//                        menu.getItem(position).setChecked(true);
                        // set page title
                        GenericFragmentHostActivity.this.setTitle(mAdapter.getPageTitle(position));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
