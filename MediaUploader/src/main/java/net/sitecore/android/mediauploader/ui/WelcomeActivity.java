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
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.model.ItemsResponse;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static net.sitecore.android.mediauploader.util.Utils.showToast;

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
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button_ok)
    public void connect() {
        if (validate()) {
            String url = mUrl.getText().toString();
            String login = mLogin.getText().toString();
            String password = mPassword.getText().toString();
            ScApiSessionFactory.getSession(RequestQueueProvider.getRequestQueue(this),
                    url, login, password, this, this);
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
                    String name = mUrl.getText().toString();
                    String url = mUrl.getText().toString();
                    String login = mLogin.getText().toString();
                    String password = mPassword.getText().toString();

                    Utils.setDefaultInstance(WelcomeActivity.this, name, url, login, password, ScUtils.PATH_MEDIA_LIBRARY);
                    Prefs.from(WelcomeActivity.this).put(R.string.key_instance_exist, true);

                    saveInstance(name, url, login, password, ScUtils.PATH_MEDIA_LIBRARY);

                    showToast(WelcomeActivity.this, R.string.toast_logged_in);

                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                } else {
                    showToast(WelcomeActivity.this, R.string.toast_login_failed);
                }
            }
        };

        RequestQueueProvider.getRequestQueue(this).add(session.getItems(success, this).build());
    }

    private void saveInstance(String name, String url, String login, String password, String defaultRootFolder) {
        ContentValues values = new ContentValues();
        values.put(Instances.NAME, name);
        values.put(Instances.URL, url);
        values.put(Instances.LOGIN, login);
        values.put(Instances.PASSWORD, password);
        values.put(Instances.ROOT_FOLDER, defaultRootFolder);

        new AsyncQueryHandler(getContentResolver()) {
        }.startInsert(0, null, Instances.CONTENT_URI, values);
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