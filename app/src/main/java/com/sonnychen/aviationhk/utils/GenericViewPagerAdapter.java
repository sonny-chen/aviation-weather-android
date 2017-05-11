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

package com.sonnychen.aviationhk.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.sonnychen.aviationhk.views.CustomFragmentBase;
import java.util.List;


public class GenericViewPagerAdapter extends FragmentPagerAdapter {
    private List<CustomFragmentBase> fragments;

    public GenericViewPagerAdapter(List<CustomFragmentBase> fragments, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragments = fragments;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return fragments.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        if (position < fragments.size())
            return fragments.get(position);

        return null;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        if (position < fragments.size())
            return fragments.get(position).FragmentTitle;

        return null;
    }
}

