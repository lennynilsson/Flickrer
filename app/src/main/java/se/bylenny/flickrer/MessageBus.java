package se.bylenny.flickrer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by Lenny Nilsson on 2015-02-03.
 */
public class MessageBus extends Bus {

    private static final MessageBus BUS = new MessageBus();
    private static final String FLICKRER_BUS = "FLICKRER_BUS";

    public static MessageBus getInstance() {
        return BUS;
    }

    private MessageBus() {
        super(FLICKRER_BUS);
        // No instances.
    }

    public static void send(final Object event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                BUS.post(event);
            }
        });
    }

    public static void subscribe(Object object) {
        BUS.register(object);
    }

    public static void unsubscribe(Object object) {
        BUS.unregister(object);
    }
}
