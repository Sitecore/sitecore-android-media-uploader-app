package net.sitecore.android.mediauploader.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
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

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.DownloadMediaOptions.Builder;
import net.sitecore.android.sdk.api.model.ScItem;
import net.sitecore.android.sdk.api.model.ScItemsLoader;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoView;

public class PreviewActivity extends Activity implements LoaderCallbacks<List<ScItem>> {
    public static final String PARENT_ITEM_ID = "parent_item_id";
    public static final String CURRENT_ITEM_ID = "current_item_id";

    public static final int MAXIMUM_IMAGE_HEIGHT = 2000;
    public static final int MAXIMUM_IMAGE_WIDTH = 1000;

    @InjectView(R.id.pager_pictures_preview) ViewPager mViewPager;
    @Inject Picasso mImageLoader;

    private String mParentItemID;
    private String mCurrentItemID;
    private String mInstanceUrl;
    private String mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UploaderApp.from(this).inject(this);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Drawable drawable = getResources().getDrawable(R.color.preview_activity_action_bar);
        actionBar.setBackgroundDrawable(drawable);

        mParentItemID = getIntent().getStringExtra(PARENT_ITEM_ID);
        mCurrentItemID = getIntent().getStringExtra(CURRENT_ITEM_ID);
        mInstanceUrl = getIntent().getStringExtra(BrowserFragment.INSTANCE_URL);
        mDatabase = getIntent().getStringExtra(BrowserFragment.INSTANCE_DATABASE);
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
    public Loader<List<ScItem>> onCreateLoader(int id, Bundle args) {
        String selection = Items.PARENT_ITEM_ID + "='" + mParentItemID + "'";
        return new ScItemsLoader(this, selection, null);
    }

    @Override
    public void onLoadFinished(Loader<List<ScItem>> loader, List<ScItem> data) {
        if (data == null || data.size() == 0) return;

        Builder builder = new Builder();
        builder.maxWidth(MAXIMUM_IMAGE_WIDTH);
        builder.maxHeight(MAXIMUM_IMAGE_HEIGHT);
        builder.database(mDatabase);

        ArrayList<String> urls = new ArrayList<>();
        final ArrayList<String> names = new ArrayList<>();
        int currentPosition = 0;

        for (ScItem scItem : data) {
            if (ScUtils.isImageTemplate(scItem.getTemplate())) {
                String itemUrl = mInstanceUrl + scItem.getMediaDownloadUrl(builder.build());
                if (mCurrentItemID.equalsIgnoreCase(scItem.getId())) {
                    // Not the best solution
                    currentPosition = urls.size();
                }
                urls.add(itemUrl);
                names.add(scItem.getDisplayName());
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
    public void onLoaderReset(Loader<List<ScItem>> loader) {
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

            mImageLoader.load(mImageUrls.get(position))
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
