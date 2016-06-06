package allen96.com.weatherboy;

/**
 * Created by allen on 29/03/16.
 */
public class WeatherInfo {

    protected String currentLocation;
    protected String weatherDescription;
    protected String temperature;

    public WeatherInfo(String currentLocation, String weatherDescription, String temperature) {
        this.currentLocation = currentLocation;
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
    }

    public WeatherInfo(String currentLocation) {
        this.currentLocation = currentLocation;
        weatherDescription = "clear";
        temperature = "11";
    }
}
