package se.bylenny.flickrer;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;


public class MainActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ListExhaustionListener {

    private static final String TAG = "MainActivity";

    private static final String STATE_TEXT_QUERY = "STATE_TEXT_QUERY";
    private static final String STATE_LIST = "STATE_LIST";
    private static final String STATE_REFRESHING = "STATE_REFRESHING";

    private FlickrCursorAdapter cursorAdapter;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String textQuery;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String initialQuery = getResources().getString(R.string.initial_query);
        if (savedInstanceState != null) {
            textQuery = savedInstanceState.getString(STATE_TEXT_QUERY, initialQuery);
        } else {
            textQuery = initialQuery;
        }
        updateTitle();

        // Setup Picasso and http client cache
        FlickrRestRequest.setup(getApplicationContext());
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.white);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reset this query and fetch new posts
                fetch(textQuery, true);
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Click");
                //TODO: Open in detail view
            }
        });
        listView.setDividerHeight(0);
        cursorAdapter = new FlickrCursorAdapter(getApplicationContext(), null, false, this);
        listView.setAdapter(cursorAdapter);

        initCursor();
    }

    private void setRefreshing(boolean refreshing) {
        Log.d(TAG, "IS refreshing: " + refreshing);
        isRefreshing = refreshing;
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageBus.subscribe(this);
        reloadCursor();
        setRefreshing(isRefreshing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessageBus.unsubscribe(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        textQuery = savedInstanceState.getString(
                STATE_TEXT_QUERY, getResources().getString(R.string.initial_query));
        isRefreshing = savedInstanceState.getBoolean(STATE_REFRESHING);
        listView.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LIST));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_TEXT_QUERY, textQuery);
        outState.putBoolean(STATE_REFRESHING, isRefreshing);
        outState.putParcelable(STATE_LIST, listView.onSaveInstanceState());
    }

    private void fetch(String text, final boolean reset) {
        textQuery = text;
        updateTitle();
        Log.d(TAG, "CALL startActionFetch "+this.toString());
        FlickrIntentService.startActionFetch(
                getApplicationContext(), textQuery, reset);
        setRefreshing(true);
    }

    @Subscribe
    public void onUpdate(Integer status) {
        if (status == FlickrIntentService.RESULT_FETCH_SUCCESS
                || status == FlickrIntentService.RESULT_FETCH_ERROR) {
            setRefreshing(false);
            reloadCursor();
        }
    }

    private void updateTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(textQuery == getResources().getString(R.string.initial_query)
                    ? getResources().getString(R.string.app_name)
                    : textQuery);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                textQuery = s;
                updateTitle();
                listView.setSelection(0);
                reloadCursor();
                MenuItemCompat.collapseActionView(searchItem);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setQuery(textQuery, false);
            return true;
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new FlickrCursorLoader(getApplicationContext(),
                FlickrIntentService.getHelper(getApplicationContext()), textQuery);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        setCursor(cursor);
        if (cursor == null) {
            fetch(textQuery, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setCursor(null);
    }

    @Override
    public void onListNearEnd() {
        if (!isRefreshing) {
            fetch(textQuery, false);
        }
    }


    private void initCursor() {
        getSupportLoaderManager().initLoader(1, null, this);
    }

    private void reloadCursor() {
        Loader<Object> loader = getSupportLoaderManager().getLoader(1);
        if (loader != null && ! loader.isReset()) {
            getSupportLoaderManager().restartLoader(1, null, this);
        } else {
            getSupportLoaderManager().initLoader(1, null, this);
        }
    }

    private void setCursor(Cursor cursor) {
        Cursor oldCursor = cursorAdapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private void destroyCursor() {
        getSupportLoaderManager().destroyLoader(1);
    }
}
