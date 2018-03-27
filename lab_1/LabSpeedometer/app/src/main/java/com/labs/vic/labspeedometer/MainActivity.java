package com.labs.vic.labspeedometer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView latitudeView;
    private TextView longitudeView;
    private TextView accuracyView;
    private TextView speedView1;
    private TextView speedView2;

    private GpsSpeedProvider speedProvider;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_units:
                    return true;
                case R.id.navigation_gps:
                    startActivity(new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    return false;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeView = findViewById(R.id.latitudeView);
        longitudeView = findViewById(R.id.longitudeView);
        accuracyView = findViewById(R.id.accuracyView);
        speedView1 = findViewById(R.id.display1);
        speedView2 = findViewById(R.id.display2);
        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        speedProvider = new GpsSpeedProvider((LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
                new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        speedView1.setText(String.format(Locale.getDefault(), "%.3f m/s", speed));
                    }
                }, new Consumer<Double>() {
            @Override
            public void accept(Double speed) {
                speedView2.setText(String.format(Locale.getDefault(), "%.3f m/s", speed));
            }
        }, new Consumer<Location>() {
            @Override
            public void accept(Location location) {
                latitudeView.setText(String.format(Locale.getDefault(), "%s: %f",
                        getString(R.string.title_latitude), location.getLatitude()));

                longitudeView.setText(String.format(Locale.getDefault(), "%s: %f",
                        getString(R.string.title_longitude), location.getLongitude()));

                accuracyView.setText(String.format(Locale.getDefault(), "%s: %.1f",
                        getString(R.string.title_accuracy), location.getAccuracy()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No permission to use GPS", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            speedProvider.resume();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onPause() {
        super.onPause();

        speedProvider.pause();
    }
}
