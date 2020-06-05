package org.menhera.campus_wifi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final static int MAIN_LOCATION_REQUEST = 100;

    private AppBarConfiguration mAppBarConfiguration;
    private CampusWiFiApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        application = (CampusWiFiApplication) getApplication();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.message_updating, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                application.initiateScan();
            }
        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_list, R.id.nav_about_app, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(CampusWiFiApplication.class.getSimpleName(), "Location not granted, requesting");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MAIN_LOCATION_REQUEST);
        }

        final View drawerHeaderView = navigationView.getHeaderView(0);
        final TextView drawerStatus = drawerHeaderView.findViewById(R.id.nav_header_status);
        final ImageView drawerIcon = drawerHeaderView.findViewById(R.id.nav_header_icon);

        application.getConnectionState().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals(CampusWiFiApplication.STATE_CONNECTED)) {
                    drawerStatus.setText(application.getLastSsid());
                    drawerIcon.setImageDrawable(getDrawable(R.drawable.ic_signal_wifi_4_bar_lock_white_24dp));
                } else if (s.equals(CampusWiFiApplication.STATE_AVAILABLE)) {
                    drawerStatus.setText(getString(R.string.notification_background_scanning));
                    drawerIcon.setImageDrawable(getDrawable(R.drawable.ic_signal_wifi_0_bar_white_24dp));
                } else {
                    drawerStatus.setText(getString(R.string.notification_background_disconnected));
                    drawerIcon.setImageDrawable(getDrawable(R.drawable.ic_signal_wifi_off_white_24dp));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
