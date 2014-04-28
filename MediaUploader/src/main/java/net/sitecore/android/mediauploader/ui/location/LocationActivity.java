package net.sitecore.android.mediauploader.ui.location;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.sitecore.android.mediauploader.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LocationActivity extends Activity {
    MapFragment mMapFragment;

    @InjectView(R.id.edit_location) EditText mLocationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_location);

        ButterKnife.inject(this);

        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMap().setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override public void onMapLongClick(LatLng latLng) {
            }
        });
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
}
