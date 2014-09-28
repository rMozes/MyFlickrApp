package com.android.flickr.flickr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RICHI on 2014.09.28..
 */
public class ThumbDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThumbDownloader";

    private static final int MESSAGE_WHAT = 0;
    private Map<Token, String> map = Collections.synchronizedMap(new HashMap<Token, String>());
    private Handler mHandler;
    private Handler photoHandler;
    private UpdatePhoto updatePhoto;

    public interface UpdatePhoto {
        public void updatePhoto(ImageView view, BitmapDrawable drawable);
    }

    public ThumbDownloader(Handler handler, UpdatePhoto updatePhoto) {
        super(TAG);
        photoHandler = handler;
        this.updatePhoto = updatePhoto;
    }

    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "Got an url: "+url);
        map.put(token, url);
        Message message = mHandler.obtainMessage(MESSAGE_WHAT, token);
        message.sendToTarget();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_WHAT) {
                    Token token = (Token) msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    private void handleRequest(final Token token) {
        final String url = map.get(token);
        if (url == null) {
            return;
        }
        try {
            byte[] bitmapRaw = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapRaw, 0, bitmapRaw.length);
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            Log.i(TAG, "Bitmap created");

            photoHandler.post(new Runnable() {
                @Override
                public void run() {
                    updatePhoto.updatePhoto((ImageView) token, drawable);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image!", e);
        }
    }
}
