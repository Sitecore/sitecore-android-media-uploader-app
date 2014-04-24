package net.sitecore.android.mediauploader.ui.upload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;

import javax.inject.Inject;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.InstancesAsyncHandler;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.util.ImageHelper;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;

public class UploadActivity extends Activity implements SelectMediaListener {
    private final int MAX_IMAGE_WIDTH = 2000;
    private final int MAX_IMAGE_HEIGHT = 2000;

    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) ImageView mPreview;

    @Inject Picasso mImageLoader;
    @Inject Instance mInstance;
    @Inject ScApiSession mApiSession;

    private Uri mImageUri;
    private ImageSize mCurrentImageSize;
    private ImageHelper mImageHelper;
    private SelectMediaDialogHelper mMediaDialogHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mImageUri = getIntent().getData();
        mCurrentImageSize = ImageSize.valueOf(Prefs.from(this).getString(R.string.key_current_image_size,
                ImageSize.ACTUAL.name()));
        mImageHelper = new ImageHelper(this);

        // This adds ability to get real mPreview width and height.
        mPreview.getViewTreeObserver().addOnGlobalLayoutListener(VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN
                ? new V16OnGlobalLayoutListener()
                : new LegacyOnGlobalLayoutListener());
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private class V16OnGlobalLayoutListener implements OnGlobalLayoutListener {
        @Override public void onGlobalLayout() {
            mPreview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            loadImageIntoPreview();
        }
    }

    private class LegacyOnGlobalLayoutListener implements OnGlobalLayoutListener {
        @Override public void onGlobalLayout() {
            mPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            loadImageIntoPreview();
        }
    }

    private void loadImageIntoPreview() {
        RequestCreator creator = mImageLoader.load(mImageUri);
        if (mImageHelper.isResizeNeeded(mImageUri.toString(), MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)) {
            creator.resize(mPreview.getWidth(), mPreview.getHeight())
                    .centerInside();
        }
        creator.placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_action_cancel)
                .into(mPreview);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.button_upload_now)
    public void onUploadNow() {
        final UploadHelper mUploadHelper = new UploadHelper(getApplicationContext());
        final String itemName = getItemName();

        new InstancesAsyncHandler(getContentResolver()) {
            @Override protected void onInsertComplete(int token, Object cookie, Uri uri) {
                mUploadHelper.uploadMedia(mApiSession, uri, mInstance, itemName, mImageUri.toString());
            }
        }.insertDelayedUpload(itemName, mImageUri, mInstance, mCurrentImageSize);
        finish();
    }

    @OnClick(R.id.button_upload_later)
    public void onUploadLater() {
        new InstancesAsyncHandler(getContentResolver()).insertDelayedUpload(getItemName(), mImageUri, mInstance,
                mCurrentImageSize);
        showToast(this, R.string.toast_added_to_uploads);
        finish();
    }

    @OnClick(R.id.image_preview)
    public void onPreviewClick() {
        mMediaDialogHelper = new SelectMediaDialogHelper(this, this);
        mMediaDialogHelper.showDialog();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            mMediaDialogHelper.onActivityResult(requestCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getItemName() {
        String itemName = mEditName.getText().toString();
        if (TextUtils.isEmpty(itemName)) {
            String dateString = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date(System.currentTimeMillis()));
            itemName = "Image_" + dateString;
        }
        return itemName;
    }

    @Override public void onImageSelected(Uri imageUri) {
        mImageUri = imageUri;
        loadImageIntoPreview();
    }
}