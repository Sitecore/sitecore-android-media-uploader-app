package net.sitecore.android.mediauploader.ui.browser;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import net.sitecore.android.mediauploader.UploaderApp;

public class PreviewActivity extends Activity {
    public static final String IMAGE_URL_KEY = "image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra(IMAGE_URL_KEY);
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "You have to specify image url for this activity", Toast.LENGTH_LONG).show();
            finish();
        }

        NetworkImageView imageView = new NetworkImageView(this);
        imageView.setImageUrl(url, UploaderApp.from(this).getImageLoader());

        setContentView(imageView);
    }
}
