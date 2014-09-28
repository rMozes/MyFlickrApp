package com.android.flickr.flickr;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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

        new FechItemsTask().execute();

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
            return new FlickrFetchr().fetchItems();
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
