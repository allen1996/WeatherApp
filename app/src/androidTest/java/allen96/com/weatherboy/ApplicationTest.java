package allen96.com.weatherboy;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testFetchWeather(){
        DetailActivityFragment.FetchWeatherTask task = new DetailActivityFragment.FetchWeatherTask();
        task.execute("Auckland");
        assertNotNull("Executing FetchWeatherTask() resulted in a null", task);
    }

    public void testDoInBackground(){
        DetailActivityFragment.FetchWeatherTask task = new DetailActivityFragment.FetchWeatherTask();
        task.doInBackground();
        assertNotNull("Executing doInBackground() resulted in a null", task);
    }

    public void testTempArray(){
        DetailActivityFragment.FetchWeatherTask task = new DetailActivityFragment.FetchWeatherTask();
        task.execute("Auckland");
        ArrayList<String> taskArray = task.tempArray;
        assertNotNull("Array holding tempuratures are null",taskArray);
    }



}