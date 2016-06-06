package allen96.com.weatherboy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allen on 29/03/16.
 */
public class Database {

    protected static List<WeatherInfo> createDummyWeatherData(ArrayList<String> places) {
        List<WeatherInfo> dummyData = new ArrayList<>();

        for (String place : places) {
            dummyData.add(new WeatherInfo(place));
        }

        return dummyData;
    }
}
