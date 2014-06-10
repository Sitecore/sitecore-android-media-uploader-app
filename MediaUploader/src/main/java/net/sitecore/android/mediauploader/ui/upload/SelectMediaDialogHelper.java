package net.sitecore.android.mediauploader.ui.upload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sitecore.android.mediauploader.R;

import static android.os.Build.VERSION_CODES;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGD;

public class SelectMediaDialogHelper {

    public interface SelectMediaListener {
        public void onImageSelected(Uri imageUri);
    }

    private static final int SOURCE_TYPE_GALLERY = 5;
    private static final int SOURCE_TYPE_CAMERA = 6;
    private static final int SOURCE_TYPE_VIDEO = 7;

    private final Activity mActivity;
    private final SelectMediaListener mMediaSourceListener;

    private Uri mMediaUri;

    public SelectMediaDialogHelper(Activity activity, SelectMediaListener mediaSourceListener) {
        mMediaSourceListener = mediaSourceListener;
        mActivity = activity;
    }

    public void showDialog() {
        Resources res = mActivity.getResources();
        final String[] titles = res.getStringArray(R.array.select_image_source);
        final TypedArray icons = res.obtainTypedArray(R.array.select_image_source_icons);

        final OnClickListener onMediaSourceSelected = (dialog, which) -> {
            if (which == 0) onCameraSelected();
            else if (which == 1) onGallerySelected();
            else onVideoSelected();
        };

        new Builder(mActivity)
                .setAdapter(new MediaSourcesArrayAdapter(mActivity, titles, icons), onMediaSourceSelected)
                .setTitle(R.string.dialog_select_image)
                .create()
                .show();
    }

    private void onVideoSelected() {
        Intent intent = new Intent();
        intent.setType("video/*");
        if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            setDocumentAction(intent);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        mActivity.startActivityForResult(intent, SOURCE_TYPE_VIDEO);
    }

    private void onGallerySelected() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            setDocumentAction(intent);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        mActivity.startActivityForResult(intent, SOURCE_TYPE_GALLERY);
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private void setDocumentAction(Intent intent) {
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
    }

    private void onCameraSelected() {
        final File photo = getOutputMediaFile();
        mMediaUri = Uri.fromFile(photo);

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
            LOGD("Selected image from camera: " + mMediaUri.toString());
        } else if (requestCode == SOURCE_TYPE_GALLERY) {
            LOGD("Selected image from gallery: " + data.getDataString());
            mMediaUri = Uri.parse(data.getDataString());
        }  else if (requestCode == SOURCE_TYPE_VIDEO) {
            LOGD("Selected video from gallery: " + data.getDataString());
            mMediaUri = Uri.parse(data.getDataString());
        }

        mMediaSourceListener.onImageSelected(mMediaUri);
    }

    private static class MediaSourcesArrayAdapter extends ArrayAdapter<String> {

        private final TypedArray mIcons;

        public MediaSourcesArrayAdapter(Context context, String[] titles, TypedArray icons) {
            super(context, android.R.layout.select_dialog_item, android.R.id.text1, titles);
            mIcons = icons;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            final View v = super.getView(position, convertView, parent);
            TextView tv = (TextView) v.findViewById(android.R.id.text1);

            tv.setCompoundDrawablesWithIntrinsicBounds(mIcons.getDrawable(position), null, null, null);
            int dp12 = (int) (12 * getContext().getResources().getDisplayMetrics().density + 0.5f);
            tv.setCompoundDrawablePadding(dp12);

            return v;
        }
    }

}
