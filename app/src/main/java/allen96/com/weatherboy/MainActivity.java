package allen96.com.weatherboy;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        WeatherInfoRecyclerAdapter.OnRecyclerItemClickListener{

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

        WeatherInfoRecyclerAdapter recyclerAdapter = new WeatherInfoRecyclerAdapter(
                Database.createDummyWeatherData());
        //attach adapter with the itemClickListener
        recyclerAdapter.attachRecyclerItemClickListener(this);
        recyclerView.setAdapter(recyclerAdapter);

        //setup swipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
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
     * called when user swipes down to refresh the weather
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
        Log.d(LOG_TAG, "new activity/fragment for item at position " + position);
    }
}
