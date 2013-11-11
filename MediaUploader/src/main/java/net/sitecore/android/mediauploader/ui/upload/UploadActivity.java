package net.sitecore.android.mediauploader.ui.upload;

import android.app.ActionBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.UploadStatus;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Uploads;
import net.sitecore.android.mediauploader.service.MediaUploaderService;
import net.sitecore.android.mediauploader.ui.IntentExtras;
import net.sitecore.android.mediauploader.util.EmptyErrorListener;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.UploadMediaRequestOptions;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

import static net.sitecore.android.sdk.api.LogUtils.LOGD;

public class UploadActivity extends Activity implements Listener<ItemsResponse>, ErrorListener {

    private static final int SOURCE_TYPE_GALLERY = 0;
    private static final int SOURCE_TYPE_CAMERA = 1;

    @InjectView(R.id.edit_item_name) EditText mEditName;
    @InjectView(R.id.edit_item_path) EditText mEditPath;
    @InjectView(R.id.image_preview) ImageView mPreview;

    private Uri mImageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();

        setContentView(R.layout.activity_upload);
        Views.inject(this);

        String itemPath = getIntent().getStringExtra(IntentExtras.ITEM_PATH);
        mEditPath.setText(itemPath);
    }

    private void initActionBar() {
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View customActionBarView = inflater.inflate(R.layout.layout_cancel_upload, null);
        customActionBarView.findViewById(R.id.actionbar_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpload();
            }
        });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

    @OnClick(R.id.button_upload_gallery)
    public void openGalleryImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, SOURCE_TYPE_GALLERY);
    }

    @OnClick(R.id.button_upload_camera)
    public void openCameraImage() {
        final File photo = getOutputMediaFile();
        //final File photo = new File(getCacheDir(), "tmp.png");
        mImageUri = Uri.fromFile(photo);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        startActivityForResult(intent, SOURCE_TYPE_CAMERA);
    }

    private File getOutputMediaFile() {
        String imageName = "Camera_image.png";
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ScMobile");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return new File(getCacheDir(), imageName);
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + imageName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SOURCE_TYPE_CAMERA) {
                LOGD("Selected image from camera: " + mImageUri.toString());
            } else if (requestCode == SOURCE_TYPE_GALLERY) {
                LOGD("Selected image from gallery: " + data.getDataString());
                mImageUri = Uri.parse(data.getDataString());
            }

            mPreview.setImageURI(mImageUri);
        }
    }

    public void startUpload() {
        if (mImageUri == null) {
            Toast.makeText(this, "Select image first!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "upload started!", Toast.LENGTH_LONG).show();

        final ContentValues values = new ContentValues();
        values.put(Uploads.URL, "url");
        values.put(Uploads.USERNAME, "name");
        values.put(Uploads.PASSWORD, "password");
        values.put(Uploads.ITEM_NAME, mEditName.getText().toString());
        values.put(Uploads.ITEM_PATH, mEditPath.getText().toString());
        values.put(Uploads.FILE_URI, mImageUri.toString());
        values.put(Uploads.STATUS, UploadStatus.PENDING);

        new AsyncQueryHandler(getContentResolver()){}.startInsert(0, null, Uploads.CONTENT_URI, values);
        setResult(RESULT_OK);
        finish();
        /*
        UploaderApp.from(this).getSession(new Listener<ScApiSession>() {
            @Override
            public void onResponse(ScApiSession scApiSession) {
                uploadMedia(scApiSession);
            }
        }, new EmptyErrorListener());
        */
    }

    private void uploadMedia(ScApiSession session) {
        LOGD("Uploading: " + mImageUri.toString());
        UploadMediaRequestOptions options = session.uploadMedia(
                mEditPath.getText().toString(),
                mEditName.getText().toString(),
                mImageUri.toString());
        options.setFileName("image.png");

        MediaUploaderService.startUpload(UploadActivity.this, MediaUploaderService.class, options, this, this);
    }

    @Override
    public void onResponse(ItemsResponse itemsResponse) {
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
    }
}