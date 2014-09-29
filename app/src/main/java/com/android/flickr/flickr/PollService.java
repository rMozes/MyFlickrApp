package com.android.flickr.flickr;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by RICHI on 2014.09.29..
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    private static final int POLL_TIME = 1000 * 60 * 5;

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Received an intent: " + intent);

        if (!checkInternet()) {
            return;
        }
        makeLog("Network is available!");

        String id = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(FlickrFetchr.PREF_RESULT_ID, null);

        String query = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(FlickrFetchr.SHARED_QUERY, null);

        ArrayList<GalleryItem> items;
        if (query != null) {
            items = new FlickrFetchr().search(query);
        } else {
            items = new FlickrFetchr().fetchItems();
        }

        if (items.size() == 0) {
            makeLog("Do not have new items!");
            return;
        }

        String resultId = items.get(0).getId();
        if (!(resultId.equals(id))) {
            makeLog("Got a new result: " + resultId);

            PendingIntent pi = PendingIntent
                    .getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(getString(R.string.new_pictures_title))
                    .setContentText(getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, notification);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetchr.PREF_RESULT_ID, resultId)
                    .commit();
        }

    }

    public static void setAlarm(Context context, boolean on) {
        PendingIntent pi = getPendingIntent(context, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (on) {
            manager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_TIME, pi );
        } else {
            manager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceOn(Context context) {
        PendingIntent pi = getPendingIntent(context, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private static PendingIntent getPendingIntent(Context context, int flag) {
        Intent intent = new Intent(context, PollService.class);

        return PendingIntent.getService(context, 0, intent, flag);
    }

    private void makeLog(String message) {
        Log.i(TAG, message);
    }

    private boolean checkInternet() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean check = manager.getBackgroundDataSetting() &&
                (manager.getActiveNetworkInfo() != null);

        return check;
    }
}
