package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.ScUtils;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

public class WelcomeActivity extends Activity implements ErrorListener, Listener<ScApiSession> {
    @InjectView(R.id.edit_url) EditText mUrl;
    @InjectView(R.id.edit_login) EditText mLogin;
    @InjectView(R.id.edit_password) EditText mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean instanceExist = Prefs.from(this).getBool(R.string.key_instance_exist, false);
        if (instanceExist) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);
        Views.inject(this);
    }

    @OnClick(R.id.button_ok)
    public void connect() {
        if (validate()) {
            String url = mUrl.getText().toString();
            String login = mLogin.getText().toString();
            String password = mPassword.getText().toString();
            ScApiSession.getSession(this, url, login, password, this, this);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, Utils.getMessageFromError(error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(ScApiSession session) {
        session.setShouldCache(true);
        UploaderApp.from(this).setSession(session);
        Listener<ItemsResponse> success = new Listener<ItemsResponse>() {
            @Override
            public void onResponse(ItemsResponse response) {
                if (response.isSuccess()) {
                    String url = mUrl.getText().toString();
                    String login = mLogin.getText().toString();
                    String password = mPassword.getText().toString();

                    Prefs prefs = Prefs.from(WelcomeActivity.this);

                    prefs.put(R.string.key_instance_exist, true);
                    prefs.put(R.string.key_instance_url, url);
                    prefs.put(R.string.key_instance_login, login);
                    prefs.put(R.string.key_instance_password, password);
                    prefs.put(R.string.key_instance_root_folder, ScUtils.PATH_MEDIA_LIBRARY);

                    saveInstance(url, login, password, ScUtils.PATH_MEDIA_LIBRARY);

                    Toast.makeText(WelcomeActivity.this, "Successfully logged in", Toast.LENGTH_LONG).show();

                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(WelcomeActivity.this, "Failed to log in", Toast.LENGTH_LONG).show();
                }
            }
        };

        RequestQueueProvider.getRequestQueue(this).add(session.getItems(success, this).build());
    }

    private void saveInstance(String url, String login, String password, String defaultRootFolder) {
        ContentValues values = new ContentValues();
        values.put(Instances.URL, url);
        values.put(Instances.LOGIN, login);
        values.put(Instances.PASSWORD, password);
        values.put(Instances.DEFAULT_FOLDER, defaultRootFolder);

        new AsyncQueryHandler(getContentResolver()){}.startInsert(0, null, Instances.CONTENT_URI, values);
    }

    boolean validate() {
        boolean valid = true;

        String url = mUrl.getText().toString();
        if (TextUtils.isEmpty(url) || !URLUtil.isValidUrl(url)) {
            mUrl.setError("Please enter valid CMS Instance url");
            valid = false;
        }

        String login = mLogin.getText().toString();
        if (TextUtils.isEmpty(login)) {
            mLogin.setError("Please enter valid login");
            valid = false;
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Please enter valid password");
            valid = false;
        }
        return valid;
    }
}