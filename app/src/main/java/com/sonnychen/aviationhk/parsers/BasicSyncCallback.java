package com.sonnychen.aviationhk.parsers;

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
 * Created by Sonny Chen on 5/4/2017.
 **/

public interface BasicSyncCallback {
    void onProgressUpdate(DataType dataType, int progress, int max);

    void onSyncFinished(DataType dataType, boolean success);

    enum DataType {
        METAR, TAF, SIGMET, LOCAL, RADAR, RADAR64, RADAR128, RADAR256, RSS
    }
}