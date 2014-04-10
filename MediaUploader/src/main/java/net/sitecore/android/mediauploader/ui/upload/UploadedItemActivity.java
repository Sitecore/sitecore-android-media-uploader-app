package net.sitecore.android.mediauploader.ui.upload;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;

import javax.inject.Inject;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;

import uk.co.senab.photoview.PhotoView;

public class UploadedItemActivity extends Activity {
    public final static String EXTRA_IMAGE_URI = "image_uri";
    public final static String EXTRA_ITEM_NAME = "item_name";

    @Inject Picasso mImageLoader;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UploaderApp.from(this).inject(this);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Drawable drawable = getResources().getDrawable(R.color.preview_activity_action_bar);
        actionBar.setBackgroundDrawable(drawable);

        String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        setTitle(getIntent().getStringExtra(EXTRA_ITEM_NAME));

        PhotoView photoView = new PhotoView(this);
        photoView.setScaleType(ScaleType.CENTER_INSIDE);

        mImageLoader.load(imageUri)
                .centerInside()
                .fit()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_action_cancel)
                .into(photoView);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setContentView(photoView, params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
