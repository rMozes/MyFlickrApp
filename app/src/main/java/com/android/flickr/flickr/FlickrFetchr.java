package com.android.flickr.flickr;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by RICHI on 2014.09.28..
 */
public class FlickrFetchr {

    public static final String TAG = "FlickrFetchr";

    private static final String ENDPOINT            = "https://api.flickr.com/services/rest/";
    private static final String API_KEY             = "72655d5cb0d493a295533b693e07dee1";
    private static final String METHOD_GET_RECENT   = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS        = "extras";

    private static final String EXTRA_SMALL_URL     = "url_s";

    private static final String XML_PHOTO           = "photo";

    public ArrayList<GalleryItem> fetchItems() {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        try {
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .build()
                    .toString();
            String xmlString = getUrls(url);
            Log.i(TAG, "Received xml: "+xmlString);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlString));
            parseItems(items, xmlPullParser);

        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Failed to parse xml", e);
        }

        return items;
    }

    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws IOException,
            XmlPullParserException {
        int type = parser.next();

        while (type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.END_TAG && XML_PHOTO.equals(parser.getName())) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem.Builder()
                        .setCaption(caption)
                        .setId(id)
                        .setUrl(smallUrl)
                        .build();
                items.add(item);
            }

            type = parser.next();
        }
    }

    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = connection.getInputStream();

        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }
        //number of bytes, what you will be reading
        int bytesRead = 0;
        //piece of byteArray, what you will be reading
        byte[] buffer = new byte[1024];

        /** @in.read()   - this method will be reading the next
          *                1024 bytes in the buffer
          * @bytesRead   - how many bytes was read
          * @out.write() - this method will be appending the buffer to
          *                byteArrayOutputStream
          **/
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        connection.disconnect();

        return out.toByteArray();
    }

    public String getUrls(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}
