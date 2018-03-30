package com.labs.vic.labspeedometer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView latitudeView;
    private TextView longitudeView;
    private TextView accuracyView;
    private TextView speedView1;
    private TextView speedView2;

    private BottomNavigationView unitsMenuView;
    private boolean unitsMenuHidden = true;

    private GpsSpeedProvider speedProvider;

    private BottomNavigationView.OnNavigationItemSelectedListener onUnitsSelected = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int itemId = item.getItemId();
            if (itemId == R.id.units_ms || itemId == R.id.units_kmh) {
                if (itemId == R.id.units_ms) {
                    speedProvider.setSpeedUnit(SpeedUnit.MS);
                } else {
                    speedProvider.setSpeedUnit(SpeedUnit.KMH);
                }

                if(!speedProvider.isGpsUpdating()) {
                    String defaultSpeed = String.format(Locale.getDefault(), getString(R.string.speed_format),
                            0.0, getString(speedProvider.getSpeedUnit().getStringResource()));
                    speedView1.setText(defaultSpeed);
                    speedView2.setText(defaultSpeed);
                }

                return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    unitsMenuView.animate().translationY(0);
                    unitsMenuHidden = true;
                    return true;
                case R.id.navigation_units:
                    unitsMenuView.setVisibility(View.VISIBLE);
                    unitsMenuView.animate().translationY(-unitsMenuView.getHeight());
                    unitsMenuHidden = false;
                    return true;
                case R.id.navigation_gps:
                    unitsMenuView.animate().translationY(0);
                    unitsMenuHidden = true;
                    startActivity(new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    return false;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener onNavigationItemReselectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_units) {
                if (unitsMenuHidden) {
                    unitsMenuView.animate().translationY(-unitsMenuView.getHeight());
                    unitsMenuHidden = false;
                } else {
                    unitsMenuView.animate().translationY(0);
                    unitsMenuHidden = true;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeView = findViewById(R.id.latitude_view);
        longitudeView = findViewById(R.id.longitude_view);
        accuracyView = findViewById(R.id.accuracy_view);
        speedView1 = findViewById(R.id.display1);
        speedView2 = findViewById(R.id.display2);

        unitsMenuHidden = true;
        unitsMenuView = findViewById(R.id.units_menu);
        unitsMenuView.setOnNavigationItemSelectedListener(onUnitsSelected);

        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        navigationView.setOnNavigationItemReselectedListener(onNavigationItemReselectedListener);

        speedProvider = new GpsSpeedProvider.Builder(this)
                .setOnSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        speedView1.setText(
                                String.format(
                                        Locale.getDefault(), getString(R.string.speed_format),
                                        speed,
                                        getString(speedProvider.getSpeedUnit().getStringResource())));
                    }
                })
                .setOnNativeSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        speedView2.setText(
                                String.format(Locale.getDefault(), getString(R.string.speed_format),
                                        speed,
                                        getString(speedProvider.getSpeedUnit().getStringResource())));
                    }
                })
                .setOnLocationChanged(new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        latitudeView.setText(String.format(Locale.getDefault(), "%s: %f",
                                getString(R.string.title_latitude), location.getLatitude()));

                        longitudeView.setText(String.format(Locale.getDefault(), "%s: %f",
                                getString(R.string.title_longitude), location.getLatitude()));

                        accuracyView.setText(String.format(Locale.getDefault(), "%d satellites give %.1f m accuracy",
                                location.getExtras().getInt("satellites"),
                                location.getAccuracy()));
                    }
                })
                .build();

        String defaultSpeed = String.format(Locale.getDefault(), getString(R.string.speed_format),
                0.0, getString(speedProvider.getSpeedUnit().getStringResource()));
        speedView1.setText(defaultSpeed);
        speedView2.setText(defaultSpeed);
    }

    @Override
    protected void onResume() {
        super.onResume();

        unitsMenuHidden = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.gps_no_permission, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            speedProvider.resume();
        } catch (SecurityException ignored) {}
    }

    @Override
    protected void onPause() {
        super.onPause();

        speedProvider.pause();
    }
}
