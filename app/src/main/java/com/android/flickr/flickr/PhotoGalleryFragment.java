package com.android.flickr.flickr;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * Created by RICHI on 2014.09.28..
 */
public class PhotoGalleryFragment extends Fragment implements ThumbDownloader.UpdatePhoto {

    private static final String TAG = "PhotoGalleryFragment";

    private ThumbDownloader<ImageView> downloader;
    private GridView mGridView;
    private ArrayList<GalleryItem> mItems;
    private Handler mHandler;

    public static PhotoGalleryFragment newInstance() {
        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void updatePhoto(ImageView view, BitmapDrawable drawable) {
        view.setImageDrawable(drawable);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

        mHandler = new Handler();
        downloader = new ThumbDownloader<ImageView>(mHandler, this);
        downloader.start();
        downloader.getLooper();
        Log.i(TAG, "Downloader started!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) view.findViewById(R.id.grid_view);

        setupAdapter();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloader.clear();
        downloader.quit();
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo_gallery, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            MenuItem item = menu.findItem(R.id.search_button);
            SearchView searchView = (SearchView) item.getActionView();
            SearchManager manager = (SearchManager) getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            SearchableInfo searchableInfo = manager
                    .getSearchableInfo(getActivity().getComponentName());
            searchView.setSearchableInfo(searchableInfo);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_button:
                getActivity().onSearchRequested();
                return true;
            case R.id.clear_button:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.SHARED_QUERY, null)
                        .commit();
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateItems() {
        new FechItemsTask().execute();
    }

    private void setupAdapter() {
        if (getActivity() == null || mGridView == null) {
            return;
        }
        if(mItems != null) {
            mGridView.setAdapter(new MyAdapter(mItems, getActivity()));
        } else {
            mGridView.setAdapter(null);
        }
    }

    private class FechItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... voids) {
            String query = PreferenceManager
                    .getDefaultSharedPreferences(PhotoGalleryFragment.this.getActivity())
                    .getString(FlickrFetchr.SHARED_QUERY, null);
            if (query != null) {
                return new FlickrFetchr().search(query);
            } else {
                return new FlickrFetchr().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            super.onPostExecute(items);
            mItems = items;
            setupAdapter();
        }
    }

    private class MyAdapter extends ArrayAdapter<GalleryItem> {
        public MyAdapter(ArrayList<GalleryItem> items, Context context) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.gallery_item, parent, false);
            }

            ImageView view = (ImageView) convertView.findViewById(R.id.gallery_item_image);
            view.setImageResource(android.R.drawable.gallery_thumb);

            GalleryItem item = getItem(position);
            downloader.queueThumbnail(view, item.getUrl());

            return convertView;
        }
    }
}
