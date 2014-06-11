package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.content.Intent;
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
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper;
import net.sitecore.android.mediauploader.ui.upload.SelectMediaDialogHelper.SelectMediaListener;
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
    @InjectView(R.id.text_no_sites) TextView mNoInstancesView;

    private SelectMediaDialogHelper mMediaDialogHelper;
    private SelectMediaListener mSelectMediaListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);

        ButterKnife.inject(this);

        mMediaDialogHelper = new SelectMediaDialogHelper(this, mSelectMediaListener);
        mSelectMediaListener = new MainActivitySelectMediaListener(this);
    }

    @Override protected void onResume() {
        super.onResume();

        UploaderApp.from(this).inject(this);

        if (mApiSession == null) {
            mNoInstancesView.setVisibility(View.VISIBLE);

            mUploadButton.setEnabled(false);
            mBrowseButton.setEnabled(false);
        } else {
            mNoInstancesView.setVisibility(View.GONE);
            mUploadButton.setEnabled(true);
            mBrowseButton.setEnabled(true);
        }
    }

    @OnClick(R.id.button_upload)
    public void onUploadClick() {
        mMediaDialogHelper.showDialog();
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

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            mMediaDialogHelper.onActivityResult(requestCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
