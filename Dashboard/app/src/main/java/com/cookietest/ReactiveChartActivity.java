package com.cookietest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.shinobicontrols.charts.ChartFragment;
import com.shinobicontrols.charts.DataAdapter;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.DateTimeAxis;
import com.shinobicontrols.charts.LineSeries;
import com.shinobicontrols.charts.NumberAxis;
import com.shinobicontrols.charts.ShinobiChart;
import com.shinobicontrols.charts.SimpleDataAdapter;

import rx.Observable;
import rx.android.concurrency.AndroidSchedulers;
import rx.util.functions.Action1;

public class ReactiveChartActivity extends Activity {

    private ShinobiChart mChart;
    private DataAdapter<Date, Integer> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactive_chart);

        ChartFragment chartFragment = (ChartFragment) getFragmentManager().findFragmentById(R.id.chart);
        mChart = chartFragment.getShinobiChart();
        mChart.setLicenseKey(Config.SHINOBI_LICENCE_KEY);

        DateTimeAxis dateTimeAxis = new DateTimeAxis();
        dateTimeAxis.enableGesturePanning(true);
        dateTimeAxis.enableGestureZooming(true);
        dateTimeAxis.enableMomentumPanning(true);
        dateTimeAxis.enableMomentumZooming(true);

        mChart.addXAxis(dateTimeAxis);
        mChart.addYAxis(new NumberAxis());

        mAdapter = new SimpleDataAdapter<Date, Integer>();

//        for (int i = 0; i < 10; i++) {
//            mAdapter.add(new DataPoint<Integer, Double>(i, i * i * 2.5));
//        }

        mAdapter.add(new DataPoint<Date, Integer>(new Date(), 5));

        LineSeries series = new LineSeries();
        series.setDataAdapter(mAdapter);
        series.getStyle().setLineColor(Color.RED);
        series.getStyle().setLineColorBelowBaseline(Color.RED);

        mChart.addSeries(series);

        Integer[] data = new Integer[]{
                1, 3, 3, 8, 10
        };

        Observable.interval(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mAdapter.add(new DataPoint<Date, Integer>(new Date(), new Random().nextInt(10)));
                    }
                });

                        /*new Action1<Integer>() {
                    @Override
                    public void call(Integer point) {
                        mAdapter.add(new DataPoint<Date, Integer>(new Date(), point));
                        mChart.redrawChart();
                        Log.i("111", "" + point);
                    }
                });
                */
    }

}
