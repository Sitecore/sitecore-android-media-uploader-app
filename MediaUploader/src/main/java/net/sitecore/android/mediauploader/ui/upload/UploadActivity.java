package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import javax.inject.Inject;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.provider.InstancesAsyncHandler;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;

public class UploadActivity extends Activity implements SelectMediaListener {

    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) ImageView mPreview;

    @Inject Picasso mImageLoader;
    @Inject Instance mInstance;
    @Inject ScApiSession mApiSession;

    private Uri mImageUri;
    private SelectMediaDialogHelper mMediaDialogHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mImageUri = getIntent().getData();
        mImageLoader.load(mImageUri)
                .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_action_cancel)
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
        new InstancesAsyncHandler(getContentResolver()){
            @Override protected void onInsertComplete(int token, Object cookie, Uri uri) {
                mUploadHelper.uploadMedia(mApiSession, uri, mInstance, itemName, mImageUri.toString());
            }
        }.insertDelayedUpload(itemName, mImageUri, mInstance);
        finish();
    }

    @OnClick(R.id.button_upload_later)
    public void onUploadLater() {
        new InstancesAsyncHandler(getContentResolver()).insertDelayedUpload(getItemName(), mImageUri, mInstance);
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
        mImageLoader.load(mImageUri).fit()
                .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_action_cancel)
                .into(mPreview);
    }
}