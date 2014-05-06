package net.sitecore.android.mediauploader.requests;

import java.util.ArrayList;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.google.android.gms.maps.model.LatLng;

import net.sitecore.android.mediauploader.model.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class BaseGeocodeRequest extends Request<ArrayList<Address>> {
    protected final static String GEOCODING_BASE_URL = "http://maps.googleapis.com/maps/api/geocode/json?";
    private Listener<ArrayList<Address>> mSuccessListener;

    public BaseGeocodeRequest(String url, Listener<ArrayList<Address>> listener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mSuccessListener = listener;
    }

    @Override protected Response<ArrayList<Address>> parseNetworkResponse(NetworkResponse networkResponse) {
        ArrayList<Address> receivedAddresses = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(new String(networkResponse.data));
            if (object.getString("status").equals("OK")) {
                JSONArray results = object.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject element = results.getJSONObject(i);

                    String addressLine = element.getString("formatted_address");
                    double latitude = element.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double longitude = element.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                    String countryCode = null;
                    String zipCode = null;

                    JSONArray addressComponents = element.getJSONArray("address_components");
                    for (int j = 0; j < addressComponents.length(); j++) {
                        JSONObject adddressElement = addressComponents.getJSONObject(j);
                        ArrayList<String> types = getTypesList(adddressElement);
                        if (types.contains("country")) countryCode = adddressElement.getString("short_name");
                        if (types.contains("postal_code")) zipCode = adddressElement.getString("short_name");
                    }

                    receivedAddresses.add(new Address(addressLine, countryCode, zipCode,
                            new LatLng(latitude, longitude)));
                }
            }
        } catch (JSONException e) {
            LOGE(e);
        }
        return Response.success(receivedAddresses, null);
    }

    private ArrayList<String> getTypesList(JSONObject component) throws JSONException {
        ArrayList<String> list = new ArrayList<>();

        JSONArray typesArray = component.optJSONArray("types");
        if (typesArray != null) {
            for (int i = 0; i < typesArray.length(); i++) {
                list.add(typesArray.getString(i));
            }
        } else {
            list.add(component.getString("types"));
        }
        return list;
    }

    @Override protected void deliverResponse(ArrayList<Address> addresses) {
        mSuccessListener.onResponse(addresses);
    }
}
