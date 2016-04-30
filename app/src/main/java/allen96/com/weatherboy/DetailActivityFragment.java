package allen96.com.weatherboy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ArrayList<String> weatherString = new ArrayList<>();
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.cityText = (TextView)rootView.findViewById(R.id.detailed_location);
        weatherTask.humidityText = (TextView)rootView.findViewById(R.id.detailed_humidity);
        weatherTask.pressureText = (TextView)rootView.findViewById(R.id.detailed_pressure);
        weatherTask.descriptionText = (TextView)rootView.findViewById(R.id.detailed_description);
        weatherTask.windSpeedText = (TextView)rootView.findViewById(R.id.detailed_wind);
        weatherTask.precipitationText = (TextView)rootView.findViewById(R.id.detailed_precipitation);
        weatherTask.temperatureText = (TextView)rootView.findViewById(R.id.detailed_temperature);
        weatherTask.execute("Auckland,84");
        weatherTask.adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherString);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(weatherTask.adapter);
        return rootView;
    }


}




