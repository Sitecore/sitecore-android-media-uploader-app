package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

import net.sitecore.android.mediauploader.R;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class SelectMediaDialogFragment extends DialogFragment {

    private static final int SOURCE_TYPE_GALLERY = 0;
    private static final int SOURCE_TYPE_CAMERA = 1;

    public interface SelectMediaListener {
        public void onImageSelected(Uri imageUri);
    }

    private SelectMediaListener mMediaSourceListener;
    private Uri mImageUri;

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SelectMediaListener)) {
            throw new IllegalArgumentException(activity.getTitle() + " must implement SelectMediaListener");
        }
        mMediaSourceListener = (SelectMediaListener) activity;
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new Builder(getActivity())
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

        return builder.create();
    }

    private void onGallerySelected() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, SOURCE_TYPE_GALLERY);
    }

    private void onCameraSelected() {
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
                return new File(getActivity().getCacheDir(), imageName);
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + imageName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SOURCE_TYPE_CAMERA) {
                LOGD("Selected image from camera: " + mImageUri.toString());
            } else if (requestCode == SOURCE_TYPE_GALLERY) {
                LOGD("Selected image from gallery: " + data.getDataString());
                mImageUri = Uri.parse(data.getDataString());
            }

            mMediaSourceListener.onImageSelected(mImageUri);
        }
    }

}
