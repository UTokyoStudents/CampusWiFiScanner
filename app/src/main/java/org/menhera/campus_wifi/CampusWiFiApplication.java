package org.menhera.campus_wifi;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class CampusWiFiApplication extends Application {
    public static final String STATE_CONNECTED = "connected";
    public static final String STATE_AVAILABLE = "available";
    public static final String STATE_UNAVAILABLE = "unavailable";

    private boolean serviceStarted = false;

    private List<String> previousResultStrings;

    MutableLiveData<List<ScanResult>> results;
    MutableLiveData<List<ScanResult>> allResults;
    MutableLiveData<String> connected;
    MutableLiveData<Location> location;

    String lastSsid = "";
    String lastBssid = "";
    int lastRssi = 0;
    int lastFrequency = 0;
    int lastLinkSpeed = 0;

    ConnectivityManager connectivityManager;
    WifiManager wifiManager;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onCreate() {
        super.onCreate();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        results = new MutableLiveData<>();
        connected = new MutableLiveData<>();
        allResults = new MutableLiveData<>();
        location = new MutableLiveData<>();

        updateWifiData();

        Intent serviceIntent = new Intent(this, ForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location aLocation) {
                location.setValue(aLocation);
            }
        });

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000L);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                location.setValue(locationResult.getLastLocation());
            }
        }, getMainLooper());

        serviceStarted = true;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    public boolean isForegroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }

    public String getLastSsid() {
        return lastSsid;
    }

    public String getLastBssid() {
        return lastBssid;
    }

    public int getLastFrequency() {
        return lastFrequency;
    }

    public int getLastRssi() {
        return lastRssi;
    }

    public int getLastLinkSpeed() {
        return lastLinkSpeed;
    }

    public void updateWifiData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(CampusWiFiApplication.class.getSimpleName(), "Location not granted, skipping Wi-Fi scan");
            return;
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();

        String ssid;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (networkInfo == null) {
            ssid = null;
        } else if (wifiInfo == null) {
            ssid = null;
        } else if (!networkInfo.isConnected()) {
            ssid = null;
        } else {
            ssid = wifiInfo.getSSID().replaceAll("^\"|\"$", "");
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String primarySsid = sharedPreferences.getString("wifi_ssid_main", "UTokyo-WiFi");
        String secondarySsid = sharedPreferences.getString("wifi_ssid_secondary", "eduroam");

        boolean apFound = false;
        List<ScanResult> filteredResults = new ArrayList<>();

        List<String> allStrings = new ArrayList<>();

        for (ScanResult result: scanResults) {
            String scanedSsid = result.SSID;
//            Log.d(CampusWiFiApplication.class.getSimpleName(), "Scanned SSID: " + scanedSsid);
//            Log.d(CampusWiFiApplication.class.getSimpleName(), "Result: " + result);
            allStrings.add(result.toString());

            if (scanedSsid.equals(primarySsid) || scanedSsid.equals(secondarySsid)) {
                Log.d(CampusWiFiApplication.class.getSimpleName(), "AP_FOUND");
                filteredResults.add(result);
                allStrings.add(result.toString());
            }
        }

        apFound = filteredResults.size() > 0;

        Log.d(CampusWiFiApplication.class.getSimpleName(), "Last connected SSID: " + ssid);
        if (ssid != null && (ssid.equals(primarySsid) || ssid.equals(secondarySsid))) {
            lastSsid = ssid;
            lastRssi = wifiInfo.getRssi();
            lastBssid = wifiInfo.getBSSID();
            lastFrequency = wifiInfo.getFrequency();
            lastLinkSpeed = wifiInfo.getLinkSpeed();
            connected.setValue(STATE_CONNECTED);
        } else if (apFound) {
            connected.setValue(STATE_AVAILABLE);
        } else {
            connected.setValue(STATE_UNAVAILABLE);
        }

        if (null == previousResultStrings || !previousResultStrings.equals(allStrings)) {
            previousResultStrings = allStrings;

            results.setValue(filteredResults);
            allResults.setValue(scanResults);

            Log.d(CampusWiFiApplication.class.getSimpleName(), "Scan data update sent");
        } else {
            Log.d(CampusWiFiApplication.class.getSimpleName(), "Skipped scan data update");
        }

    }

    public LiveData<List<ScanResult>> getScanResults() {
        return results;
    }

    public LiveData<List<ScanResult>> getAllScanResults() {
        return allResults;
    }

    public MutableLiveData<String> getConnectionState() {
        return connected;
    }

    public void initiateScan() {
        wifiManager.startScan();
    }

    @Nullable
    public Location getLastLocation() {
        return location.getValue();
    }

    public LiveData<Location> getLocation() {
        return location;
    }

    public List<ScanResult> getLastScanResults() {
        return results.getValue();
    }

    public List<ScanResult> getAllLastScanResults() {
        return allResults.getValue();
    }
}
