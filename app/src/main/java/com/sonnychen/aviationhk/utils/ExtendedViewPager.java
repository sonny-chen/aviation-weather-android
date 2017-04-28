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

package com.sonnychen.aviationhk.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;


public class ExtendedViewPager extends ViewPager {

    public ExtendedViewPager(Context context) {
        super(context);
    }

    public ExtendedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof TouchImageView) {
            //
            // canScrollHorizontally is not supported for Api < 14. To get around this issue,
            // ViewPager is extended and canScrollHorizontallyFroyo, a wrapper around
            // canScrollHorizontally supporting Api >= 8, is called.
            //
            return ((TouchImageView) v).canScrollHorizontallyFroyo(-dx);

        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }

}