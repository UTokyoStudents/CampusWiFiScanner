package org.menhera.campus_wifi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView about_app_version = findViewById(R.id.about_app_version);
        Button about_start_button = findViewById(R.id.about_start_button);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            about_app_version.setText("Version: " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        about_start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivity(intent);
            }
        });
    }
}
