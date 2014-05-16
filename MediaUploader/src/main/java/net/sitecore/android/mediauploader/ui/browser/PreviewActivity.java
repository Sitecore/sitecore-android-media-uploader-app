package net.sitecore.android.mediauploader.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.Toast;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.SortByMediaFolderTemplateComparator;
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
        ScItemsLoader loader = new ScItemsLoader(this, selection, null);
        loader.setItemsSortOrder(new SortByMediaFolderTemplateComparator());
        return loader;
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
        mViewPager.setAdapter(new PreviewAdapter(this, urls, mImageLoader));
        mViewPager.setCurrentItem(currentPosition);
        mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                setTitle(names.get(position));
            }
        });
        setTitle(names.get(currentPosition));
    }

    @Override
    public void onLoaderReset(Loader<List<ScItem>> loader) {
    }

    static class PreviewAdapter extends PagerAdapter {
        private final ArrayList<String> mImageUrls;
        private final Picasso mImageLoader;
        private final LayoutInflater mLayoutInflater;

        PreviewAdapter(Context context, ArrayList<String> imageUrls, Picasso imageLoader) {
            mImageUrls = imageUrls;
            mImageLoader = imageLoader;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mImageUrls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View root = mLayoutInflater.inflate(R.layout.pager_item_preview, container, false);
            final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);

            final PhotoView photoView = (PhotoView) root.findViewById(R.id.preview);
            photoView.setScaleType(ScaleType.CENTER_INSIDE);

            mImageLoader.load(mImageUrls.get(position))
                    .error(R.drawable.ic_action_cancel)
                    .into(photoView, new Callback() {
                        @Override public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override public void onError() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            container.addView(root, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return root;
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
