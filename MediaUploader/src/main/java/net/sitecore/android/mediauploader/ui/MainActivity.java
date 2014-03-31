package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import javax.inject.Inject;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.browser.BrowserActivity;
import net.sitecore.android.mediauploader.ui.settings.SettingsActivity;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.ui.upload.UploadsListActivity;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @Inject ScApiSession mApiSession;

    @InjectView(R.id.button_upload) Button mUploadButton;
    @InjectView(R.id.button_browse) Button mBrowseButton;
    @InjectView(R.id.button_my_uploads) Button mMyUploadsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);

        if (mApiSession == null) {
            // TODO: disable Upload and Browse
            mUploadButton.setEnabled(false);
            mBrowseButton.setEnabled(false);
            mMyUploadsButton.setEnabled(false);
        }
    }

    @OnClick(R.id.button_upload)
    public void onUploadClick() {
        startActivity(new Intent(MainActivity.this, UploadActivity.class));
    }

    @OnClick(R.id.button_browse)
    public void onBrowseClick() {
        startActivity(new Intent(MainActivity.this, BrowserActivity.class));
    }

    @OnClick(R.id.button_my_uploads)
    public void onMyUploadsClick() {
        startActivity(new Intent(MainActivity.this, UploadsListActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
