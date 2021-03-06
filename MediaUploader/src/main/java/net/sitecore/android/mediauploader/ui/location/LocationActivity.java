package net.sitecore.android.mediauploader.ui.location;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.model.Address;
import net.sitecore.android.mediauploader.model.request.GeocodingRequest;
import net.sitecore.android.mediauploader.model.request.ReverseGeocodeRequest;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.ScRequestQueue;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;

import static net.sitecore.android.mediauploader.util.Utils.showToast;

public class LocationActivity extends Activity implements ErrorListener {

    public final static String EXTRA_ADDRESS = "location";

    @InjectView(R.id.edit_location) EditText mLocationField;
    @InjectView(R.id.button_search_location) Button mSearchAddressButton;

    @Inject ScRequestQueue mRequestQueue;

    private Address mCurrentAddress;
    private MapFragment mMapFragment;
    private View mUseMenuView;

    private Marker mSelectedLocationMarker;

    private Listener<ArrayList<Address>> mGeocodeListener = new Listener<ArrayList<Address>>() {
        @Override
        public void onResponse(ArrayList<Address> addresses) {
            setLoading(false);
            if (addresses.size() != 0) {
                mCurrentAddress = addresses.get(0);
                setTitle(mCurrentAddress.address);

                if (mMapFragment.isDetached() || mMapFragment.getMap() == null) return;

                CameraUpdate center = CameraUpdateFactory.newLatLng(mCurrentAddress.latLng);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                mMapFragment.getMap().moveCamera(center);
                mMapFragment.getMap().animateCamera(zoom);
            }
        }
    };

    private Listener<ArrayList<Address>> mReverseGecoodeListener = new Listener<ArrayList<Address>>() {
        @Override
        public void onResponse(ArrayList<Address> addresses) {
            setLoading(false);
            if (addresses.size() != 0) {
                mCurrentAddress = addresses.get(0);
                setTitle(mCurrentAddress.address);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        initActionBar();

        UploaderApp.from(this).inject(this);
        setContentView(R.layout.activity_location);
        ButterKnife.inject(this);

        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMap().setOnMapLongClickListener(latLng -> {
            if (mSelectedLocationMarker != null) {
                mSelectedLocationMarker.remove();
            }
            mSelectedLocationMarker = mMapFragment.getMap().addMarker(LocationMarker.newOptions(latLng));
            performReverseGeocodingRequest(latLng);
        });
        mMapFragment.getMap().setMyLocationEnabled(true);

        mCurrentAddress = getIntent().getParcelableExtra(EXTRA_ADDRESS);
        if (mCurrentAddress != null) setTitle(mCurrentAddress.address);
    }

    private void initActionBar() {
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE |
                ActionBar.DISPLAY_SHOW_CUSTOM);

        mUseMenuView = LayoutInflater.from(this).inflate(R.layout.layout_menu_use, null);

        mUseMenuView.setOnClickListener(v -> {
            if (mCurrentAddress != null) {
                final Intent intent = new Intent();
                intent.putExtra(EXTRA_ADDRESS, mCurrentAddress);
                setResult(RESULT_OK, intent);
            }
            finish();
        });

        ActionBar.LayoutParams params = new LayoutParams(Gravity.RIGHT);
        getActionBar().setCustomView(mUseMenuView, params);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnEditorAction(R.id.edit_location)
    public boolean onEditorAction(int code) {
        if (code == EditorInfo.IME_ACTION_SEARCH) {
            performGeocoding(mLocationField.getText().toString());
            return true;
        }
        return false;
    }

    @OnClick(R.id.button_search_location)
    public void onSearchAddress() {
        performGeocoding(mLocationField.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLoading(boolean isLoading) {
        setProgressBarIndeterminateVisibility(isLoading);
        mUseMenuView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        setProgressBarIndeterminateVisibility(isLoading);
    }

    public static Intent prepareIntent(Context context, Address address) {
        Intent intent = new Intent(context, LocationActivity.class);
        intent.putExtra(EXTRA_ADDRESS, address);
        return intent;
    }

    private void performGeocoding(String text) {
        if (TextUtils.isEmpty(text)) return;
        Request request = new GeocodingRequest(text, mGeocodeListener, this);
        setLoading(true);
        mRequestQueue.add(request);
    }

    private void performReverseGeocodingRequest(LatLng latLng) {
        Request request = new ReverseGeocodeRequest(latLng, mReverseGecoodeListener, this);
        setLoading(true);
        mRequestQueue.add(request);
    }

    @Override public void onErrorResponse(VolleyError error) {
        setLoading(false);
        String message = ScUtils.getMessageFromError(getApplicationContext(), error);
        showToast(this, message);
    }
}
