package net.sitecore.android.mediauploader.ui.upload;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.model.request.ReverseGeocodeRequest;
import net.sitecore.android.mediauploader.provider.InstancesAsyncHandler;
import net.sitecore.android.mediauploader.service.UploadHelper;
import net.sitecore.android.mediauploader.ui.location.LocationActivity;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;
import net.sitecore.android.mediauploader.ui.settings.SettingsActivity;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.mediauploader.widget.NotifyingLayoutFinishedImageView;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequestQueue;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.getLatLngFromImage;
import static net.sitecore.android.mediauploader.util.Utils.showToast;
import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class UploadActivity extends Activity {

    private static final int MAX_IMAGE_WIDTH = 2000;
    private static final int MAX_IMAGE_HEIGHT = 2000;
    private static final String EXTRA_IS_IMAGE = "extra_is_image";

    public static final int LOCATION_ACTIVITY_CODE = 10;

    public static void uploadImage(Activity parent, Uri imageUri) {
        final Intent intent = new Intent(parent, UploadActivity.class);
        intent.setData(imageUri);
        intent.putExtra(EXTRA_IS_IMAGE, true);
        parent.startActivity(intent);
    }

    public static void uploadVideo(Activity parent, Uri videoUri) {
        final Intent intent = new Intent(parent, UploadActivity.class);
        intent.setData(videoUri);
        intent.putExtra(EXTRA_IS_IMAGE, false);
        parent.startActivity(intent);
    }

    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) NotifyingLayoutFinishedImageView mPreview;
    @InjectView(R.id.button_location) ImageButton mLocationButton;
    @InjectView(R.id.layout_location) LinearLayout mLocationViewsGroup;
    @InjectView(R.id.text_location) TextView mLocationText;

    @Inject Picasso mImageLoader;
    @Inject Instance mInstance;
    @Inject ScRequestQueue mRequestQueue;
    @Inject ScApiSession mApiSession;
    @Inject Prefs mPrefs;

    private boolean mIsImageSelected;
    private Uri mMediaUri;
    private Address mAddress;

    private SelectMediaDialogHelper mMediaDialogHelper;
    private LocationClient mLocationClient;

    private final Listener<ArrayList<Address>> mReverseGeocodeListener = addresses -> {
        setProgressBarIndeterminateVisibility(false);
        if (addresses.size() != 0) {
            mAddress = addresses.get(0);
            mLocationText.setText(mAddress.address);
        }
    };

    private final SelectMediaListener mSelectMediaListener = new SelectMediaListener() {
        @Override public void onImageSelected(Uri imageUri) {
            mIsImageSelected = true;
            mMediaUri = imageUri;
            updateImageUi();
        }

        @Override public void onVideoSelected(Uri videoUri) {
            mIsImageSelected = false;
            mMediaUri = videoUri;
            updateVideoUi();
        }
    };

    private void updateImageUi() {
        mLocationViewsGroup.setVisibility(View.VISIBLE);
        mEditName.setText(generateMediaItemName());

        mAddress = null;
        mLocationText.setText("");

        mImageLoader.load(mMediaUri)
                .fit().centerInside()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_action_cancel)
                .into(mPreview);

        processImageLocation();
    }

    private void updateVideoUi() {
        mLocationViewsGroup.setVisibility(View.INVISIBLE);

        mEditName.setText(generateMediaItemName());

        mAddress = null; // don't assign location to videos
        mPreview.setImageResource(R.drawable.ic_template_video);
    }

    private final ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle dataBundle) {
            Location location = mLocationClient.getLastLocation();
            if (location == null) {
                startLocationUpdate();
            } else {
                performReverseGeocodingRequest(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }

        @Override
        public void onDisconnected() {
        }
    };


    private final ErrorListener mErrorListener = error -> {
        setProgressBarIndeterminateVisibility(false);
        LOGE(error);
    };

    private final OnConnectionFailedListener mConnectionFailedListener = connectionResult -> {
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_upload);

        ButterKnife.inject(this);

        mIsImageSelected = getIntent().getBooleanExtra(EXTRA_IS_IMAGE, true);
        mMediaUri = getIntent().getData();

        mLocationClient = new LocationClient(this, mConnectionCallbacks, mConnectionFailedListener);

        mPreview.setOnLayoutFinishedListener(() -> {
            if (mIsImageSelected) {
                updateImageUi();
            } else {
                updateVideoUi();
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        UploaderApp.from(this).inject(this);
        if (mInstance == null) finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void processImageLocation() {
        if (mAddress != null) return;

        LatLng latLng = getLatLngFromImage(mMediaUri);
        if (latLng != null) {
            performReverseGeocodingRequest(latLng);
        } else {
            if (isPlayServicesConnected()) mLocationClient.connect();
            else {
                mLocationButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    @OnClick(R.id.button_upload_now)
    public void onUploadNow() {
        final UploadHelper mUploadHelper = new UploadHelper(getApplicationContext());
        final String itemName = getValidMediaItemName();
        ImageSize imageSize = ImageSize.valueOf(mPrefs.getString(R.string.key_current_image_size,
                ImageSize.ACTUAL.name()));

        new InstancesAsyncHandler(getContentResolver()) {
            @Override protected void onInsertComplete(int token, Object cookie, Uri uri) {
                mUploadHelper.startUploadService(mApiSession, uri, mInstance, itemName, mMediaUri.toString(),
                        mAddress, mIsImageSelected);
            }
        }.insertDelayedImageUpload(itemName, mMediaUri, mInstance, imageSize, mAddress, mIsImageSelected);
        finish();
    }

    @OnClick(R.id.button_upload_later)
    public void onUploadLater() {
        ImageSize imageSize = ImageSize.valueOf(mPrefs.getString(R.string.key_current_image_size,
                ImageSize.ACTUAL.name()));
        new InstancesAsyncHandler(getContentResolver()).insertDelayedImageUpload(getValidMediaItemName(), mMediaUri, mInstance,
                imageSize, mAddress, mIsImageSelected);
        showToast(this, R.string.toast_added_to_uploads);
        finish();
    }

    @OnClick(R.id.image_preview)
    public void onPreviewClick() {
        mMediaDialogHelper = new SelectMediaDialogHelper(this, mSelectMediaListener);
        mMediaDialogHelper.showDialog();
    }

    @OnClick(R.id.button_location)
    public void onPickLocationClick() {
        Intent intent = LocationActivity.prepareIntent(this, mAddress);
        startActivityForResult(intent, LOCATION_ACTIVITY_CODE);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOCATION_ACTIVITY_CODE) {
                mAddress = data.getParcelableExtra(LocationActivity.EXTRA_ADDRESS);
                if (mAddress != null) mLocationText.setText(mAddress.address);
            } else {
                mMediaDialogHelper.onActivityResult(requestCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getValidMediaItemName() {
        String itemName = mEditName.getText().toString();
        if (TextUtils.isEmpty(itemName)) {
            itemName = generateMediaItemName();
        }

        // ItemsWebAPI doesn't understand non-alphanumeric chars
        return ScUtils.cleanupMediaItemName(itemName);
    }

    private String generateMediaItemName() {
        return mIsImageSelected
                ? "Image_" + Utils.getCurrentDate()
                : "Video_" + Utils.getCurrentDate();
    }

    private boolean isPlayServicesConnected() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return ConnectionResult.SUCCESS == resultCode;
    }

    private void startLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setNumUpdates(1);
        locationRequest.setFastestInterval(100);

        mLocationClient.requestLocationUpdates(
                locationRequest,
                location -> performReverseGeocodingRequest(new LatLng(location.getLatitude(), location.getLongitude()))
        );
    }

    private void performReverseGeocodingRequest(LatLng latLng) {
        Request request = new ReverseGeocodeRequest(latLng, mReverseGeocodeListener, mErrorListener);
        mRequestQueue.add(request);
        setProgressBarIndeterminateVisibility(true);
    }

}