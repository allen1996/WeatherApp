package allen96.com.weatherboy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by allen on 29/03/16.
 */
public class WeatherInfoRecyclerAdapter extends RecyclerView.Adapter
        <WeatherInfoRecyclerAdapter.WeatherInfoViewHolder>{

    List<WeatherInfo> weatherLists;

    public WeatherInfoRecyclerAdapter(List<WeatherInfo> weatherLists) {
        this.weatherLists = weatherLists;
    }

    @Override
    public WeatherInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.cardview_item, parent, false);

        return new WeatherInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WeatherInfoViewHolder holder, int position) {
        WeatherInfo weatherInfo = weatherLists.get(position);
        holder.mLocation.setText(weatherInfo.currentLocation);
        holder.mDescription.setText(weatherInfo.weatherDescription);
        holder.mTemperature.setText(weatherInfo.temperature + "°");
    }

    @Override
    public int getItemCount() {
        return weatherLists.size();
    }

    public static class WeatherInfoViewHolder extends RecyclerView.ViewHolder {

        protected TextView mLocation;
        protected TextView mDescription;
        protected TextView mTemperature;

        public WeatherInfoViewHolder(View itemView) {
            super(itemView);
            mLocation = (TextView) itemView.findViewById(R.id.tv_location);
            mDescription = (TextView) itemView.findViewById(R.id.tv_description);
            mTemperature = (TextView) itemView.findViewById(R.id.tv_temperature);
        }
    }

}
