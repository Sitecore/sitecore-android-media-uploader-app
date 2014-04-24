package net.sitecore.android.mediauploader.ui.settings;

public enum ImageSize {
    SMALL(800, 600),
    MEDIUM(1024, 768),
    ACTUAL(0, 0);

    private int mWidth;
    private int mHeight;

    ImageSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
