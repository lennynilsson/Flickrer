package se.bylenny.flickrer;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Lenny Nilsson on 2015-01-29.
 */
public class FlickrResultReceiver extends ResultReceiver {

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    private Receiver receiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler The handler
     */
    public FlickrResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver activity) {
        this.receiver = activity;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }
}
