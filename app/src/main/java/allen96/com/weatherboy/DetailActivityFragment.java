package allen96.com.weatherboy;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private ArrayList<String> tempArray = new ArrayList<String>();
    private ArrayList<String> descriptionList = new ArrayList<String>();
    private HashSet<String> removeDuplicate = new HashSet<String>();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ArrayList<String> weatherString = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherString);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute("Hanoi,84");
        listView.setAdapter(adapter);
        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low) {

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "main";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DESCRIPTION = "description";
            ArrayList<Double> highestTemp = new ArrayList<Double>();
            ArrayList<Double> lowestTemp = new ArrayList<Double>();

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();
            Log.v(LOG_TAG, "Test entry: " + getReadableDateString(dayTime.setJulianDay(julianStartDay)));
            Log.v(LOG_TAG, Long.toString(dayTime.setJulianDay(julianStartDay))); //1458205200
            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + Math.round(high) + "/" + Math.round(low);
                tempArray.add(Long.toString(Math.round(high)) + " on " + day.split(" ")[0]);
                tempArray.add(Long.toString(Math.round(low)) + " on " + day.split(" ")[0]);
                descriptionList.add(description);
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            String appid = "dcec9f8e144965ee19f337a24f6ae445";
            int numDays = 7;
            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                final String QUERY_PARAM = "q";
                final String APPID_PARAM = "APPID";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0]) //params[0] refer to the String parameter of AsyncTask
                        .appendQueryParameter(APPID_PARAM, appid)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();
                URL url = new URL(builtUri.toString());
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=Auckland,64&APPID=dcec9f8e144965ee19f337a24f6ae445&mode=json&units=metric&cnt=7");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String[] result) { //result refer to the last parameter of AsyncTask
            if (result != null) {
                adapter.clear();
                adapter.add(findWeeklyDescription());
                for (String dayForecastStr : result) {
                    adapter.add(dayForecastStr);
                }
            }
        }

        public String findWeeklyDescription() {
            String weeklyDescription = "";
            String minTemp = tempArray.get(0);
            String maxTemp = tempArray.get(0);
            for (int i = 0; i < tempArray.size(); i++) {
                if (Integer.parseInt(tempArray.get(i).split(" ")[0]) > Integer.parseInt(maxTemp.split(" ")[0]))
                    maxTemp = tempArray.get(i);
                if (Integer.parseInt(tempArray.get(i).split(" ")[0]) < Integer.parseInt(minTemp.split(" ")[0]))
                    minTemp = tempArray.get(i);
            }
            double chance = Math.random() * 100;
            if (chance < 60)
                weeklyDescription += chosenDescription() + "with temperature peaking at " + maxTemp + ".";
            else
                weeklyDescription += chosenDescription() + "with temperature falling to " + minTemp + ".";
            return weeklyDescription;
        }

        public String chosenDescription() {
            ArrayList<String> newArray = new ArrayList<String>();
            String weeklyDescription = "";

            for (int i = 0; i < descriptionList.size(); i++) {
                int count = 0;
                for (int j = 0; j < descriptionList.size(); j++) {
                    if (descriptionList.get(i).equals(descriptionList.get(j))) {
                        count++;
                    }
                }
                removeDuplicate.add(count + " " + descriptionList.get(i));
            }
            Iterator it = removeDuplicate.iterator();
            while(it.hasNext()) {
                newArray.add((String) it.next());
            }
            String maxCount = newArray.get(0);
            if (newArray.size() >= 5) {
                weeklyDescription += "The weather in the next week change continuously ";
            }
            if (newArray.size() > 2 && newArray.size() < 5) {
                for (int i = 0; i < newArray.size(); i++) {
                    if (Integer.parseInt(newArray.get(i).split(" ")[0]) > Integer.parseInt(maxCount.split(" ")[0]))
                        maxCount = newArray.get(i);
                }
                weeklyDescription += maxCount.substring(1) + " mostly in next 7 days ";
            }
            if (newArray.size() < 3) {
                for (int i = 0; i < newArray.size(); i++) {
                    if (Integer.parseInt(newArray.get(i).split(" ")[0]) > Integer.parseInt(maxCount.split(" ")[0]))
                        maxCount = newArray.get(i);
                }
                weeklyDescription += maxCount.substring(1) + " throughout the week ";
            }
            return weeklyDescription;
        }
    }

}



