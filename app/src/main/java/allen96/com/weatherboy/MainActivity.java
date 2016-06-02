package allen96.com.weatherboy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        WeatherInfoRecyclerAdapter.OnRecyclerItemClickListener {

    /**
     * Request code for the autocomplete activity. This will be used to identify results from the
     * autocomplete activity in onActivityResult.
     */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private WeatherInfoRecyclerAdapter recyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FetchCurrentWeatherTask db;
    private ArrayList<String> places;
    private SimpleDateFormat sdf;
    private Date lastUpdateTime;

    protected static boolean isMetric;
    protected static boolean isCentimeters;
    protected static boolean isMph;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sdf = new SimpleDateFormat("HH:mm a", Locale.getDefault());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openAutocompleteActivity();
                }
            });
        }

        //setup swipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent,
                R.color.colorPrimary, R.color.colorAccent);

        //setup recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.cardList);
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        //create and set the recycler adapter
        recyclerAdapter = new WeatherInfoRecyclerAdapter(
                Database.createDummyWeatherData());
        recyclerAdapter.attachRecyclerItemClickListener(this);
        recyclerView.setAdapter(recyclerAdapter);

        //add places to the ArrayList and fetch current temp data for each city
        getUnitType();
        places = new ArrayList<>();
        places.add("Auckland");
        places.add("Sydney");
        places.add("London");
        places.add("Hanoi");
        places.add("Bangkok");
        places.add("Wellington");
        db = new FetchCurrentWeatherTask();
        db.execute(places);
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        lastUpdateTime = cal.getTime();
        recyclerAdapter.setLastUpdateTime(getCurrentTime());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(LOG_TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(LOG_TAG, "Place Selected: " + place.getName());
                places.add(place.getName().toString());
                recyclerAdapter.notifyDataSetChanged();
                db = new FetchCurrentWeatherTask();
                db.execute(places);
                Snackbar.make(recyclerView, place.getName() + " has been added", Snackbar.LENGTH_LONG).show();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(LOG_TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    static View view;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Initializing variables needed for dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        String[] colourArray = new String[]{"Red", "Blue", "Black", "Green", "Yellow", "Cyan", "Orange"};
        view = findViewById(R.id.card_view);

        //noinspection SimplifiableIfStatement
        //Once the user makes a choice of the colour, it will change to the background colour
        if (id == R.id.backgroundcolour) {
            alert.setSingleChoiceItems(colourArray, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0)
                        view.getRootView().setBackgroundColor(Color.parseColor("#fc0000"));
                    if(which == 1)
                        view.getRootView().setBackgroundColor(Color.parseColor("#0127ff"));
                    if(which == 2)
                        view.getRootView().setBackgroundColor(Color.parseColor("#000000"));
                    if(which == 3)
                        view.getRootView().setBackgroundColor(Color.parseColor("#04ca01"));
                    if(which == 4)
                        view.getRootView().setBackgroundColor(Color.parseColor("#ffef13"));
                    if(which == 5)
                        view.getRootView().setBackgroundColor(Color.parseColor("#79fffd"));
                    if(which == 6)
                        view.getRootView().setBackgroundColor(Color.parseColor("#ff7301"));

                }
            }).setCancelable(false).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setTitle("Choose Background Colour").create().show();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * called when user swipes down to refresh the weather.
     */
    @Override
    public void onRefresh() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        Date updateTime = cal.getTime();
        //time difference in minutes
        long timeDifference = TimeUnit.MILLISECONDS.toMinutes(updateTime.getTime() - lastUpdateTime.getTime());

        //if the time difference between two updates is within 10 minutes
        // it should not execute the async task
        if (timeDifference < 1) {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(recyclerView, "Weather already updated.",
                    Snackbar.LENGTH_LONG).show();
        } else {
            lastUpdateTime = updateTime;
            recyclerAdapter.setLastUpdateTime(getCurrentTime());
            db = new FetchCurrentWeatherTask();
            db.execute(places);
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(recyclerView, "Weather has been updated", Snackbar.LENGTH_LONG).show();
        }
    }

    public String getCurrentTime() {
        String currentTime;
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        currentTime = sdf.format(cal.getTime());
        return currentTime;
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

    public void getUnitType() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String unitType = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        if (unitType.equals(getString(R.string.pref_units_imperial))) {
            isMetric = false;
        } else
            isMetric = true;
        String preUnitType = sharedPrefs.getString(getString(R.string.pref_preunits_key), getString(R.string.pref_preunits_cm));
        if (preUnitType.equals(getString(R.string.pref_preunits_inch))) {
            isCentimeters = false;
        } else
            isCentimeters = true;
        String windUnitType = sharedPrefs.getString(getString(R.string.pref_windunits_key), getString(R.string.pref_windunits_mph));
        if (windUnitType.equals(getString(R.string.pref_windunits_kph))) {
            isMph = false;
        } else
            isMph = true;

    }

    public static String formatTemp(double value) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        if (isMetric == false) {
            value = (value * 1.8) + 32;
        }
        long roundedValue = Math.round(value);
        String formattedValue = String.valueOf(roundedValue);
        return formattedValue;
    }

    public static List<String> getCurrentWeather(String forecastJsonStr, int numDays) {
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://allen96.com.weatherboy/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://allen96.com.weatherboy/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
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
                            .appendQueryParameter(QUERY_PARAM, String.valueOf(places.get(i)))
                            .appendQueryParameter(APPID_PARAM, appid)
                            .appendQueryParameter(FORMAT_PARAM, format)
                            .appendQueryParameter(UNITS_PARAM, units)
                            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                            .build();
                    URL url = new URL(builtUri.toString());
                    //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=Auckland,
                    //64&APPID=dcec9f8e144965ee19f337a24f6ae445&mode=json&units=metric&cnt=7");
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

                List<String> info = getCurrentWeather(forecastJsonStr, numDays);
                dummyData.add(new WeatherInfo(places.get(i), info.get(0), info.get(1)));
            }
            return dummyData;
        }

        @Override
        protected void onPostExecute(List<WeatherInfo> dummyData) {
            if (dummyData != null) {
                recyclerAdapter.updateWeatherList(dummyData);
                recyclerAdapter.notifyDataSetChanged();
            }
        }
    }
}