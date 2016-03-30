package allen96.com.weatherboy;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by allen on 29/03/16.
 */
public class WeatherInfoRecyclerAdapter extends RecyclerView.Adapter<
        WeatherInfoRecyclerAdapter.WeatherInfoViewHolder> {

    private List<WeatherInfo> mWeatherLists;
    static OnRecyclerItemClickListener listener;

    private static final String LOG_TAG = WeatherInfoRecyclerAdapter.class.getSimpleName();

    public WeatherInfoRecyclerAdapter(List<WeatherInfo> weatherLists) {
        mWeatherLists = weatherLists;
    }

    /**
     * called when the WeatherInfoViewHolder need to be initialised
     * only called once and reuse it to improve performance
     */
    @Override
    public WeatherInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.cardview_item, parent, false);

        return new WeatherInfoViewHolder(itemView);
    }

    /**
     * called when the views bind with data
     * using this ViewHolder pattern avoid looking up UI with findViewById all the time
     */
    @Override
    public void onBindViewHolder(WeatherInfoViewHolder holder, int position) {
        WeatherInfo weatherInfo = mWeatherLists.get(position);
        holder.mLocation.setText(weatherInfo.currentLocation);
        holder.mDescription.setText(weatherInfo.weatherDescription);
        holder.mTemperature.setText(weatherInfo.temperature + "°");
    }

    /**
     * @return the number of items in the recyclerList
     */
    @Override
    public int getItemCount() {
        return mWeatherLists.size();
    }

    public interface OnRecyclerItemClickListener {
        void onRecyclerItemClick(View view, int position);
    }

    public void attachRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        WeatherInfoRecyclerAdapter.listener = listener;
    }

    public static class WeatherInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected TextView mLocation;
        protected TextView mDescription;
        protected TextView mTemperature;

        public WeatherInfoViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mLocation = (TextView) itemView.findViewById(R.id.tv_location);
            mDescription = (TextView) itemView.findViewById(R.id.tv_description);
            mTemperature = (TextView) itemView.findViewById(R.id.tv_temperature);
        }

        /**
         * start detailActivity here
         * the position of the item clicked is get by getAdapterPosition
         */
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, String.valueOf(getAdapterPosition()));
            listener.onRecyclerItemClick(v, getAdapterPosition());
        }
    }

}
