package se.bylenny.flickrer;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import se.bylenny.flickrer.models.external.FlickrResponse;
import se.bylenny.flickrer.models.external.Photo;
import se.bylenny.flickrer.models.internal.Image;
import se.bylenny.flickrer.models.internal.Post;
import se.bylenny.flickrer.models.internal.Query;

public class FlickrIntentService extends IntentService {
    private static final String TAG = "FlickrIntentService";

    private static final String ACTION_FETCH = "se.bylenny.flickrer.action.FETCH";
    private static final String EXTRA_RESET = "se.bylenny.flickrer.extra.RESET";
    public static final String EXTRA_TEXT = "se.bylenny.flickrer.extra.TEXT";
    public static final String EXTRA_PAGE = "se.bylenny.flickrer.extra.EXTRA_PAGE";
    public static final String EXTRA_PAGES = "se.bylenny.flickrer.extra.EXTRA_PAGES";

    public static final int RESULT_FETCH_SUCCESS = 1;
    public static final int RESULT_RESET_SUCCESS = 2;
    public static final int RESULT_FETCH_ERROR = -1;
    public static final int RESULT_RESET_ERROR = -2;

    public static final int PAGE_SIZE = 50;

    private static FlickrDatabaseHelper helper;

    /**
     * Starts this service to perform action Fetch with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionFetch(Context context, String text, boolean reset) {
        Intent intent = new Intent(context, FlickrIntentService.class);
        intent.setAction(ACTION_FETCH);
        intent.putExtra(EXTRA_TEXT, text);
        intent.putExtra(EXTRA_RESET, reset);
        context.startService(intent);
    }

    public FlickrIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_FETCH.equals(intent.getAction())) {
            final String text = intent.getStringExtra(EXTRA_TEXT);
            final boolean reset = intent.getBooleanExtra(EXTRA_RESET, false);
            try {
                handleActionFetch(text, reset);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to fetch query", e);
                MessageBus.send(RESULT_FETCH_ERROR);
            }
        }
    }

    /**
     * Handle action Fetch in the provided background thread with the provided
     * parameters.
     */
    private synchronized void handleActionFetch(final String text, final boolean reset) throws SQLException {
        Log.d(TAG, "handleActionFetch");
        Dao<Query, String> dao = getHelper(getApplicationContext()).getQueryDao();
        Query query = dao.queryForId(text);
        if (query == null) {
            Log.e(TAG, "New query: " + text);
            query = new Query();
            query.setLastPage(-1);
            query.setLastIndex(-1);
            query.setFreeText(text);
            dao.create(query);
        } else if (reset) {
            query.setLastPage(-1);
            query.setLastIndex(-1);
            query.getPosts().clear();
            dao.update(query);
        } else if (query.getLastPage() >= query.getPageCount()) {
            Log.d(TAG, "End of list");
            MessageBus.send(RESULT_FETCH_ERROR);
            return;
        }
        fetch(text, query.getLastPage() + 1);
    }

    private void fetch(final String text, long page) {
        String apiKey = getApplicationContext().getString(R.string.api_key);
        FlickrRestRequest<FlickrResponse> request = new FlickrRestRequest<FlickrResponse>();
        Uri uri = createQuery(apiKey, text, page);
        request.call(uri, FlickrResponse.class,
            new FlickrRestRequest.ResponseListener<FlickrResponse>() {
                @Override
                public void onSuccess(final FlickrResponse response) {
                    if (response.stat == "fail") {
                        Log.e(TAG, "Got error response: " + response.message);
                    } else {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    translateAndStore(response, text);
                                    MessageBus.send(RESULT_FETCH_SUCCESS);
                                } catch (SQLException e) {
                                    Log.e(TAG, "Unable to translate response", e);
                                    MessageBus.send(RESULT_FETCH_ERROR);
                                }
                            }
                        }, TAG);
                        thread.start();
                    }
                }
                @Override
                public void onFailure(String error) {
                    Log.e(TAG, error == null ? "" : error);
                    MessageBus.send(RESULT_FETCH_ERROR);
                }
            });
    }

    public static FlickrDatabaseHelper getHelper(Context context) {
        if (helper == null) {
            helper = OpenHelperManager.getHelper(context, FlickrDatabaseHelper.class);
        }
        return helper;
    }

    private Uri createQuery(String apiKey, String text, long page) {
        if (text == null || text.isEmpty()) {
            text = getString(R.string.initial_query);
        }
        return Uri.parse("https://flickr.com/services/rest/").buildUpon()
                .appendQueryParameter("method", "flickr.photos.search")
                .appendQueryParameter("api_key", apiKey)
                .appendQueryParameter("sort", "interestingness-desc")
                .appendQueryParameter("privacy_filter", "1")
                .appendQueryParameter("content_type", "1")
                .appendQueryParameter("media", "photo")
                .appendQueryParameter("per_page", String.valueOf(PAGE_SIZE))
                .appendQueryParameter("page", String.valueOf(page + 1))
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("text", text)
                .appendQueryParameter("extras", "description,license,owner_name,"
                        + "url_sq,url_t,url_s,url_q,url_m,url_n,url_z,url_c,url_l,url_o")
                .build();
    }

    private synchronized void translateAndStore(FlickrResponse response, String text) throws SQLException {
        Log.d(TAG, "Importing \"" + text + "\"...");
        final String[] fields = {"sq", "s", "q", "m", "n", "z", "c", "l", "o"};
        FlickrDatabaseHelper helper = getHelper(getApplicationContext());
        Dao<Query, String> queryDao = helper.getQueryDao();
        Dao<Post, Integer> postDao = helper.getPostDao();
        Dao<Image, Integer> imageDao = helper.getImageDao();
        Query query = queryDao.queryForId(text);
        if (query == null) {
            query = new Query();
            query.setFreeText(text);
            query.setLastIndex(-1);
        }
        long lastIndex = query.getLastIndex();
        query.setLastPage(response.photos.page);
        query.setPageCount(response.photos.pages);
        query.setPostCount(response.photos.total);
        queryDao.createOrUpdate(query);
        for (Photo photo : response.photos.photo) {
            try {
                Post post = new Post();
                post.setIndex(++lastIndex);
                post.setCreator(photo.ownername);
                post.setTitle(photo.title);
                post.setDescription(photo.description._content);
                post.setQuery(query);
                postDao.create(post);
                postDao.refresh(post);
                for (String field : fields) {
                    try {
                        String url = (String) Photo.class.getField("url_" + field)
                                .get(photo);
                        int width = Integer.valueOf(Photo.class.getField("width_" + field)
                                .getInt(photo));
                        int height = Integer.valueOf(Photo.class.getField("height_" + field)
                                .getInt(photo));
                        if (url != null) {
                            Image image = new Image();
                            image.setUrl(url);
                            image.setWidth(width);
                            image.setHeight(height);
                            image.setPost(post);
                            imageDao.create(image);
                        }
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "Unable to access data", e);
                    } catch (NoSuchFieldException e) {
                        Log.e(TAG, "Unable to access data", e);
                    }
                }
                postDao.update(post);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to create post", e);
            }
        }
        query.setLastIndex(lastIndex);
        queryDao.update(query);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Relaesing helper");
        this.helper = null;
    }
}
