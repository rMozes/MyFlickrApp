package com.android.flickr.flickr;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static final String TAG = "PhotoGalleryActivity";

    @Override
    public int getLayout() {
        return R.layout.activity_photo_gallery;
    }

    @Override
    public Fragment creatFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if(intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Search query is received: "+query);

            PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetchr.SHARED_QUERY, query)
                    .commit();
        }

        fragment.updateItems();
    }
}
