package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import javax.inject.Inject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.service.MediaUploaderService;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.UploadMediaIntentBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class UploadActivity extends Activity implements ErrorListener, SelectMediaListener {
    public static final int MAX_WIDTH = 500;
    public static final int MAX_HEIGHT = 500;

    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) ImageView mPreview;

    @Inject Picasso mImageLoader;
    @Inject ScApiSession mApiSession;
    @Inject UploaderPrefs mUploaderPrefs;

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
        mImageLoader.load(mImageUri).fit()
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
        final ContentValues values = populateUploadValues();
        values.put(Uploads.STATUS, UploadStatus.IN_PROGRESS.name());

        new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, final Uri uri) {
                uploadMedia(uri);
            }
        }.startInsert(0, null, Uploads.CONTENT_URI, values);

        Toast.makeText(this, "Uploading media started.", Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }

    @OnClick(R.id.button_upload_later)
    public void onUploadLater() {
        final ContentValues values = populateUploadValues();
        values.put(Uploads.STATUS, UploadStatus.PENDING.name());

        new AsyncQueryHandler(getContentResolver()) {
        }.startInsert(0, null, Uploads.CONTENT_URI, values);

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

    private ContentValues populateUploadValues() {
        Instance instance = mUploaderPrefs.getCurrentInstance();

        final ContentValues values = new ContentValues();
        values.put(Uploads.URL, instance.getUrl());
        values.put(Uploads.USERNAME, instance.getLogin());
        values.put(Uploads.PASSWORD, instance.getPassword());
        values.put(Uploads.ITEM_NAME, mEditName.getText().toString());
        values.put(Uploads.ITEM_PATH, instance.getRootFolder());
        values.put(Uploads.FILE_URI, mImageUri.toString());

        return values;
    }

    private void uploadMedia(final Uri uploadUri) {
        LOGD("Uploading: " + mImageUri.toString());

        Instance instance = UploaderPrefs.from(this).getCurrentInstance();
        String itemName = mEditName.getText().toString();

        mApiSession.setMediaLibraryPath("/");
        MediaUploadListener responseListener = new
                MediaUploadListener(getApplicationContext(), uploadUri, itemName);

        UploadMediaIntentBuilder builder = mApiSession.uploadMediaIntent(instance.getRootFolder(), itemName,
                mImageUri.toString())
                .setDatabase(instance.getDatabase())
                .setSuccessListener(responseListener)
                .setErrorListener(responseListener)
                .setUploadMediaServiceClass(MediaUploaderService.class);

        if (!TextUtils.isEmpty(instance.getSite())) builder.setSite(instance.getSite());
        Intent intent = builder.build(this);
        intent.putExtra(MediaUploaderService.EXTRA_UPLOAD_URI, uploadUri);

        startService(intent);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        //TODO: handle error
    }

    @Override public void onImageSelected(Uri imageUri) {
        mImageUri = imageUri;
        mImageLoader.load(mImageUri).fit()
                .placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_action_cancel)
                .into(mPreview);
    }
}