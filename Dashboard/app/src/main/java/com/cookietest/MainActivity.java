package com.cookietest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.HashMap;
import java.util.LinkedList;

import com.cookietest.csv.Data;
import com.cookietest.retrofit.CsvConverter;
import com.cookietest.retrofit.DashBoardApi;
import com.cookietest.retrofit.StringConverter;
import com.shinobicontrols.charts.ChartFragment;
import com.shinobicontrols.charts.DataAdapter;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.LineSeries;
import com.shinobicontrols.charts.NumberAxis;
import com.shinobicontrols.charts.ShinobiChart;
import com.shinobicontrols.charts.SimpleDataAdapter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class MainActivity extends Activity {
    public static final String CMS_URL = "http://mobiledev1ua1.dk.sitecore.net:72/";

    private Button mLoginButton;
    private Button mGetChartDataButton;

    private Tokens mTokens;
    private CookieManager manager;
    private DashBoardApi mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        ChartFragment chartFragment = (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        ShinobiChart shinobiChart = chartFragment.getShinobiChart();

        shinobiChart.setLicenseKey("QThA3HY38ZfhroFMjAxNDAxMTZpbmZvQHNoaW5vYmljb250cm9scy5jb20=Easwmya/Dk5cPygZjOSiCgfKyb6Jjmu+nUgSP9AeluApqqMPI1WaMI3w3HA5fTgwKu9l24UQYO7HdgcfKbGe/VK8EMCa/e6+ov1wvgomCFO1yYtHN7M7htXGOkWXInfVDGZL9FgmxvhrmWTzd8ZIRRJAmC1M=BQxSUisl3BaWf/7myRmmlIjRnMU2cA7q+/03ZX9wdj30RzapYANf51ee3Pi8m2rVW6aD7t6Hi4Qy5vv9xpaQYXF5T7XzsafhzS3hbBokp36BoJZg8IrceBj742nQajYyV7trx5GIw9jy/V6r0bvctKYwTim7Kzq+YPWGMtqtQoU=PFJTQUtleVZhbHVlPjxNb2R1bHVzPnh6YlRrc2dYWWJvQUh5VGR6dkNzQXUrUVAxQnM5b2VrZUxxZVdacnRFbUx3OHZlWStBK3pteXg4NGpJbFkzT2hGdlNYbHZDSjlKVGZQTTF4S2ZweWZBVXBGeXgxRnVBMThOcDNETUxXR1JJbTJ6WXA3a1YyMEdYZGU3RnJyTHZjdGhIbW1BZ21PTTdwMFBsNWlSKzNVMDg5M1N4b2hCZlJ5RHdEeE9vdDNlMD08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+");

        mLoginButton = (Button) findViewById(R.id.button_login);
        mGetChartDataButton = (Button) findViewById(R.id.button_get_data);

        mGetChartDataButton.setOnClickListener(mListener);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });


        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(CMS_URL)
                .setClient(new OkClient())
                .setConverter(new StringConverter())
                .build();

        mApi = restAdapter.create(DashBoardApi.class);
    }

    private Activity getActivity() {
        return this;
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Callback<LinkedList<Data>> listener = new BasicCallbackHandler<LinkedList<Data>>() {
                @Override
                public void success(LinkedList<Data> s, Response response) {
                    Toast.makeText(getActivity(), "result ok", Toast.LENGTH_SHORT).show();
                    initChart(s);
                }
            };

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setServer(CMS_URL)
                    .setConverter(new CsvConverter())
                    .build();

            DashBoardApi api = restAdapter.create(DashBoardApi.class);
            api.getTrafficSearchKeywords(mTokens.toString(), listener);
        }
    };

    private void initChart(LinkedList<Data> s) {
        ChartFragment chartFragment = (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        ShinobiChart shinobiChart = chartFragment.getShinobiChart();

        //shinobiChart.setLicenseKey("QThA3HY38ZfhroFMjAxNDAxMTZpbmZvQHNoaW5vYmljb250cm9scy5jb20=Easwmya/Dk5cPygZjOSiCgfKyb6Jjmu+nUgSP9AeluApqqMPI1WaMI3w3HA5fTgwKu9l24UQYO7HdgcfKbGe/VK8EMCa/e6+ov1wvgomCFO1yYtHN7M7htXGOkWXInfVDGZL9FgmxvhrmWTzd8ZIRRJAmC1M=BQxSUisl3BaWf/7myRmmlIjRnMU2cA7q+/03ZX9wdj30RzapYANf51ee3Pi8m2rVW6aD7t6Hi4Qy5vv9xpaQYXF5T7XzsafhzS3hbBokp36BoJZg8IrceBj742nQajYyV7trx5GIw9jy/V6r0bvctKYwTim7Kzq+YPWGMtqtQoU=PFJTQUtleVZhbHVlPjxNb2R1bHVzPnh6YlRrc2dYWWJvQUh5VGR6dkNzQXUrUVAxQnM5b2VrZUxxZVdacnRFbUx3OHZlWStBK3pteXg4NGpJbFkzT2hGdlNYbHZDSjlKVGZQTTF4S2ZweWZBVXBGeXgxRnVBMThOcDNETUxXR1JJbTJ6WXA3a1YyMEdYZGU3RnJyTHZjdGhIbW1BZ21PTTdwMFBsNWlSKzNVMDg5M1N4b2hCZlJ5RHdEeE9vdDNlMD08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+");

        shinobiChart.addXAxis(new NumberAxis());
        shinobiChart.addYAxis(new NumberAxis());

        DataAdapter<Integer, Double> adapter = new SimpleDataAdapter<Integer, Double>();

        for (int i = 0; i < 10; i++) {
            adapter.add(new DataPoint<Integer, Double>(i, i*2.5));
        }

        LineSeries series = new LineSeries();
        series.setDataAdapter(adapter);
        series.getStyle().setLineColor(Color.RED);
        series.getStyle().setLineColorBelowBaseline(Color.RED);
        shinobiChart.addSeries(series);
    }

    private void login() {
        Callback<String> listener = new BasicCallbackHandler<String>() {
            @Override
            public void success(String html, Response response) {
                sendLoginRequest(html);
            }
        };
        mApi.getMainPage(listener);
    }

    private void sendLoginRequest(String html) {
        final HashMap<String, String> data = Utils.parseHtml(html);
        data.put("Login$UserName", "admin");
        data.put("Login$Password", "b");
        data.put("Language", "");

        Callback<String> listener = new BasicCallbackHandler<String>() {
            @Override
            public void success(String html, Response response) {
                CookieStore cookieJar = manager.getCookieStore();
                mTokens = new Tokens(cookieJar.getCookies());

                if (!html.contains("Your login attempt was not successful.")) {
                    Toast.makeText(getActivity(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Login with unsuccessfully", Toast.LENGTH_LONG).show();
                }
            }
        };

        String postData = null;
        try {
            postData = Utils.convertMapToPostData(data);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getActivity(), "Error in constructing postdata", Toast.LENGTH_LONG).show();
        }
        mApi.performLogin(postData, listener);
    }

    abstract class BasicCallbackHandler<T> implements Callback<T> {
        @Override
        abstract public void success(T t, Response response);

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(getActivity(), "Error : " + error.getCause(), Toast.LENGTH_LONG).show();
        }
    }
}
