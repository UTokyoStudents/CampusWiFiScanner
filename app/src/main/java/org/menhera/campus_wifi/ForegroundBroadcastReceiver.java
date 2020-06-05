package org.menhera.campus_wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ForegroundBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent) {
            return;
        }

        String action = intent.getAction();
        if (null == action) {
            return;
        }

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            Log.d(ForegroundService.class.getCanonicalName(), "NETWORK_STATE_CHANGED_ACTION");
        }
        if (action.equals(WifiManager.RSSI_CHANGED_ACTION)){
            Log.d(ForegroundService.class.getCanonicalName(), "RSSI_CHANGED_ACTION");
        }
        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
            Log.d(ForegroundService.class.getCanonicalName(), "SCAN_RESULTS_AVAILABLE_ACTION");
        }

        final CampusWiFiApplication application = (CampusWiFiApplication) context.getApplicationContext();
        application.updateWifiData();
    }
}
