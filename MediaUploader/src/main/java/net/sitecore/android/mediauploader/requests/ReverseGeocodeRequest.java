package net.sitecore.android.mediauploader.requests;

import java.util.ArrayList;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.google.android.gms.maps.model.LatLng;

import net.sitecore.android.mediauploader.model.Address;

public class ReverseGeocodeRequest extends BaseGeocodeRequest {

    /**
     * Creates request that perfomrs reverse geocoding.
     *
     * @param latLng        Latitude and Longitude of the target point.
     * @param listener      succes listener.
     * @param errorListener error listener
     */
    public ReverseGeocodeRequest(LatLng latLng, Listener<ArrayList<Address>> listener, ErrorListener errorListener) {
        super(GEOCODING_BASE_URL + "latlng=" + latLng.latitude + "," + latLng.longitude + "&sensor=" + true,
                listener, errorListener);
    }
}
