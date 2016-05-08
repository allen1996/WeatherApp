package allen96.com.weatherboy;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testFetchWeather(){
        FetchWeatherTask task = new FetchWeatherTask();
        task.execute("Auckland");
        assertNotNull("Executing FetchWeatherTask() resulted in a null", task);
    }

    public void testDoInBackground(){
       FetchWeatherTask task = new FetchWeatherTask();
        task.doInBackground();
        assertNotNull("Executing doInBackground() resulted in a null", task);
    }

    public void testTempArray(){
        FetchWeatherTask task = new FetchWeatherTask();
        task.execute("Auckland");
        ArrayList<String> taskArray = task.tempArray;
        assertNotNull("Array holding tempuratures are null",taskArray);
    }

    public void testGetCurrentWeather() throws IOException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=Auckland,64&APPID=dcec9f8e144965ee19f337a24f6ae445&mode=json&units=metric&cnt=7");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }
        String forecastJsonStr = buffer.toString();
        List<String> info = MainActivity.getCurrentWeather(forecastJsonStr, 1);
        String description = info.get(0);
        String temp = info.get(1);
        assertNotNull(description);
        assertNotNull(temp);
    }

    public void testGetReadableDateString(){
        long time = 1462611600;
        SimpleDateFormat shortendedDateFormat = new SimpleDateFormat("EEE MMM DD");
        String readableDate = shortendedDateFormat.format(time);
        assertEquals("Sun Jan 18", readableDate);
    }

    public void testFormatTemp(){
        double value = 28.9;
        long roundedValue = Math.round(value);
        String formattedValue = String.valueOf(roundedValue);
        assertEquals("29", formattedValue);
    }
    public void testWindDirectionTranslate() {
        FetchWeatherTask task = new FetchWeatherTask();
        double degree = 12;
        assertEquals("NNE",task.translateDegree(degree));
    }

}