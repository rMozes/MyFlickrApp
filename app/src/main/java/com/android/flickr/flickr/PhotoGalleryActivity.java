package com.android.flickr.flickr;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public int getLayout() {
        return R.layout.activity_photo_gallery;
    }

    @Override
    public Fragment creatFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
