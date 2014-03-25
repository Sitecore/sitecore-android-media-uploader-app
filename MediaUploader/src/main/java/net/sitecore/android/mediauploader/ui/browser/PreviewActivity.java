package net.sitecore.android.mediauploader.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.DownloadMediaOptions.Builder;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items.Query;
import net.sitecore.android.sdk.api.provider.ScItemsDatabase.Tables;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoView;

public class PreviewActivity extends Activity implements LoaderCallbacks<Cursor> {
    public static final int MAXIMUM_IMAGE_HEIGHT = 2000;
    public static final int MAXIMUM_IMAGE_WIDTH = 1000;
    private String SORT_ORDER = Tables.ITEMS + "." + Items.ITEM_ID + " asc";

    @InjectView(R.id.pager_pictures_preview) ViewPager mViewPager;

    private String mParentItemID;
    private String mCurrentItemID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Drawable drawable = getResources().getDrawable(R.color.preview_activity_action_bar);
        actionBar.setBackgroundDrawable(drawable);

        mParentItemID = getIntent().getStringExtra(IntentExtras.PARENT_ITEM_ID);
        mCurrentItemID = getIntent().getStringExtra(IntentExtras.CURRENT_ITEM_ID);
        if (TextUtils.isEmpty(mParentItemID)) {
            Toast.makeText(this, "You have to specify image url for this activity", Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.activity_preview);
        ButterKnife.inject(this);

        getLoaderManager().initLoader(0, null, this);
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
        String selection = Items.PARENT_ITEM_ID + "='" + mParentItemID + "'";
        return new CursorLoader(this, Items.CONTENT_URI, Query.PROJECTION, selection, null, SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Builder builder = new Builder();
        builder.maxWidth(MAXIMUM_IMAGE_WIDTH);
        builder.maxHeight(MAXIMUM_IMAGE_HEIGHT);

        ArrayList<String> urls = new ArrayList<>();
        final ArrayList<String> names = new ArrayList<>();
        int currentPosition = 0;

        while (data.moveToNext()) {
            String template = data.getString(Query.TEMPLATE);
            if (ScUtils.isImageTemplate(template)) {
                String itemUrl = ScUtils.getMediaDownloadUrl(this, data.getString(Query.ITEM_ID), builder.build());
                String itemName = data.getString(Query.DISPLAY_NAME);
                if (mCurrentItemID.equalsIgnoreCase(data.getString(Query.ITEM_ID))){
                    // Not the best solution
                    currentPosition = urls.size();
                }
                urls.add(itemUrl);
                names.add(itemName);
            }
        }
        prepareViewPager(names, urls, currentPosition);

    }

    private void prepareViewPager(final ArrayList<String> names, ArrayList<String> urls, int currentPosition) {
        mViewPager.setAdapter(new SamplePagerAdapter(urls));
        mViewPager.setCurrentItem(currentPosition);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle(names.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setTitle(names.get(currentPosition));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    class SamplePagerAdapter extends PagerAdapter {
        private ArrayList<String> mImageUrls = new ArrayList<>();

        SamplePagerAdapter(ArrayList<String> imageUrls) {
            mImageUrls = imageUrls;
        }

        @Override
        public int getCount() {
            return mImageUrls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            UploaderApp.from(PreviewActivity.this).getImageLoader().load(mImageUrls.get(position))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_action_cancel)
                    .into(photoView);

            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
