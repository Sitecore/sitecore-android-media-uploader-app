package com.cookietest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.LinkedList;

import com.cookietest.csv.Data;
import com.cookietest.retrofit.BasicCallbackHandler;
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
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class MainActivity extends Activity {
    private Button mLoginButton;
    private Button mGetChartDataButton;

    private Tokens mTokens;
    private DashBoardApi mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
                .setServer(Constants.CMS_URL)
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
            Callback<LinkedList<Data>> listener = new BasicCallbackHandler<LinkedList<Data>>(getActivity()) {
                @Override
                public void success(LinkedList<Data> s, Response response) {
                    Toast.makeText(getActivity(), "result ok", Toast.LENGTH_SHORT).show();
                    initChart(s);
                }
            };

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setServer(Constants.CMS_URL)
                    .setConverter(new CsvConverter())
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("Cookie", mTokens.toString());
                        }
                    })
                    .build();

            DashBoardApi api = restAdapter.create(DashBoardApi.class);
            api.getTrafficSearchKeywords(listener);
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
        new LoginUtils().performLogin("admin", "b", new BasicCallbackHandler<Tokens>(getActivity()) {
            @Override
            public void success(Tokens tokens, Response response) {
                if (tokens != null) {
                    mTokens = tokens;
                    Toast.makeText(getActivity(), "Login succesfull", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
