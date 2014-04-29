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

public class GeocodeRequest extends Request<ArrayList<Address>> {
    private Listener<ArrayList<Address>> mSuccessListener;

    public GeocodeRequest(int method, String url, Listener<ArrayList<Address>> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
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

                    receivedAddresses.add(new Address(addressLine, new LatLng(latitude, longitude)));
                }
            }
        } catch (JSONException e) {
            LOGE(e);
        }
        return Response.success(receivedAddresses, null);
    }

    @Override protected void deliverResponse(ArrayList<Address> addresses) {
        mSuccessListener.onResponse(addresses);
    }
}
