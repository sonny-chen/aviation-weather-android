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

package com.sonnychen.aviationhk.parsers;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.sonnychen.aviationhk.parsers.BasicSyncCallback.DataType;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.sonnychen.aviationhk.utils.DownloadUtils.readStream;

public class HKOData {
    private static final int RUNWAY_DIRECTION = 110;
    private static final double KNOT_OVER_KM = 0.539956803456D;

    public Date METAR_Timestamp;
    public String METAR_Date;
    public String METAR_Code;
    public String METAR_Wind;
    public String METAR_Visibility;
    public String METAR_Weather;
    public String METAR_CloudAmount;
    public String METAR_CloudBase;
    public String METAR_Temperature;
    public String METAR_DewPoint;
    public String METAR_QNH;
    public String METAR_HTML;
    public ArrayList<Pair<String, String>> METAR_Data;

    public Date TAF_Timestamp;
    public String TAF_Date;
    public String TAF_Code;
    public String TAF_HTML;
    public ArrayList<Pair<String, String>>[] TAF_Data;

    public Date SIGMET_Timestamp;
    public String SIGMET_Date;
    public String SIGMET_Code;

    public Date LocalAviationForecast_Timestamp;
    public String LocalAviationForecast_Date;
    public String LocalAviationForecast_Code;

    public Date RADAR_Timestamp;
    public ArrayList<String> Radar_Animation64URLs; // dynamic
    public ArrayList<String> Radar_Animation128URLs; // dynamic
    public ArrayList<String> Radar_Animation256URLs; // dynamic

    public ArrayList<Pair<String, String>> WeatherPhotoURLs; // name, url // static
    public ArrayList<Pair<String, String>> VHSKChartURLs; // name, url // static
    public AnimationDrawable Radar_Animation64;
    public AnimationDrawable Radar_Animation128;
    public AnimationDrawable Radar_Animation256;

    public Date RegionalWeather_Timestamp;
    public double VHSK_Temperature_Celsius;
    public double VHSK_RH;
    public double VHSK_TemperatureMax_Celsius;
    public double VHSK_TemperatureMin_Celsius;
    public String VHSK_WindDirection;
    public double VHSK_Wind_Knots;
    public double VHSK_Gust_Knots;
    public double VHSK_CrossWind_Knots;
    public double VHSK_CrossWind_Angle;
    public double VHSK_CrossWindMax_Knots;
    public double VHSK_CrossWindAverage_Knots;
    public double VHSK_Pressure_hPa;

    public Visibility[] VisibilityReadings;

