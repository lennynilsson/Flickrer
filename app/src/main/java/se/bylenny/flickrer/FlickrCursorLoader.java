package se.bylenny.flickrer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;

import se.bylenny.flickrer.models.internal.Post;

public class FlickrCursorLoader extends CursorLoader {

    private static final String TAG = "FlickrCursorLoader";
    private final FlickrDatabaseHelper helper;
    private String text;

    public FlickrCursorLoader(Context context, FlickrDatabaseHelper helper, String text) {
        super(context);
        this.helper = helper;
        this.text = text;
    }

    @Override
    public Cursor loadInBackground() {
        try {
            Dao<Post, Integer> dao = helper.getPostDao();
            PreparedQuery<Post> query = dao.queryBuilder()
                    .orderBy("index", true)
                    .where().eq("query_id", text).prepare();
            CloseableIterator<Post> iterator = dao.iterator(query);
            AndroidDatabaseResults results = (AndroidDatabaseResults)iterator.getRawResults();
            Cursor cursor = results.getRawCursor();
            return cursor;
        } catch (SQLException e) {
            Log.e(TAG, "Unable to load cursor", e);
            return null;
        } catch (NullPointerException e) {
            Log.e(TAG, "Database helper not loaded", e);
            return null;
        }
    }
}
