package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;

import butterknife.ButterKnife;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UploaderApp.from(this).inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

}
