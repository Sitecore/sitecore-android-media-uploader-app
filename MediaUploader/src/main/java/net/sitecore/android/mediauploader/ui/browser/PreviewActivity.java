package net.sitecore.android.mediauploader.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.squareup.picasso.Callback;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.MediaParamsBuilder;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PreviewActivity extends Activity implements LoaderCallbacks<Cursor>, Callback {
    public static final String IMAGE_ITEM_ID_KEY = "image_item_id";
    public static final int MAXIMUM_IMAGE_HEIGHT = 2000;
    public static final int MAXIMUM_IMAGE_WIDTH = 1000;

    private PhotoViewAttacher mAttacher;
    private String mItemId;

    @InjectView(R.id.picture) ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Drawable drawable = getResources().getDrawable(R.color.preview_activity_action_bar);
        actionBar.setBackgroundDrawable(drawable);

        mItemId = getIntent().getStringExtra(IMAGE_ITEM_ID_KEY);
        if (TextUtils.isEmpty(mItemId)) {
            Toast.makeText(this, "You have to specify image url for this activity", Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.activity_preview);
        ButterKnife.inject(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAttacher != null) mAttacher.cleanup();
        UploaderApp.from(this).getImageLoader().cancelRequest(mImageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = Items.ITEM_ID + "='" + mItemId + "'";
        return new CursorLoader(this, Items.CONTENT_URI, Query.PROJECTION, selection, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            MediaParamsBuilder params = new MediaParamsBuilder();
            params.maxWidth(MAXIMUM_IMAGE_WIDTH);
            params.maxHeight(MAXIMUM_IMAGE_HEIGHT);

            String itemUrl = ScUtils.getMediaDownloadUrl(this, mItemId, params);
            String itemName = data.getString(Query.DISPLAY_NAME);

            setTitle(itemName);

            UploaderApp.from(this).getImageLoader().load(itemUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_action_cancel)
                    .into(mImageView, this);
        }
    }

    @Override
    public void onSuccess() {
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error happened while downloading image", Toast.LENGTH_LONG)
                .show();
    }
}
