package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.ui.browser.BrowserActivity;
import net.sitecore.android.mediauploader.ui.settings.SettingsActivity;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogFragment;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogFragment.SelectMediaListener;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.ui.upload.UploadsListActivity;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity implements SelectMediaListener {

    @Inject ScApiSession mApiSession;

    @InjectView(R.id.button_upload) Button mUploadButton;
    @InjectView(R.id.button_browse) Button mBrowseButton;
    @InjectView(R.id.button_my_uploads) Button mMyUploadsButton;
    @InjectView(R.id.text_no_sites) TextView mNoInstancesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);

        ButterKnife.inject(this);
        UploaderApp.from(this).inject(this);
    }

    @Override protected void onResume() {
        super.onResume();
        if (mApiSession == null) {
            mNoInstancesView.setVisibility(View.VISIBLE);
            // TODO: disable Upload and Browse
            mUploadButton.setEnabled(false);
            mBrowseButton.setEnabled(false);
            mMyUploadsButton.setEnabled(false);
        } else {
            mNoInstancesView.setVisibility(View.GONE);
            mUploadButton.setEnabled(true);
            mBrowseButton.setEnabled(true);
            mMyUploadsButton.setEnabled(true);
        }
    }

    @OnClick(R.id.button_upload)
    public void onUploadClick() {
        new SelectMediaDialogFragment().show(getFragmentManager(), "dialog");

    }

    @Override public void onImageSelected(Uri imageUri) {
        final Intent intent = new Intent(MainActivity.this, UploadActivity.class);
        intent.setData(imageUri);
        startActivity(intent);
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
