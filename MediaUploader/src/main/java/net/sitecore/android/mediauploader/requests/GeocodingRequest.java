package net.sitecore.android.mediauploader.requests;

import java.util.ArrayList;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import net.sitecore.android.mediauploader.model.Address;

public class GeocodingRequest extends BaseGeocodeRequest {

    /**
     * Creates request that perfomrs geocoding.
     *
     * @param address       address to geocode.
     * @param listener      succes listener.
     * @param errorListener error listener
     */
    public GeocodingRequest(String address, Listener<ArrayList<Address>> listener, ErrorListener errorListener) {
        super(GEOCODING_BASE_URL + "address=" + address + "&sensor=" + true, listener, errorListener);
    }
}
