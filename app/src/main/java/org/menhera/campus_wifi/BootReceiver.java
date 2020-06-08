package org.menhera.campus_wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    CampusWiFiApplication application;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent){
            return;
        }


        String action = intent.getAction();
        if (null == action) {
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            application = (CampusWiFiApplication) context.getApplicationContext();
            boolean started = application.isServiceStarted();
            Log.d(BootReceiver.class.getSimpleName(), "Service started on boot: " + started);
        }
    }
}
