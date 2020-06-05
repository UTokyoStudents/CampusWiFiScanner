package org.menhera.campus_wifi.ui.home;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.menhera.campus_wifi.CampusWiFiApplication;
import org.menhera.campus_wifi.R;
import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        final TextView home_stats_aps = root.findViewById(R.id.home_stats_aps);
        final TextView home_stats_strongest = root.findViewById(R.id.home_stats_strongest);
        final TextView home_connection_status = root.findViewById(R.id.home_connection_status);
        final TextView home_connection_bssid = root.findViewById(R.id.home_connection_bssid);
        final TextView home_connection_ssid = root.findViewById(R.id.home_connection_ssid);
        final TextView home_connection_frequency = root.findViewById(R.id.home_connection_frequency);
        final TextView home_connection_rssi = root.findViewById(R.id.home_connection_rssi);
        final TextView home_connection_linkspeed = root.findViewById(R.id.home_connection_linkspeed);

        final TextView home_location_lat = root.findViewById(R.id.home_location_lat);
        final TextView home_location_lon = root.findViewById(R.id.home_location_lon);
        final TextView home_location_accuracy = root.findViewById(R.id.home_location_accuracy);

        final CampusWiFiApplication application = (CampusWiFiApplication) getActivity().getApplication();

        application.getLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                String latStr = "";
                String lonStr = "";

                latStr += Math.abs(lat);
                latStr += lat < 0 ? " S" : " N";
                lonStr += Math.abs(lon);
                lonStr += lon < 0 ? " W" : " E";

                home_location_lat.setText(latStr);
                home_location_lon.setText(lonStr);

                home_location_accuracy.setText("(± " + location.getAccuracy() + " m)");
            }
        });

        homeViewModel.getStatistics().observe(this, new Observer<HomeViewModel.ScanStatistics>() {
            @Override
            public void onChanged(HomeViewModel.ScanStatistics scanStatistics) {
                home_stats_aps.setText("Available APs: " + scanStatistics.numberOfAccessPoints);
                home_stats_strongest.setText("Strongest signal: " + scanStatistics.strongestSignal + " dBm (" + scanStatistics.strongestBSSID + ")");
            }
        });

        application.getScanResults().observe(this, new Observer<List<ScanResult>>() {
            @Override
            public void onChanged(List<ScanResult> scanResults) {
                Set<String> aps = new HashSet<>();
                int strongestSignal = -32767;
                String strongestSSID = "<none>";
                String strongestBSSID = "-";

                for (ScanResult result : scanResults) {
                    aps.add(result.BSSID);
                    if (strongestSignal < result.level) {
                        strongestSignal = result.level;
                        strongestSSID = result.SSID;
                        strongestBSSID = result.BSSID;
                    }
                }

                HomeViewModel.ScanStatistics stats = new HomeViewModel.ScanStatistics();
                stats.numberOfAccessPoints = aps.size();
                stats.strongestSignal = strongestSignal;
                stats.strongestSSID = strongestSSID;
                stats.strongestBSSID = strongestBSSID;

                homeViewModel.setStatistics(stats);
            }
        });

        application.getConnectionState().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String status = "";
                if (s.equals(CampusWiFiApplication.STATE_CONNECTED)) {
                    status = "Connected";
                } else if (s.equals(CampusWiFiApplication.STATE_AVAILABLE)) {
                    status = "Available";
                } else {
                    status = "Disconnected";
                }

                home_connection_status.setText(status);
                home_connection_bssid.setText("BSSID: " + application.getLastBssid());
                home_connection_ssid.setText("SSID: " + application.getLastSsid());
                home_connection_frequency.setText("Frequency: " + application.getLastFrequency() + " MHz");
                home_connection_rssi.setText("RSSI: " + application.getLastRssi() + " dBm");
                home_connection_linkspeed.setText("Link speed: " + application.getLastLinkSpeed() + " Mbps");
            }
        });

        return root;
    }
}