package net.sitecore.android.mediauploader.ui.upload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.InstancesAsyncHandler;
import net.sitecore.android.mediauploader.requests.ReverseGeocodeRequest;
import net.sitecore.android.mediauploader.ui.location.LocationActivity;
import net.sitecore.android.mediauploader.ui.settings.ImageSize;
import net.sitecore.android.mediauploader.ui.settings.SettingsActivity;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
import net.sitecore.android.mediauploader.util.ImageHelper;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequestQueue;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;

public class UploadActivity extends Activity implements SelectMediaListener,
        GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener,
        ErrorListener {
    public static final int LOCATION_ACTIVITY_CODE = 10;

    private final int MAX_IMAGE_WIDTH = 2000;

    private final int MAX_IMAGE_HEIGHT = 2000;
    @InjectView(R.id.edit_name) EditText mEditName;
    @InjectView(R.id.image_preview) ImageView mPreview;
    @InjectView(R.id.button_location) ImageButton mLocationButton;

    @InjectView(R.id.textview_location) TextView mLocationText;
    @Inject Picasso mImageLoader;
    @Inject Instance mInstance;
    @Inject ScRequestQueue mRequestQueue;

    @Inject ScApiSession mApiSession;
    private Uri mImageUri;
    private ImageHelper mImageHelper;
    private Address mImageAddress;

    private SelectMediaDialogHelper mMediaDialogHelper;
    private LocationClient mLocationClient;

    private Listener<ArrayList<Address>> mReverseGeocodelistener = new Listener<ArrayList<Address>>() {
        @Override
        public void onResponse(ArrayList<Address> addresses) {
            setProgressBarIndeterminateVisibility(false);
            if (addresses.size() != 0) {
                mImageAddress = addresses.get(0);
                mLocationText.setText(mImageAddress.address);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_upload);

        ButterKnife.inject(this);

        mImageUri = getIntent().getData();

        mImageHelper = new ImageHelper(this);
        mLocationClient = new LocationClient(this, this, this);

        // This adds ability to get real mPreview width and height.
        mPreview.getViewTreeObserver().addOnGlobalLayoutListener(VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN
                ? new V16OnGlobalLayoutListener()
                : new LegacyOnGlobalLayoutListener());

        proccessImageLocation();
    }

    @Override protected void onResume() {
        super.onResume();
        UploaderApp.from(this).inject(this);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void proccessImageLocation() {
        if (mImageAddress != null) return;

        LatLng latLng = ImageHelper.getLatLngFromImage(mImageUri.toString());
        if (latLng != null) {
            performReverseGeocodingRequest(latLng);
        } else {
            if (servicesConnected()) mLocationClient.connect();
            else {
                mLocationButton.setEnabled(false);
            }
        }
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

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
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

    @OnClick(R.id.button_upload_now)
    public void onUploadNow() {
        final UploadHelper mUploadHelper = new UploadHelper(getApplicationContext());
        final String itemName = getItemName();
        ImageSize imageSize = ImageSize.valueOf(Prefs.from(this).getString(R.string.key_current_image_size,
                ImageSize.ACTUAL.name()));

        new InstancesAsyncHandler(getContentResolver()) {
            @Override protected void onInsertComplete(int token, Object cookie, Uri uri) {
                mUploadHelper.uploadMedia(mApiSession, uri, mInstance, itemName, mImageUri.toString(), mImageAddress);
            }
        }.insertDelayedUpload(itemName, mImageUri, mInstance, imageSize, mImageAddress);
        finish();
    }

    @OnClick(R.id.button_upload_later)
    public void onUploadLater() {
        ImageSize imageSize = ImageSize.valueOf(Prefs.from(this).getString(R.string.key_current_image_size,
                ImageSize.ACTUAL.name()));
        new InstancesAsyncHandler(getContentResolver()).insertDelayedUpload(getItemName(), mImageUri, mInstance,
                imageSize, mImageAddress);
        showToast(this, R.string.toast_added_to_uploads);
        finish();
    }

    @OnClick(R.id.image_preview)
    public void onPreviewClick() {
        mMediaDialogHelper = new SelectMediaDialogHelper(this, this);
        mMediaDialogHelper.showDialog();
    }

    @OnClick(R.id.button_location)
    public void onLocation() {
        Intent intent = LocationActivity.prepareIntent(this, mImageAddress);
        startActivityForResult(intent, LOCATION_ACTIVITY_CODE);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOCATION_ACTIVITY_CODE) {
                mImageAddress = data.getParcelableExtra(LocationActivity.EXTRA_ADDRESS);
                if (mImageAddress != null) mLocationText.setText(mImageAddress.address);
            } else {
                mMediaDialogHelper.onActivityResult(requestCode, data);
            }
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

        mImageAddress = null;
        mLocationText.setText("");
        proccessImageLocation();
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return ConnectionResult.SUCCESS == resultCode;
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        Location location = mLocationClient.getLastLocation();
        if (location == null) {
            startLocationUpdate();
        } else {
            performReverseGeocodingRequest(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    private void startLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setNumUpdates(1);
        locationRequest.setFastestInterval(100);

        mLocationClient.requestLocationUpdates(locationRequest, new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                performReverseGeocodingRequest(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private void performReverseGeocodingRequest(LatLng latLng) {
        Request request = new ReverseGeocodeRequest(latLng, mReverseGeocodelistener, this);
        mRequestQueue.add(request);
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}