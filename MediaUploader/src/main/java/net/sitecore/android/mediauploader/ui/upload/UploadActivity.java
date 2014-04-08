package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import javax.inject.Inject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class UploadActivity extends Activity implements ErrorListener, SelectMediaListener {
    public static final int MAX_WIDTH = 500;
    public static final int MAX_HEIGHT = 500;

    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) ImageView mPreview;

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
        loadImageIntoImageView();
    }

    private void loadImageIntoImageView() {
        try {
            mPreview.setImageBitmap(decodeBitmapFromStream(MAX_WIDTH, MAX_HEIGHT));
        } catch (IOException e) {
            LOGE(e);
            mPreview.setImageResource(R.drawable.ic_action_cancel);
        }
    }

    public Bitmap decodeBitmapFromStream(int reqWidth, int reqHeight) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getInputStreamFromUri(mImageUri.toString()), null, options);

        options.inSampleSize = calculateTargetImageSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getInputStreamFromUri(mImageUri.toString()), null, options);
    }

    public int calculateTargetImageSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private InputStream getInputStreamFromUri(String path) throws IOException {
        if (path.startsWith("content:")) {
            Uri uri = Uri.parse(path);
            return getContentResolver().openInputStream(uri);
        }

        if (path.startsWith("http:") || path.startsWith("https:") || path.startsWith("file:")) {
            URL url = new URL(path);
            return url.openStream();
        } else {
            return new FileInputStream(path);
        }
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
        loadImageIntoImageView();
    }
}