    public HKOData(final Context context, final BasicSyncCallback callback) {

        // initialize static urls (static links require no parsing)
        WeatherPhotoURLs = new ArrayList<>();
        WeatherPhotoURLs.add(new Pair<>("Radar", "http://www.hko.gov.hk/content_elements_v2/images/radar/R1.jpg?" + (new Random()).nextLong()));
        WeatherPhotoURLs.add(new Pair<>("Satellite", "http://www.hko.gov.hk/content_elements_v2/images/satellite/S1.jpg?" + (new Random()).nextLong()));
        WeatherPhotoURLs.add(new Pair<>("Lightning", "http://www.hko.gov.hk/content_elements_v2/images/lightning/lightning.png?" + (new Random()).nextLong()));

        WeatherPhotoURLs.add(new Pair<>("Kadoorie Farm and Botanic Garden", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/kfb/latest_KFB.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Tai Po Kau", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/tpk/latest_TPK.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Sai Kung Marine East Station 1", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/sk2/latest_SK2.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Sai Kung Marine East Station 2", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/skg/latest_SKG.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Sai Wan Ho", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/swh/latest_SWH.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Kowloon City", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/klt/latest_KLT.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Tsim Sha Tsui 1", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/hk2/latest_HK2.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Tsim Sha Tsui 2", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/hko/latest_HKO.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Central", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/cp1/latest_CP1.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Victoria Peak 1", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/vpb/latest_VPB.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Victoria Peak 2", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/vpa/latest_VPA.jpg"));
        WeatherPhotoURLs.add(new Pair<>("German Swiss International School", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/gsi/latest_GSI.jpg"));

        WeatherPhotoURLs.add(new Pair<>("Waglan Island 1", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/wl2/latest_WL2.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Waglan Island 2", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/wgl/latest_WGL.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Lau Fau Shan", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/lfs/latest_LFS.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Wetland Park", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/wlp/latest_WLP.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Elegantia College in Sheung Shui", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/elc/latest_ELC.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Tai Lam Chung", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/tlc/latest_TLC.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Sha Lo Wan", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/slw/latest_SLW.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Peng Chau 1", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/dnl/latest_DNL.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Peng Chau 2", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/pe2/latest_PE2.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Cheung Chau", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/cch/latest_CCH.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Cheung Chau Tung Wan", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/cce/latest_CCE.jpg"));
        WeatherPhotoURLs.add(new Pair<>("Lamma Island", "http://www.hko.gov.hk/wxinfo/aws/hko_mica/lam/latest_LAM.jpg"));

        VHSKChartURLs = new ArrayList<>();
        VHSKChartURLs.add(new Pair<>("Wind Speed", "http://www.hko.gov.hk/wxinfo/ts/sekspd.png"));
        VHSKChartURLs.add(new Pair<>("Wind Direction", "http://www.hko.gov.hk/wxinfo/ts/sekdir.png"));
        VHSKChartURLs.add(new Pair<>("QNH Sea Level Pressure", "http://www.hko.gov.hk/wxinfo/ts/pre/sekpre.png"));
        VHSKChartURLs.add(new Pair<>("Temperature / RH", "http://www.hko.gov.hk/wxinfo/ts/temp/sektemp.png"));

        new FillMETARCodeTask() {
            @Override
            protected void onPostExecute(Boolean result) {
                METAR_Timestamp = new Date();
                callback.onSyncFinished(DataType.METAR, result);
            }
        }.execute();
        new FillTAFCodeTask() {
            @Override
            protected void onPostExecute(Void data) {
                TAF_Timestamp = new Date();
                callback.onSyncFinished(DataType.TAF, TAF_Code != null && !TAF_Code.isEmpty());
            }
        }.execute();
        new FillSIGMETCodeTask() {
            @Override
            protected void onPostExecute(Void data) {
                SIGMET_Timestamp = new Date();
                callback.onSyncFinished(DataType.SIGMET, SIGMET_Code != null && !SIGMET_Code.isEmpty());
            }
        }.execute();
        new FillLocalCodeTask() {
            @Override
            protected void onPostExecute(Void data) {
                LocalAviationForecast_Timestamp = new Date();
                callback.onSyncFinished(DataType.LOCAL, LocalAviationForecast_Code != null && !LocalAviationForecast_Code.isEmpty());
            }
        }.execute();
        new FillRadarAnimationURLsTask() {
            @Override
            protected void onPostExecute(Void data) {
                RADAR_Timestamp = new Date();
                callback.onSyncFinished(DataType.RADAR, Radar_Animation64URLs != null && Radar_Animation64URLs.size() > 0);

                // download 64km range radar sequence
                new DownloadRadarImagesTask(context, DataType.RADAR64, HKOData.this, callback) {
                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (success)
                            callback.onSyncFinished(DataType.RADAR, Radar_Animation64URLs != null && Radar_Animation64URLs.size() > 0);
                    }
                }.execute();
            }
        }.execute();
        new FillRegionalWeatherTask() {
            @Override
            protected void onPostExecute(Void data) {
                RegionalWeather_Timestamp = new Date();
                callback.onSyncFinished(DataType.VHSK, VHSK_WindDirection != null && !VHSK_WindDirection.isEmpty());
            }
        }.execute();
    }


    // -------------------------
    private class FillMETARCodeTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params) {
            try {
                // HKO's SSL certificate is invalid (?)
                URL url = new URL("http://www.hko.gov.hk/aviat/wxobs_decode_e.htm");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String HTML = readStream(con.getInputStream());

                int startPos = HTML.indexOf("The latest aviation weather");
                int endPos = HTML.indexOf("\n", startPos + 1); // find the first line break
                endPos = HTML.indexOf("\n", endPos + 1); // METAR is on the next line

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-METAR-Fail", HTML);
                    return null;
                }

                // initialize storage
                String data_raw = HTML.substring(startPos, endPos);
                data_raw = data_raw.replaceAll("<[^>]*>", "");
                METAR_Date = data_raw.split("\n")[0];
                METAR_Code = data_raw.split("\n")[1];

                // html
                //<table width='100%' border="1"... <p><table><tr>

                startPos = HTML.indexOf("Decoded METAR/SPECI");
                startPos = HTML.indexOf("<p>", startPos);
                endPos = HTML.indexOf("<p><table><tr>", startPos);

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-METAR-HTML-Fail", HTML);
                    return null;
                }
                METAR_HTML = HTML.substring(startPos, endPos);

                // fields

                startPos = HTML.indexOf("This table shows the decoded METAR/SPECI in plain language");
                startPos = HTML.indexOf("</tr>", startPos);
                endPos = HTML.indexOf(" hPa", startPos + 1);
                endPos = HTML.indexOf("</tr>", endPos + 1);

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-METAR-FIELDS-Fail", HTML);
                    return null;
                }

                String fields = HTML.substring(startPos, endPos);
                // remove nested table from fields HTML
                fields = fields.replaceAll("<td align='right' valign=\"top\">", "");
                fields = fields.replaceAll("</td></tr>", "");
                int fieldCount = 0;
                int fieldStartIndex = fields.indexOf("<td ");

                while (fieldStartIndex >= 0) {
                    int endIndex = fields.indexOf("</td>", fieldStartIndex);
                    String field = fields.substring(fieldStartIndex, endIndex).replaceAll("<[^>]*>", "").trim(); // remove HTML from line
                    Log.v("Parser-Dump", "Field " + fieldCount + ": " + field);
                    switch (fieldCount) {
                        case 0:
                            METAR_Wind = field;
                            break;
                        case 1:
                            METAR_Visibility = field;
                            break;
                        case 2:
                            METAR_Weather = field;
                            break;
                        default:
                            // The cloud amount and cloud base fields may contain child tables
                            // messing up our <td /> tag count. We're resorting to heuristics.
                            if (field.contains("okta") && TextUtils.isEmpty(METAR_CloudAmount))
                                METAR_CloudAmount = field;
                            else if (field.contains("feet") && TextUtils.isEmpty(METAR_CloudBase))
                                METAR_CloudBase = field;
                            else if (field.contains("oC") && TextUtils.isEmpty(METAR_Temperature))
                                METAR_Temperature = field;
                            else if (field.contains("oC") && TextUtils.isEmpty(METAR_DewPoint))
                                METAR_DewPoint = field;
                            else if (field.contains("hPa") && TextUtils.isEmpty(METAR_QNH))
                                METAR_QNH = field;
                            break;
                    }
                    fieldStartIndex = fields.indexOf("<td ", fieldStartIndex + 1);
                    if (fieldCount++ > 10) break; // dead loop - something is wrong
                }

                Log.v("Parser-METAR", METAR_Date);
                Log.v("Parser-METAR", METAR_Code);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return false;
        }
    }

    private class FillTAFCodeTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://www.hko.gov.hk/aviat/taf_decode_e.htm");

//<span style="font-size:113%;"><strong>The latest Aerodrome Forecast at the Hong Kong International Airport issued by the Hong Kong Observatory at 07:00 HKT on 25 Apr 17</strong>
//<p>TAF VHHH 242300Z 2500/2606 10010KT 7000 FEW015 SCT025 TX24/2506Z TX25/2606Z TN20/2523Z TEMPO 2500/2502 3000 SHRA FEW012CB SCT025 TEMPO 2502/2508 VRB20G30KT 2000 TSRA +SHRA FEW010 SCT020CB BKN040 TEMPO 2508/2510 16010KT 3000 SHRA FEW012CB SCT025 BECMG 2510/2512 18010KT TEMPO 2522/2602 3500 SHRA=<p>

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String HTML = readStream(con.getInputStream());

                int startPos = HTML.indexOf("The latest Aerodrome");
                int endPos = HTML.indexOf("\n", startPos + 1); // find the first line break
                endPos = HTML.indexOf("\n", endPos + 1); // TAF is on the next line

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-TAF-Fail", HTML);
                    return null;
                }

