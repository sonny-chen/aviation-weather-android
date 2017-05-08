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
import android.text.TextUtils;
import android.util.Log;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.RSSFeed;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.sonnychen.aviationhk.utils.DownloadUtils.readStream;

public class HKORss {
    public Date RSS_Timestamp;
    public Date LocalWeatherWarnings_Timestamp;
    public String LocalWeatherForecastDescription;
    public String[] WeatherWarnings;

    public String GeneralSituation;
    public WeatherForecast[] WeatherForecasts;

    public class WeatherForecast {
        public Date Date;
        public String Wind;
        public String Weather;
        public String TemperatureRange;
        public String RHRange;
        public String WeatherCartoonURL;
        public String WeatherCartoonDescription;
    }

    public HKORss(final Context context, final BasicSyncCallback callback) {
        new FillLocalWeatherForecastTask() {
            @Override
            protected void onPostExecute(Boolean success) {
                RSS_Timestamp = new Date();
                callback.onSyncFinished(BasicSyncCallback.DataType.FORECASTS, success);
            }
        }.execute();
//        new FillLocalWeatherWarningsTask() {
//            @Override
//            protected void onPostExecute(Boolean success) {
//                LocalWeatherWarnings_Timestamp = new Date();
//                callback.onSyncFinished(BasicSyncCallback.DataType.WEATHER_WARNINGS, success);
//            }
//        }.execute();
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

                // NB. we'll use the text-only page as source - the RSS data feed is much messier than text-only
                //con = (HttpURLConnection) new URL("http://rss.weather.gov.hk/rss/SeveralDaysWeatherForecast.xml").openConnection();
                con = (HttpURLConnection) new URL("http://www.hko.gov.hk/textonly/v2/forecast/nday.htm").openConnection();

/*
...
9-Day Weather Forecast

General Situation:
Under the influence of a trough of low pressure, there will
be showers over southern China today and tomorrow. With the
ridge of high pressure over the western North Pacific
extending westwards, it will be mainly fine and hot over
southeastern China in the middle and latter parts of this
week. Another trough of low pressure is expected to bring
thundery showers to southern China early next week.

Date/Month 8/5 (Monday)
Wind: East force 3 to 4.
Weather: Sunny intervals and a few showers. Isolated
thunderstorms later.
Temp Range: 24 - 29 C
R.H. Range: 70 - 95 Per Cent

Date/Month 9/5(Tuesday)
Wind: Light winds force 2.
Weather: Sunny intervals and a few showers.
Temp Range: 25 - 30 C
R.H. Range: 70 - 95 Per Cent
...
Sea surface temperature at 7 a.m.8/5/2017 at North Point was
24 degrees C.
...
Weather Cartoons for 9-day weather forecast
Day 1 cartoon no. 54 - Sunny Intervals with Showers
Day 2 cartoon no. 54 - Sunny Intervals with Showers
Day 3 cartoon no. 51 - SUNNY PERIODS
...
 */
                String HTML = readStream(con.getInputStream());

                int startPos = HTML.indexOf("General Situation:");
                int endPos = HTML.indexOf("Date/Month", startPos);

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("TEXT-GeneralSituation-Fail", HTML);
                    return null;
                }

                // initialize storage
                GeneralSituation = HTML.substring(startPos, endPos).replaceAll("<[^>]*>", ""); // strip HTML tags

                // parse daily forecasts
                ArrayList<WeatherForecast> forecastList = new ArrayList<>();
                startPos = HTML.indexOf("Date/"); // find the first Date/Month occurrence
                endPos = HTML.indexOf("Sea surface", startPos);
                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("TEXT-SeveralDays-Fail", HTML);
                    return null;
                }
                String[] forecasts_raw = HTML.substring(startPos, endPos).split("Date/");

                Log.v("DUMP", Arrays.toString(forecasts_raw));

                for (int i = 1; i < forecasts_raw.length; i++) {
                    String[] lines = forecasts_raw[i].split("\n");
                    WeatherForecast forecast = new WeatherForecast();
                    for (String line : lines) {
                        line = line.trim();
                        if (line.startsWith("Month")) {
                            // strip Month and Weekday from line
                            String date_raw = line.substring(6).split("\\(")[0].trim() + "/" + new GregorianCalendar().get(Calendar.YEAR);
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                            try {
                                forecast.Date = df.parse(date_raw);
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                                break;
                            }
                        } else if (line.startsWith("Wind"))
                            forecast.Wind = line.substring(5);
                        else if (line.startsWith("Weather"))
                            forecast.Weather = line.substring(8);
                        else if (line.startsWith("Temp Range"))
                            forecast.TemperatureRange = line.substring(11);
                        else if (line.startsWith("R.H. Range"))
                            forecast.RHRange = line.substring(11);
                    }

                    forecastList.add(forecast);
                }

                startPos = HTML.indexOf("Day 1 cartoon");
                endPos = HTML.indexOf("</pre>\n", startPos);
                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("TEXT-Cartoons-Fail", HTML);
                    return null;
                }
                String[] cartoons_raw = HTML.substring(startPos, endPos).split("\n");

                for (String rawdata : cartoons_raw) {
                    String[] parts = rawdata.split(" ");
                    // Day 1 cartoon no. 54 - Sunny Intervals with Showers
                    if (TextUtils.isDigitsOnly(parts[1])) {
                        int day = Integer.parseInt(parts[1]) - 1;
                        if (TextUtils.isDigitsOnly(parts[4]))
                            forecastList.get(day).WeatherCartoonURL = String.format("http://www.weather.gov.hk/content_elements_v2/images/weather-icon-70x70/pic%s.png", parts[4]);
                        if (rawdata.contains("-"))
                            forecastList.get(day).WeatherCartoonDescription = rawdata.substring(rawdata.indexOf("-") + 2);
                    }
                }
                WeatherForecasts = forecastList.toArray(new WeatherForecast[forecastList.size()]);

                Log.v("RSS-LocalWeather", LocalWeatherForecastDescription);
                Log.v("TEXT-GeneralSituation", GeneralSituation);
                Log.v("TEXT-WeatherForecasts", Arrays.toString(WeatherForecasts));

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return false;
        }
    }

    private class FillLocalWeatherWarningsTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params) {
            try {
                // HKO's SSL certificate is invalid (?)
                HttpURLConnection con = (HttpURLConnection) new URL("http://rss.weather.gov.hk/rss/WeatherWarningSummaryv2.xml").openConnection();
                Feed feed = EarlParser.parseOrThrow(con.getInputStream(), 0);

                if (RSSFeed.class.isInstance(feed)) {
                    RSSFeed rssFeed = (RSSFeed) feed;
                    WeatherWarnings = new String[rssFeed.items.size()];
                    if (rssFeed.items.size() > 0)
                        for (int i = 0; i < rssFeed.items.size(); i++)
                            WeatherWarnings[i] = rssFeed.items.get(i).title;
                }

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
