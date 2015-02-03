package se.bylenny.flickrer;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.RawRowMapper;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;

import se.bylenny.flickrer.models.internal.Image;
import se.bylenny.flickrer.models.internal.Post;

/**
 * Created by Lenny Nilsson on 2015-01-25.
 */
public class FlickrCursorAdapter extends CursorAdapter {

    private static final String TAG = "FlickrCursorAdapter";
    private final ListExhaustionListener listener;
    private int resolution;
    private LayoutInflater inflater;

    public class Tag {
        public String image;
        public String title;
        public FixedImageView imageView;
        public TextView titleView;
        public TextView creatorView;
    }

    public FlickrCursorAdapter(Context context, Cursor c, boolean autoRequery, ListExhaustionListener listener) {
        super(context, c, autoRequery);
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
        changeResolution();
    }

    public FlickrCursorAdapter(Context context, Cursor c, int flags, RawRowMapper<Post> mapper, ListExhaustionListener listener) {
        super(context, c, flags);
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
        changeResolution();
    }

    public void changeResolution() {
        Point size = new Point();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);
        switch (mContext.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                this.resolution = size.x;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                this.resolution = size.y;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                this.resolution = Math.max(size.x, size.y);
        }
        // Lets reduce the image resolution to save a little memory.
        this.resolution /= 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Tag tag = (Tag) view.getTag();
        if (view.getTag() == null) {
            tag = new Tag();
            tag.imageView = (FixedImageView) view.findViewById(R.id.image);
            tag.titleView = (TextView) view.findViewById(R.id.title);
            tag.creatorView = (TextView) view.findViewById(R.id.creator);
            view.setTag(tag);
        }

        String[] columns = new String[cursor.getColumnCount()];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = cursor.getString(i);
        }

        Post post = null;
        try {
            Dao<Post, Integer> dao = FlickrIntentService
                    .getHelper(context.getApplicationContext()).getPostDao();
            RawRowMapper<Post> mapper = dao.getRawRowMapper();
            post = mapper.mapRow(cursor.getColumnNames(), columns);
            dao.refresh(post);
            if (post.getImages() == null) {
                listener.onBroken();
                return;
            }
        } catch (SQLException e) {
            listener.onBroken();
            return;
        }

        Image image = selectBestImage(post.getImages(), resolution);
        tag.imageView.setAspect(image.getWidth(), image.getHeight());
        tag.image = selectBestImage(post.getImages(), 2500).getUrl();
        tag.title = post.getTitle();
        Picasso.with(context)
                .load(image.getUrl())
                .fit()
                .placeholder(R.color.gray)
                .error(R.color.red)
                .into(tag.imageView);
        tag.titleView.setText(post.getTitle());
        tag.creatorView.setText("by " + post.getCreator());

        handleExhaustion(cursor);
    }

    private void handleExhaustion(Cursor cursor) {
        int index = cursor.getInt(cursor.getColumnIndex("index"));
        int count = cursor.getCount();
        // Announce near end if we are half a page from the end
        if (count - (FlickrIntentService.PAGE_SIZE / 2) < index
                && FlickrIntentService.PAGE_SIZE - 1 < count) {
            listener.onListNearEnd();
        }
    }

    /**
     * Find the image with a resolution closest to the screen resolution.
     * @param images A list of images
     * @param resolution The prefered image resolution
     * @return The image with a resolution closest to the screens
     */
    private Image selectBestImage(ForeignCollection<Image> images, int resolution) {
        Image image = null;
        for (Image i : images) {
            if (image == null ||
                    Math.abs(i.getWidth() - resolution)
                            < Math.abs(image.getWidth() - resolution)) {
                image = i;
            }
        }
        return image;
    }
}