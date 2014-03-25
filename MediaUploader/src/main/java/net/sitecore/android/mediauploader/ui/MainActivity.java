package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.ui.browser.BrowserActivity;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.ScRequestQueue;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String url = "http://mobiledev1ua1.dk.sitecore.net:722";
        String login = "extranet\\creatorex";
        String password = "creatorex";
        Instance instance = new Instance("test", url, login, password, "/sitecore/media library");
        UploaderPrefs.from(this).setDefaultInstance(instance);

        findViewById(R.id.button_browse).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BrowserActivity.class));
            }
        });

        findViewById(R.id.button_upload).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadActivity.class));
            }
        });

        findViewById(R.id.button_my_uploads).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
