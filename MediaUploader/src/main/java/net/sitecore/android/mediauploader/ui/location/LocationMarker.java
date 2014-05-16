package net.sitecore.android.mediauploader.ui.location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.sitecore.android.mediauploader.R;

public class LocationMarker {

    public static MarkerOptions newOptions(LatLng location) {
        return new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_selected_location));
    }

}
