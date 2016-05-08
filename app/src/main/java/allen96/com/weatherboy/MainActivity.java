package allen96.com.weatherboy;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,WeatherInfoRecyclerAdapter.OnRecyclerItemClickListener {
    private WeatherInfoRecyclerAdapter recyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Add a new city", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        //setup recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.cardList);
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        ArrayList<String> places = new ArrayList<String>();
        places.add("Auckland");
        places.add("Sydney");
        places.add("London");
        places.add("Hanoi");
        places.add("Bangkok");
        FetchCurrentWeatherTask db = new FetchCurrentWeatherTask();
        db.execute(places);



        //setup swipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent,
                R.color.colorPrimary, R.color.colorAccent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * called when user swipes down to refresh the weather. -Haman Ganda Testing Github-
     */
    @Override
    public void onRefresh() {
        //use handler to delay the refresh time as the method updateWeatherData is not implemented yet
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Database.updateWeatherData();
                swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(recyclerView, "Weather has been updated", Snackbar.LENGTH_LONG)
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        })
                        .setActionTextColor(Color.YELLOW)
                        .show();
            }
        }, 2500);
    }

    @Override
    public void onRecyclerItemClick(View view, int position) {
        WeatherInfo weatherInfoString = recyclerAdapter.getList().get(position);
        String locationString = weatherInfoString.currentLocation;
        Intent intent = new Intent(this, DetailActivity.class).putExtra(Intent.EXTRA_TEXT, locationString);
        startActivity(intent);
        Log.d(LOG_TAG, "new activity/fragment for item at position " + position);
    }

    private static String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private static String formatTemp(double value) {

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedValue = Math.round(value);

        String formattedValue = String.valueOf(roundedValue);
        return formattedValue;
    }

    public static List<String> getCurrentWeather(String forecastJsonStr, int numDays ){
        List<String> data = new ArrayList<>();
        try {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "main";
            final String OWM_TEMP = "temp";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DESCRIPTION = "description";
            ArrayList<Double> highestTemp = new ArrayList<>();
            ArrayList<Double> lowestTemp = new ArrayList<>();
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            Time dayTime = new Time();
            dayTime.setToNow();
            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            // now we work exclusively in UTC
            dayTime = new Time();
            Log.v(LOG_TAG, "Test entry: " + getReadableDateString(dayTime.setJulianDay(julianStartDay)));
            Log.v(LOG_TAG, Long.toString(dayTime.setJulianDay(julianStartDay))); //1458205200
            String[] resultStrs = new String[numDays];
            String day;
            String description;
            String currentTemp;
            JSONObject dayForecast = weatherArray.getJSONObject(0);
            long dateTime;
            dateTime = dayTime.setJulianDay(julianStartDay);
            day = getReadableDateString(dateTime);
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double value = temperatureObject.getDouble(OWM_TEMP);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            currentTemp = formatTemp(value);
            String temp = currentTemp;
            data.add(description);
            data.add(temp);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return data;
    }



    public class FetchCurrentWeatherTask extends AsyncTask<ArrayList<String>, Void, List<WeatherInfo>> {
        @Override
        protected List<WeatherInfo> doInBackground(ArrayList<String>... params) {
            if (params.length == 0) {
                return null;
            }
            ArrayList<String> places = params[0];
            List<WeatherInfo> dummyData = new ArrayList<>();
            for (int i = 0; i <= places.size() - 1; i++) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String forecastJsonStr = null;
                String format = "json";
                String units = "metric";
                String appid = "dcec9f8e144965ee19f337a24f6ae445";
                int numDays = 1;
                try {
                    final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                    final String QUERY_PARAM = "q";
                    final String APPID_PARAM = "APPID";
                    final String FORMAT_PARAM = "mode";
                    final String UNITS_PARAM = "units";
                    final String DAYS_PARAM = "cnt";
                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, String.valueOf(places.get(i))) //params[0] refer to the String parameter of AsyncTask
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

                final String LOG_TAG = Database.class.getSimpleName();
                List<String> info = getCurrentWeather(forecastJsonStr,numDays);
                dummyData.add(new WeatherInfo(places.get(i), info.get(0), info.get(1)));
            }
            return dummyData;
        }

        @Override
        protected void onPostExecute(List<WeatherInfo> dummyData) {
            if (dummyData != null) {
                recyclerAdapter = new WeatherInfoRecyclerAdapter(dummyData);
                //attach adapter with the itemClickListener
                recyclerAdapter.attachRecyclerItemClickListener(MainActivity.this);
                recyclerView.setAdapter(recyclerAdapter);
            }
        }
    }
}
