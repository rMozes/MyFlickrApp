package com.android.flickr.flickr;

/**
 * Created by RICHI on 2014.09.28..
 */
public class GalleryItem {

    private String mCaption;
    private String mId;
    private String mUrl;

    public GalleryItem() {}

    public GalleryItem(String caption, String id, String url) {
        mCaption = caption;
        mId = id;
        mUrl = url;
    }

    public static class Builder {
        private String caption;
        private String id;
        private String url;

        public Builder setCaption(String caption) {
            this.caption = caption;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public GalleryItem build() {
            return new GalleryItem(caption, id, url);
        }
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    @Override
    public String toString() {
        return mCaption;
    }
}
