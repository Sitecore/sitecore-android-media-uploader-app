package com.cookietest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class MainActivity extends Activity {
    public static final String CMS_URL = "http://mobiledev1ua1.dk.sitecore.net:72/";

    private Button mLoginButton;
    private Button mGetChartDataButton;
    private TextView mResult;

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

        mLoginButton = (Button) findViewById(R.id.button_login);
        mGetChartDataButton = (Button) findViewById(R.id.button_get_data);
        mResult = (TextView) findViewById(R.id.text);

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
