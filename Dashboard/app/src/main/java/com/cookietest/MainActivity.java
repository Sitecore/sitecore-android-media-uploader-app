package com.cookietest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.LinkedList;

import com.cookietest.csv.DataRow;
import com.cookietest.retrofit.ApiFactory;
import com.cookietest.retrofit.BasicCallbackHandler;
import com.cookietest.retrofit.DashBoardApi;
import com.shinobicontrols.charts.ChartFragment;
import com.shinobicontrols.charts.DataAdapter;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.LineSeries;
import com.shinobicontrols.charts.NumberAxis;
import com.shinobicontrols.charts.ShinobiChart;
import com.shinobicontrols.charts.SimpleDataAdapter;

import retrofit.Callback;
import retrofit.client.Response;

public class MainActivity extends Activity {

    private Button mLoginButton;
    private Button mGetChartDataButton;

    private Cookies mTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        ChartFragment chartFragment = (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        ShinobiChart shinobiChart = chartFragment.getShinobiChart();

        shinobiChart.setLicenseKey(Config.SHINOBI_LICENCE_KEY);

        mLoginButton = (Button) findViewById(R.id.button_login);
        mGetChartDataButton = (Button) findViewById(R.id.button_get_data);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mGetChartDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getChartData();
            }
        });
    }

    private Activity getActivity() {
        return this;
    }

    private void login() {
        new LoginApiHelper().performLogin("admin1111", "b", new BasicCallbackHandler<Cookies>(getActivity()) {
            @Override
            public void success(Cookies tokens, Response response) {
                if (tokens != null) {
                    mTokens = tokens;
                    Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getChartData() {
        DashBoardApi api = ApiFactory.newDashboardApi(mTokens);
        api.getTrafficSearchKeywords(mChartDataCallback);
    }

    private final Callback<LinkedList<DataRow>> mChartDataCallback = new BasicCallbackHandler<LinkedList<DataRow>>(getActivity()) {
        @Override
        public void success(LinkedList<DataRow> s, Response response) {
            Toast.makeText(getActivity(), "result ok", Toast.LENGTH_SHORT).show();
            initChart(s);
        }
    };

    private void initChart(LinkedList<DataRow> s) {
        ChartFragment chartFragment = (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        ShinobiChart shinobiChart = chartFragment.getShinobiChart();

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

}
