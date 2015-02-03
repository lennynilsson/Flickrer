package se.bylenny.flickrer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FlickrRestRequest<T> implements Runnable {
    private static final String TAG = "ApiRequest";
    private static OkUrlFactory factory;
    private static ObjectMapper mapper;
    private static OkHttpClient client;
    private static boolean initialized = false;
    private Uri uri;
    private ResponseListener<T> listener;
    private Class<T> type;

    private OkUrlFactory getConnectionFactory() {
        if (factory == null) {
            factory = new OkUrlFactory(client);
        }
        return factory;
    }

    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }

    public static void setup(Context context) {
        if (!initialized) {
            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttpDownloader(getClient()))
                    .build();
            Picasso.setSingletonInstance(picasso);
            initialized = true;
        }
    }

    private ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    public void call(Uri uri, Class<T> type, ResponseListener<T> listener) {
        this.uri = uri;
        this.type = type;
        this.listener = listener;
        Thread thread = new Thread(this, TAG);
        thread.start();
    }

    @Override
    public void run() {
        InputStream stream = null;
        try {
            URL url = new URL(uri.toString());
            HttpURLConnection connection = getConnectionFactory().open(url);
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                stream = connection.getInputStream();
                T translation = getMapper().readValue(stream, type);
                listener.onSuccess(translation);
            } else {
                Log.d(TAG, "Got error code " + connection.getResponseCode() + " from " + url);
                listener.onFailure(connection.getResponseMessage());
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Error", e);
            listener.onFailure(e.getMessage());
        } catch (JsonParseException e) {
            Log.e(TAG, "Error in response", e);
            Scanner s = new Scanner(stream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            Log.d(TAG, response);
            listener.onFailure(e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
            listener.onFailure(e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }

    }

    public interface ResponseListener<T> {
        public void onSuccess(T response);
        public void onFailure(String error);
    }
}