                // initialize storage
                String data_raw = HTML.substring(startPos, endPos);
                data_raw = data_raw.replaceAll("<[^>]*>", "");
                TAF_Date = data_raw.split("\n")[0];
                TAF_Code = data_raw.split("\n")[1];

                // html
                startPos = HTML.indexOf("<p><p><strong><em>");
                endPos = HTML.indexOf("<p><table><tr><td valign=", startPos);

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-TAF-HTML-Fail", HTML);
                    return null;
                }
                TAF_HTML = HTML.substring(startPos, endPos);

                Log.v("Parser-TAF", TAF_Date);
                Log.v("Parser-TAF", TAF_Code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return null;
        }
    }

    private class FillSIGMETCodeTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://www.hko.gov.hk/aviat/sigmet_txt_e.htm");

//<p><strong>The latest significant weather information
//<br>issued by the Hong Kong Observatory at 10:50 HKT on 25 Apr 17</strong></p>
//                        VHHK SIGMET 1 VALID 250250/250650 VHHH-
//                        VHHK HONG KONG FIR
//                EMBD TS FCST WI N1936 E11130 - N2124 E11130 - N2230 E11342 -
//                        N2236 E11400 - N2018 E11400 - N1936 E11130
//                TOP FL380 MOV NE 05KT NC=
//
//
//                        VHHK SIGMET 8 VALID 242250/250250 VHHH-
//                        VHHK HONG KONG FIR
//                EMBD TS FCST N OF N2000 AND W OF E11400 TOP FL380 MOV E 05KT NC=
//
//
//
//</span>
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String HTML = readStream(con.getInputStream());

                int startPos = HTML.indexOf("The latest significant");
                int endPos = HTML.indexOf("</span>", startPos); //

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-SIGMET-Fail", HTML);
                    return null;
                }

                // initialize storage
                String data_raw = HTML.substring(startPos, endPos);
                data_raw = data_raw.replaceAll("<[^>]*>", "");
                String[] lines = data_raw.split("\n");
                SIGMET_Date = lines[0] + " " + lines[1];
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < lines.length; i++)
                    sb.append(lines[i].trim()).append("\n");
                SIGMET_Code = sb.toString();
                Log.v("Parser-SIGMET", SIGMET_Date);
                Log.v("Parser-SIGMET", SIGMET_Code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return null;
        }
    }

    private class FillLocalCodeTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://www.hko.gov.hk/aviat/100nm_e.htm");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String HTML = readStream(con.getInputStream());

                int startPos = HTML.indexOf("Bulletin issued at ");
                int endPos = HTML.indexOf("</pre>", startPos);

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-LocalAviation-Fail", HTML);
                    return null;
                }

                // initialize storage
                String data_raw = HTML.substring(startPos, endPos);
                data_raw = data_raw.replaceAll("<[^>]*>", "");
                String[] lines = data_raw.split("\n");
                LocalAviationForecast_Date = lines[0];
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < lines.length; i++)
                    sb.append(lines[i].trim()).append("\n");
                LocalAviationForecast_Code = sb.toString();
                Log.v("Parser-LocalAviation", LocalAviationForecast_Date);
                Log.v("Parser-LocalAviation", LocalAviationForecast_Code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return null;
        }
    }

    private class FillRadarAnimationURLsTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            try {
                //        www.hko.gov.hk/wxinfo/radars/radar_range1.htm
                URL url = new URL("http://www.hko.gov.hk/wxinfo/radars/radar_range1.htm");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String HTML = readStream(con.getInputStream());

                //        picture[0][0]="rad_256_png/2d256nradar_201704250848.jpg";
                //        picture[0][1]="rad_256_png/2d256nradar_201704250900.jpg";
                //        picture[1][8]="rad_128_png/2d128nradar_201704251024.jpg";
                //        picture[1][9]="rad_128_png/2d128nradar_201704251036.jpg";
                //        picture[2][17]="rad_064_png/2d064nradar_201704251024.jpg";
                //        picture[2][18]="rad_064_png/2d064nradar_201704251030.jpg";
                int startPos = HTML.indexOf("picture[0][0]");
                int endPos = HTML.indexOf("var range=");

                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-Radar-Fail", HTML);
                    return null;
                }

                // initialize storage
                Radar_Animation64URLs = new ArrayList<>();
                Radar_Animation128URLs = new ArrayList<>();
                Radar_Animation256URLs = new ArrayList<>();

                String data_raw = HTML.substring(startPos, endPos);
                String[] lines = data_raw.split("\n");
                for (String line : lines) {
                    line = "http://www.hko.gov.hk/wxinfo/radars/" + line.split("\"")[1].trim();
                    if (line.contains("_256_"))
                        Radar_Animation256URLs.add(line);
                    else if (line.contains("_128_"))
                        Radar_Animation128URLs.add(line);
                    else if (line.contains("_064_"))
                        Radar_Animation64URLs.add(line);
                }

                Log.v("Parser-Radar", Radar_Animation256URLs.toString());
                Log.v("Parser-Radar", Radar_Animation128URLs.toString());
                Log.v("Parser-Radar", Radar_Animation64URLs.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return null;
        }
    }

    public class Visibility {
        public String Location;
        public double Visibility_10min_KM;
    }

    private class FillRegionalWeatherTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            try {
                //TODO: VHSK forecast wind/dir: http://maps.weather.gov.hk/ocf/text_e.html?mode=0&station=SEK# calculate VHSK c/w component

                // HKO's SSL certificate is invalid (?)
                URL url = new URL("http://www.hko.gov.hk/wxinfo/ts/text_readings_e.htm");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String HTML = readStream(con.getInputStream());

                //Shek Kong                      28.4       74        29.6 / 24.4
                int startPos = HTML.indexOf("Shek Kong");
                int endPos = HTML.indexOf("\n", startPos); // find the first line break
                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-Regional1-Fail", HTML);
                    return null;
                }

                String data_line = HTML.substring(startPos, endPos).replaceAll("<[^>]*>", "");

                String rawvalue = data_line.substring(30, 36).trim();
                if (NumberUtils.isCreatable(rawvalue))
                    VHSK_Temperature_Celsius = Double.parseDouble(rawvalue);

                rawvalue = data_line.substring(41, 45).trim();
                if (NumberUtils.isCreatable(rawvalue))
                    VHSK_RH = Double.parseDouble(rawvalue);

                rawvalue = data_line.substring(51).trim();
                String[] subvalues = rawvalue.split("/");
                if (NumberUtils.isCreatable(subvalues[0].trim()))
                    VHSK_TemperatureMax_Celsius = Double.parseDouble(subvalues[0].trim());
                if (subvalues.length > 1 && NumberUtils.isCreatable(subvalues[1].trim()))
                    VHSK_TemperatureMin_Celsius = Double.parseDouble(subvalues[1].trim());

                //Shek Kong             Southeast      4    14
                startPos = HTML.indexOf("Shek Kong", endPos);
                endPos = HTML.indexOf("\n", startPos); // find the first line break
                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-Regional2-Fail", HTML);
                    return null;
                }

                data_line = HTML.substring(startPos, endPos).replaceAll("<[^>]*>", "");
                VHSK_WindDirection = data_line.substring(21, 33).trim();

                double windDirection;
                switch (VHSK_WindDirection.toLowerCase()) {
                    case "east":
                        windDirection = 90;
                        break;
                    case "south":
                        windDirection = 180;
                        break;
                    case "west":
                        windDirection = 270;
                        break;
                    case "northeast":
                        windDirection = 45;
                        break;
                    case "northwest":
                        windDirection = 315;
                        break;
                    case "southeast":
                        windDirection = 135;
                        break;
                    case "southwest":
                        windDirection = 225;
                        break;
                    case "north":
                    default:
                        windDirection = 0;
                        break;
                }

                VHSK_CrossWind_Angle = windDirection - RUNWAY_DIRECTION;
                rawvalue = data_line.substring(36, 40).trim();
                if (NumberUtils.isCreatable(rawvalue)) {
                    VHSK_Wind_Knots = Double.parseDouble(rawvalue) * KNOT_OVER_KM;
                    VHSK_CrossWind_Knots = Math.sin(VHSK_CrossWind_Angle > 180 ? VHSK_CrossWind_Angle - 180 : VHSK_CrossWind_Angle) * VHSK_Wind_Knots;
                }

                rawvalue = data_line.substring(41).trim();
                if (NumberUtils.isCreatable(rawvalue)) {
                    VHSK_Gust_Knots = Double.parseDouble(rawvalue) * KNOT_OVER_KM;
                    VHSK_CrossWindMax_Knots = Math.sin(VHSK_CrossWind_Angle > 180 ? VHSK_CrossWind_Angle - 180 : VHSK_CrossWind_Angle) * VHSK_Gust_Knots;
                    VHSK_CrossWindAverage_Knots = Math.sin(VHSK_CrossWind_Angle > 180 ? VHSK_CrossWind_Angle - 180 : VHSK_CrossWind_Angle) * (VHSK_Wind_Knots + VHSK_Gust_Knots) / 2D;
                }

                //Shek Kong              1010.0
                startPos = HTML.indexOf("Shek Kong", endPos);
                endPos = HTML.indexOf("\n", startPos); // find the first line break
                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-Regional3-Fail", HTML);
                    return null;
                }

                data_line = HTML.substring(startPos, endPos).replaceAll("<[^>]*>", "");
                rawvalue = data_line.substring(22).trim();
                if (NumberUtils.isCreatable(rawvalue))
                    VHSK_Pressure_hPa = Double.parseDouble(rawvalue);

