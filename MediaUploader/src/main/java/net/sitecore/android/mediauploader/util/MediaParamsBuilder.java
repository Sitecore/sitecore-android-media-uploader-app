package net.sitecore.android.mediauploader.util;

public class MediaParamsBuilder {
    private StringBuilder params;

    public MediaParamsBuilder() {
        params = new StringBuilder("?");
    }

    public MediaParamsBuilder width(int width) {
        params.append("w=").append(width).append("&");
        return this;
    }

    public MediaParamsBuilder height(int height) {
        params.append("h=").append(height).append("&");
        return this;
    }

    public MediaParamsBuilder maxWidth(int maxWidth) {
        params.append("mw=").append(maxWidth).append("&");
        return this;
    }

    public MediaParamsBuilder maxHeight(int maxHeight) {
        params.append("mh=").append(maxHeight).append("&");
        return this;
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
