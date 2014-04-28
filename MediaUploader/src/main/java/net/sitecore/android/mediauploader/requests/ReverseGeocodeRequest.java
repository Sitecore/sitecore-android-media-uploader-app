package net.sitecore.android.mediauploader.requests;

import java.util.ArrayList;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

public class ReverseGeocodeRequest extends JsonRequest<ArrayList<String>> {

    public ReverseGeocodeRequest(int method, String url, String requestBody, Listener<ArrayList<String>> listener, ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    @Override protected Response<ArrayList<String>> parseNetworkResponse(NetworkResponse networkResponse) {
        ArrayList<String> receivedAddresses = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(new String(networkResponse.data));
            if (object.getString("status").equals("OK")) {
                JSONArray results = object.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    receivedAddresses.add(results.getJSONObject(i).getString("formatted_address"));
                }
            }
        } catch (JSONException e) {
            LOGE(e);
        }
        return Response.success(receivedAddresses, null);
    }
}