//                10-Minute Mean Visibility
//                Central             7 km
//                Chek Lap Kok       17 km
//                Sai Wan Ho          8 km
//                Waglan Island       7 km
                startPos = HTML.indexOf("10-Minute Mean Visibility", endPos);
                startPos = HTML.indexOf("\n", startPos) + 1;
                endPos = HTML.indexOf("Global Solar", startPos); // find the first line break
                if (startPos < 0 || endPos < startPos) {
                    Log.wtf("Parser-Regional4-Fail", HTML);
                    return null;
                }

                String[] data_lines = HTML.substring(startPos, endPos).replaceAll("<[^>]*>", "").trim().split("\n");
                List<Visibility> visibilities = new ArrayList<>();
                for (String line : data_lines) {
                    if (line.length() < 20) continue;
                    Visibility visibility = new Visibility();
                    visibility.Location = line.substring(0, 17).trim();
                    rawvalue = line.substring(17).replace(" km", "").trim();
                    if (NumberUtils.isCreatable(rawvalue))
                        visibility.Visibility_10min_KM = Double.parseDouble(rawvalue);
                    if (visibility.Visibility_10min_KM > 100) // visibility under 5km may be expressed in meters
                        visibility.Visibility_10min_KM /= 100;

                    visibilities.add(visibility);
                }
                VisibilityReadings = visibilities.toArray(new Visibility[visibilities.size()]);

                Log.v("Parser-Visibility", Arrays.toString(VisibilityReadings));
                Log.v("Parser-VHSK", VHSK_WindDirection.concat(" ").concat(VHSK_Wind_Knots + "").concat(" kts"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            return null;
        }
    }

}
