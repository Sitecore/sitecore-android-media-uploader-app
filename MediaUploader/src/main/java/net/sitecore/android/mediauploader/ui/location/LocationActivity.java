package net.sitecore.android.mediauploader.ui.location;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.requests.ReverseGeocodeRequest;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.sdk.api.ScRequestQueue;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class LocationActivity extends Activity implements ErrorListener, Listener<ArrayList<String>> {
    public final static String EXTRA_ADDRESS_LINE = "address";
    public final static String EXTRA_LOCATION = "location";

    private final static String GEOCODING_BASE_URL = "http://maps.googleapis.com/maps/api/geocode/json?";

    @InjectView(R.id.edit_location) EditText mLocationText;
    @InjectView(R.id.button_search_location) Button mSearchAddressButton;

    @Inject ScRequestQueue mRequestQueue;

    private LatLng mSelectedLatLng;
    private String mAddressString;
    private MapFragment mMapFragment;
    private View mUseMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        initActionBar();

        UploaderApp.from(this).inject(this);
        setContentView(R.layout.activity_location);
        ButterKnife.inject(this);

        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMap().setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override public void onMapLongClick(LatLng latLng) {
                mSelectedLatLng = latLng;
                performReverseGeocodingRequest(latLng);
            }
        });
    }

    private void initActionBar() {
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE |
                ActionBar.DISPLAY_SHOW_CUSTOM);

        mUseMenuView = LayoutInflater.from(this).inflate(R.layout.layout_menu_use, null);

        mUseMenuView.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (mSelectedLatLng != null) {
                    setResult(RESULT_OK, prepareData());
                }
                finish();
            }
        });

        ActionBar.LayoutParams params = new LayoutParams(Gravity.RIGHT);
        getActionBar().setCustomView(mUseMenuView, params);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnEditorAction(R.id.edit_location)
    public boolean onEditorAction(int code) {
        if (code == EditorInfo.IME_ACTION_SEARCH) {
            performGeocoding(mLocationText.getText().toString());
            return true;
        }
        return false;
    }

    @OnClick(R.id.button_search_location)
    public void onSearchAddress() {
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

    private Intent prepareData() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ADDRESS_LINE, mAddressString);
        intent.putExtra(EXTRA_LOCATION, mSelectedLatLng);
        return intent;
    }

    private void performGeocoding(String text) {
    }

    private void performReverseGeocodingRequest(LatLng latLng) {
        String url = GEOCODING_BASE_URL + "latlng=" + latLng.latitude + "," + latLng.longitude + "&sensor=" + true;
        Request request = new ReverseGeocodeRequest(Method.GET, url, null, this, this);

        setLoading(true);
        mRequestQueue.add(request);
    }

    @Override public void onErrorResponse(VolleyError error) {
        setLoading(false);
        Toast.makeText(LocationActivity.this, ScUtils.getMessageFromError(error), Toast.LENGTH_LONG)
                .show();
    }

    @Override public void onResponse(ArrayList<String> addresses) {
        setLoading(false);
        if (addresses.size() != 0) {
            mAddressString = addresses.get(0);
            setTitle(mAddressString);
        }
    }
}
