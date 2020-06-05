package org.menhera.campus_wifi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "channel_background";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = buildNotification(getString(R.string.notification_background_title), "");

        startForeground(NOTIFICATION_ID, notification);

        BroadcastReceiver receiver = new ForegroundBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(receiver, filter);

        final CampusWiFiApplication application = (CampusWiFiApplication) getApplication();
        application.getConnectionState().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals(CampusWiFiApplication.STATE_CONNECTED)) {
                    String info = "Frequency: " + application.getLastFrequency() + " MHz\nBSSID: " + application.getLastBssid()
                            + "\nSignal: " + application.getLastRssi() + " dBm\nLink speed: " + application.getLastLinkSpeed() + " Mbps";
                    updateNotification(application.getLastSsid(), info);
                } else if (s.equals(CampusWiFiApplication.STATE_AVAILABLE)) {
                    updateNotification(getString(R.string.notification_background_scanning), "Available APs: " + application.getLastScanResults().size());
                } else {
                    updateNotification(getString(R.string.notification_background_disconnected), "Unknown APs: " + application.getAllLastScanResults().size());
                }

            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    Notification buildNotification(String title, String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sync_black_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build();
    }

    void updateNotification(Notification notification) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void updateNotification(String title, String text) {
        Log.d(ForegroundService.class.getSimpleName(), "Trying to notify: [" + title + "] " + text);
        Notification notification = buildNotification(title, text);
        updateNotification(notification);
    }

    public void updateNotification(String title) {
        updateNotification(title, "");
    }
}
