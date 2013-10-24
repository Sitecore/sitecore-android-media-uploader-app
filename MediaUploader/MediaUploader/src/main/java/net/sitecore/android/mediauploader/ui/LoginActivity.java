package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import net.sitecore.android.mediauploader.R;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Views;

public class LoginActivity extends Activity {

    @InjectView(R.id.edit_url) EditText mUrl;
    @InjectView(R.id.edit_login) EditText mLogin;
    @InjectView(R.id.edit_password) EditText mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Views.inject(this);
    }

    @OnClick(R.id.button_ok)
    public void testConnection() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}