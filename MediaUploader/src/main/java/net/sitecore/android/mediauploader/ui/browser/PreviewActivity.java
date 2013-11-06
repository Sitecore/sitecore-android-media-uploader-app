package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PreviewActivity extends Activity implements LoaderCallbacks<Cursor> {
    public static final String IMAGE_ITEM_ID_KEY = "image_item_id";

    private PhotoViewAttacher mAttacher;
    private ImageView mImageView;
    private String mItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemId = getIntent().getStringExtra(IMAGE_ITEM_ID_KEY);
        if (TextUtils.isEmpty(mItemId)) {
            Toast.makeText(this, "You have to specify image url for this activity", Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.activity_preview);

        mImageView = (ImageView) findViewById(R.id.picture);

        mAttacher = new PhotoViewAttacher(mImageView);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Need to call clean-up
        mAttacher.cleanup();
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
//        if (data.getCount() != 0) {
//            String itemUrl = ScUtils.getMediaDownloadUrl(mItemId);
//            String itemName = data.getString(Query.DISPLAY_NAME);
//
//            setTitle(itemName);
//            UploaderApp.from(this).getImageLoader().load(itemUrl).placeholder(R.drawable.ic_placeholder)
//                    .error(R.drawable.ic_action_cancel)
//                    .into(mImageView);
//        }
    }
}
