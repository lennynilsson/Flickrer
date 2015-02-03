package se.bylenny.flickrer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.squareup.picasso.Picasso;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class ImageActivity extends ActionBarActivity {

    private static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";
    private static final String ACTION_SHOW_IMAGE = "se.bylenny.flickrer.action.ACTION_SHOW_IMAGE";
    private static final String EXTRA_IMAGE_TITLE = "se.bylenny.flickrer.action.ACTION_TITLE";
    private ImageViewTouch image;

    public static Intent getIntent(Context context, String title, String imageUrl) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_IMAGE_URL, imageUrl);
        bundle.putString(EXTRA_IMAGE_TITLE, title);
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtras(bundle);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Bundle extras = getIntent().getExtras();
        String url = null;
        String title = null;
        if (extras != null) {
            url = extras.getString(EXTRA_IMAGE_URL);
            title = extras.getString(EXTRA_IMAGE_TITLE);
        } else {
            finish();
        }
        image = (ImageViewTouch) findViewById(R.id.image);
        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        Picasso.with(getApplicationContext())
                .load(url)
                .error(R.color.red)
                .into(image);
        setTitle(title);
        getSupportActionBar().setTitle(title);
    }

}
