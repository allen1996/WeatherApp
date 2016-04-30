package allen96.com.weatherboy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allen on 29/03/16.
 */
public class Database {

    protected static List<WeatherInfo> createDummyWeatherData() {
        List<WeatherInfo> dummyData = new ArrayList<>();


        return dummyData;
    }

    protected static void updateWeatherData() {
        //if current time minus previous update time is less than 15 min
        //display a Snackbar "Weather is already up to date".

        //if user do not have internet connection
        //display a Snackbar "No internet connection".

        //parse the json data from server
        //save it to database for future query
    }

}
