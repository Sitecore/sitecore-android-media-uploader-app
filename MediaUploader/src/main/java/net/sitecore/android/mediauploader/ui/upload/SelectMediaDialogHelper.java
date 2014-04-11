package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sitecore.android.mediauploader.R;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class SelectMediaDialogHelper {

    private static final int SOURCE_TYPE_GALLERY = 5;
    private static final int SOURCE_TYPE_CAMERA = 6;

    private final Activity mActivity;
    private final SelectMediaListener mMediaSourceListener;

    public interface SelectMediaListener {
        public void onImageSelected(Uri imageUri);
    }

    private Uri mImageUri;

    public SelectMediaDialogHelper(Activity activity, SelectMediaListener mediaSourceListener) {
        mMediaSourceListener = mediaSourceListener;
        mActivity = activity;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new Builder(mActivity)
                .setItems(R.array.select_image_source, new OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            onCameraSelected();
                        } else {
                            onGallerySelected();
                        }
                    }
                })
                .setTitle(R.string.dialog_select_image);

        builder.create().show();
    }

    private void onGallerySelected() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mActivity.startActivityForResult(intent, SOURCE_TYPE_GALLERY);
    }

    private void onCameraSelected() {
        final File photo = getOutputMediaFile();
        mImageUri = Uri.fromFile(photo);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));

        mActivity.startActivityForResult(intent, SOURCE_TYPE_CAMERA);
    }

    private File getOutputMediaFile() {
        String dateString = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date(System.currentTimeMillis()));
        String imageName = "Image_" + dateString + ".png";
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ScMobile");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return new File(mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + imageName);
    }

    public void onActivityResult(int requestCode, Intent data) {
        if (requestCode == SOURCE_TYPE_CAMERA) {
            LOGD("Selected image from camera: " + mImageUri.toString());
        } else if (requestCode == SOURCE_TYPE_GALLERY) {
            LOGD("Selected image from gallery: " + data.getDataString());
            mImageUri = Uri.parse(data.getDataString());
        }

        mMediaSourceListener.onImageSelected(mImageUri);
    }

}
