package net.sitecore.android.mediauploader.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.UploaderApp;
import net.sitecore.android.mediauploader.util.Prefs;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends Activity {

    @InjectView(R.id.radio_image_size) RadioGroup mImageSizeRadioButton;
    private Prefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UploaderApp.from(this).inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        mPrefs = Prefs.from(this);

        ImageSize imageSize = ImageSize.valueOf(mPrefs.getString(R.string.key_current_image_size,
                ImageSize.ACTUAL.name()));
        switch (imageSize) {
            case SMALL:
                mImageSizeRadioButton.check(R.id.radio_image_small);
                break;
            case MEDIUM:
                mImageSizeRadioButton.check(R.id.radio_image_medium);
                break;
            case ACTUAL:
                mImageSizeRadioButton.check(R.id.radio_image_actual);
                break;
        }

        mImageSizeRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                ImageSize size = ImageSize.ACTUAL;
                switch (checkedId) {
                    case R.id.radio_image_small:
                        size = ImageSize.SMALL;
                        break;
                    case R.id.radio_image_medium:
                        size = ImageSize.MEDIUM;
                        break;
                    case R.id.radio_image_actual:
                        size = ImageSize.ACTUAL;
                        break;
                }
                mPrefs.put(R.string.key_current_image_size, size.name());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

}
