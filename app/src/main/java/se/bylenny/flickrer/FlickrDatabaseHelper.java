package se.bylenny.flickrer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import se.bylenny.flickrer.models.internal.Image;
import se.bylenny.flickrer.models.internal.Query;
import se.bylenny.flickrer.models.internal.Post;

public class FlickrDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "flickr.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Query, String> queryDao;
    private Dao<Post, Integer> postDao;
    private Dao<Image, Integer> imageDao;
    private Cursor cursor;

    public FlickrDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Post.class);
            TableUtils.createTable(connectionSource, Query.class);
            TableUtils.createTable(connectionSource, Image.class);
        } catch (SQLException e) {
            Log.e(TAG, "Could not create new table", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Post.class, true);
            TableUtils.dropTable(connectionSource, Query.class, true);
            TableUtils.dropTable(connectionSource, Image.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Could not upgrade the table", e);
        }
    }

    public Dao<Query, String> getQueryDao() throws SQLException {
        if (queryDao == null) {
            queryDao = getDao(Query.class);
        }
        return queryDao;
    }

    public Dao<Post, Integer> getPostDao() throws SQLException {
        if(postDao == null) {
            postDao = getDao(Post.class);
        }
        return postDao;
    }

    public Dao<Image, Integer> getImageDao() throws SQLException {
        if(imageDao == null) {
            imageDao = getDao(Image.class);
        }
        return imageDao;
    }
}
