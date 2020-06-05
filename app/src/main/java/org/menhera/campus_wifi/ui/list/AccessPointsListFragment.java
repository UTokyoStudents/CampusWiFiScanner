package org.menhera.campus_wifi.ui.list;

import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.menhera.campus_wifi.CampusWiFiApplication;
import org.menhera.campus_wifi.R;

import java.util.ArrayList;
import java.util.List;

public class AccessPointsListFragment extends Fragment {

    private AccessPointsListViewModel accessPointsListViewModel;

    private RecyclerView recyclerView;
    private AccessPointsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private CampusWiFiApplication application;

    public static final class AccessPointsAdapter extends RecyclerView.Adapter<AccessPointsAdapter.AccessPointsViewHolder> {

        public static final class AccessPointsViewHolder extends RecyclerView.ViewHolder {
            public View mView;
            public TextView bssid;
            public TextView ssid;
            public TextView frequency;
            public TextView rssi;
            public TextView capabilities;

            public AccessPointsViewHolder(@NonNull View itemView) {
                super(itemView);
                mView = itemView;
                bssid = itemView.findViewById(R.id.list_item_bssid);
                ssid = itemView.findViewById(R.id.list_item_ssid);
                frequency = itemView.findViewById(R.id.list_item_frequency);
                rssi = itemView.findViewById(R.id.list_item_rssi);
                capabilities = itemView.findViewById(R.id.list_item_caps);
            }
        }

        List<ScanResult> mResults;

        public AccessPointsAdapter(@NonNull List<ScanResult> results) {
            mResults = results;
        }

        public AccessPointsAdapter() {
            this(new ArrayList<ScanResult>());
        }

        public void setScanResults(@NonNull List<ScanResult> results) {
            mResults = results;
            Log.d(AccessPointsAdapter.class.getSimpleName(), "setScanResults(): size = " + results.size());
        }

        @NonNull
        @Override
        public AccessPointsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_access_point, parent, false);
            AccessPointsViewHolder viewHolder = new AccessPointsViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull AccessPointsViewHolder holder, int position) {
            Log.d(AccessPointsAdapter.class.getSimpleName(), "onBindViewHolder(): " + position);
            ScanResult result = mResults.get(position);
            holder.bssid.setText(result.BSSID);
            holder.ssid.setText(result.SSID);

            String channelWidth;
            switch (result.channelWidth) {
                case ScanResult.CHANNEL_WIDTH_20MHZ:
                    channelWidth = "20 MHz";
                    break;

                case ScanResult.CHANNEL_WIDTH_40MHZ:
                    channelWidth = "40 MHz";
                    break;

                case ScanResult.CHANNEL_WIDTH_80MHZ:
                    channelWidth = "80 MHz";
                    break;

                case ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ:
                    channelWidth = "80 MHz × 2";
                    break;

                case ScanResult.CHANNEL_WIDTH_160MHZ:
                    channelWidth = "160 MHz";
                    break;

                default:
                    channelWidth = "<Unknown>";
            }
            holder.frequency.setText(result.frequency + " MHz (Channel width: " + channelWidth + ")");
            holder.rssi.setText(result.level + " dBm");
            holder.capabilities.setText(result.capabilities);
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        accessPointsListViewModel =
                ViewModelProviders.of(this).get(AccessPointsListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        application = (CampusWiFiApplication) getContext().getApplicationContext();

        recyclerView = (RecyclerView) root.findViewById(R.id.list_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new AccessPointsAdapter();
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        application.getScanResults().observe(getViewLifecycleOwner(), new Observer<List<ScanResult>>() {
            @Override
            public void onChanged(List<ScanResult> scanResults) {
                if (preferences.getBoolean("wifi_scan_all", false)) return;
                adapter.setScanResults(scanResults);
                adapter.notifyDataSetChanged();
            }
        });

        application.getAllScanResults().observe(getViewLifecycleOwner(), new Observer<List<ScanResult>>() {
            @Override
            public void onChanged(List<ScanResult> scanResults) {
                if (!preferences.getBoolean("wifi_scan_all", false)) return;
                adapter.setScanResults(scanResults);
                adapter.notifyDataSetChanged();
            }
        });

        return root;
    }
}