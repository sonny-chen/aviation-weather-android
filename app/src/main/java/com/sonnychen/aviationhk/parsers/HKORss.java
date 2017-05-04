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
 * Created by Sonny Chen on 5/3/2017.
 **/

package com.sonnychen.aviationhk.parsers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.RSSFeed;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class HKORss {
    public Date RSS_Timestamp;
    public String LocalWeatherForecastDescription;
    public String SeveralDaysWeatherForecastDescription;
    public String[] WeatherWarnings;

    public HKORss(final Context context, final BasicSyncCallback callback) {
        new FillLocalWeatherForecastTask() {
            @Override
            protected void onPostExecute(Boolean success) {
                RSS_Timestamp = new Date();
                callback.onSyncFinished(BasicSyncCallback.DataType.RSS, success);
            }
        }.execute();
    }

    private class FillLocalWeatherForecastTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params) {
            try {
                // HKO's SSL certificate is invalid (?)
                HttpURLConnection con = (HttpURLConnection) new URL("http://rss.weather.gov.hk/rss/LocalWeatherForecast.xml").openConnection();
                Feed feed = EarlParser.parseOrThrow(con.getInputStream(), 0);

                if (RSSFeed.class.isInstance(feed)) {
                    RSSFeed rssFeed = (RSSFeed) feed;
                    if (rssFeed.items.size() > 0)
                        LocalWeatherForecastDescription = rssFeed.items.get(0).description;
                }

                con = (HttpURLConnection) new URL("http://rss.weather.gov.hk/rss/SeveralDaysWeatherForecast.xml").openConnection();
                feed = EarlParser.parseOrThrow(con.getInputStream(), 0);


/*
		Date/Month:
		04/05 (Thursday)<br/>
		Wind:
		South force 3 to 4.<br/>
		Weather:
		Cloudy with showers and a few squally thunderstorms. Showers will be heavy at times at first.<br/>
		Temp range:
		24 -
		29 C<br/>
		R.H. range:
		75 -
		95 per Cent<br/><p/><p/>
		Date/Month:
		05/05 (Friday)<br/>
		Wind:
		Light winds force 2.<br/>
		Weather:
		Mainly cloudy with occasional showers and a few thunderstorms.<br/>
		Temp range:
		23 -
		28 C<br/>
		R.H. range:
		75 -
		95 per Cent<br/><p/><p/>
		Date/Month:
		06/05 (Saturday)<br/>
 */
                if (RSSFeed.class.isInstance(feed)) {
                    RSSFeed rssFeed = (RSSFeed) feed;
                    if (rssFeed.items.size() > 0)
                        // HKO dumps all forecasts in a text block - requires further parsing
                        SeveralDaysWeatherForecastDescription = rssFeed.items.get(0).description;
                }

                con = (HttpURLConnection) new URL("http://rss.weather.gov.hk/rss/WeatherWarningSummaryv2.xml").openConnection();
                feed = EarlParser.parseOrThrow(con.getInputStream(), 0);

                if (RSSFeed.class.isInstance(feed)) {
                    RSSFeed rssFeed = (RSSFeed) feed;
                    WeatherWarnings = new String[rssFeed.items.size()];
                    if (rssFeed.items.size() > 0)
                        for (int i = 0; i< rssFeed.items.size() ; i++)
                            WeatherWarnings[i] = rssFeed.items.get(i).title;
                }


                Log.v("RSS-LocalWeather", LocalWeatherForecastDescription);
                Log.v("RSS-SeveralDays", SeveralDaysWeatherForecastDescription);
                Log.v("RSS-WeatherWarnings", Arrays.toString(WeatherWarnings));

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return false;
        }
    }
}
