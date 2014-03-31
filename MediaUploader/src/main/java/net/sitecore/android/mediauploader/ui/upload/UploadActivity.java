package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import javax.inject.Inject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogFragment.SelectMediaListener;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class UploadActivity extends Activity implements ErrorListener, SelectMediaListener {

    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) ImageView mPreview;

    @Inject ScApiSession mApiSession;
    @Inject UploaderPrefs mUploaderPrefs;

    private Uri mImageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mImageUri = getIntent().getData();
        mPreview.setImageURI(mImageUri);
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

        Toast.makeText(this, "Added to My Uploads", Toast.LENGTH_LONG).show();

        new AsyncQueryHandler(getContentResolver()){
        }.startInsert(0, null, Uploads.CONTENT_URI, values);
    }

    @OnClick(R.id.image_preview)
    public void onPreviewClick() {
        new SelectMediaDialogFragment().show(getFragmentManager(), "dialog");
    }

    private ContentValues populateUploadValues() {
        Instance instance = mUploaderPrefs.getCurrentInstance();

        final ContentValues values = new ContentValues();
        values.put(Uploads.URL, instance.getUrl());
        values.put(Uploads.USERNAME, instance.getLogin());
        values.put(Uploads.PASSWORD, instance.getPassword());
        values.put(Uploads.ITEM_NAME, mEditName.getText().toString());
        values.put(Uploads.FILE_URI, mImageUri.toString());

        return values;
    }

    private void uploadMedia(final Uri uploadUri) {
        LOGD("Uploading: " + mImageUri.toString());
        Intent intent = mApiSession.uploadMediaIntent("/sitecore/media library",
                mEditName.getText().toString(),
                mImageUri.toString())
                .setSuccessListener(new SuccessfulMediaUploadListener(getContentResolver(), uploadUri))
                .build(this);

        startService(intent);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        //TODO: handle error
    }

    @Override public void onImageSelected(Uri imageUri) {
        mImageUri = imageUri;
        mPreview.setImageURI(mImageUri);
    }
}