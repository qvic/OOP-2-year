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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.labs.vic.labspeedometer.helpers.Consumer;
import com.labs.vic.labspeedometer.helpers.SpeedUnit;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int GPS_FIXED_ICON = R.drawable.ic_gps_fixed_black_24dp;
    public static final int GPS_NOT_FIXED_ICON = R.drawable.ic_gps_not_fixed_black_24dp;

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
                    updateLargeSpeedView(0.0);
                    updateSmallSpeedView(0.0);
                }

                animateUnitsMenu(true);

                return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelected
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    animateUnitsMenu(true);
                    return true;
                case R.id.navigation_units:
                    animateUnitsMenu(false);
                    return true;
                case R.id.navigation_gps:
                    startActivity(new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    return false;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener onNavigationItemReselected
            = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_units) {
                animateUnitsMenu(false);
            }
        }
    };

    private void animateUnitsMenu(boolean close) {
        BottomNavigationView unitsMenuView = findViewById(R.id.units_menu);

        int toY = 0;
        if (!close && unitsMenuHidden) {
            toY = -unitsMenuView.getHeight();
        }

        unitsMenuView.animate().translationY(toY);
        unitsMenuHidden = close || !unitsMenuHidden;
    }

    private void updateLargeSpeedView(double speedLarge) {
        TextView speedViewLarge = findViewById(R.id.display_large);

        String speedLargeFormatted = String.format(Locale.getDefault(), getString(R.string.speed_format),
                speedLarge, getString(speedProvider.getSpeedUnit().getStringResource()));

        speedViewLarge.setText(speedLargeFormatted);
    }

    private void updateSmallSpeedView(double speedSmall) {
        TextView speedViewSmall = findViewById(R.id.display_small);

        String speedSmallFormatted = String.format(Locale.getDefault(), getString(R.string.speed_format),
                speedSmall, getString(speedProvider.getSpeedUnit().getStringResource()));

        speedViewSmall.setText(speedSmallFormatted);
    }

    private void updateLocationViews(Location location) {
        TextView latitudeView = findViewById(R.id.latitude_view);
        TextView longitudeView = findViewById(R.id.longitude_view);
        TextView accuracyView = findViewById(R.id.accuracy_view);

        latitudeView.setText(String.format(Locale.getDefault(), getString(R.string.coordinate_format),
                getString(R.string.title_latitude), location.getLatitude()));

        longitudeView.setText(String.format(Locale.getDefault(), getString(R.string.coordinate_format),
                getString(R.string.title_longitude), location.getLongitude()));

        accuracyView.setText(String.format(Locale.getDefault(), getString(R.string.accuracy_message),
                location.getExtras().getInt("satellites"),
                location.getAccuracy()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView unitsMenuView = findViewById(R.id.units_menu);
        animateUnitsMenu(true);
        unitsMenuView.setOnNavigationItemSelectedListener(onUnitsSelected);

        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(onNavigationItemSelected);
        navigationView.setOnNavigationItemReselectedListener(onNavigationItemReselected);

        speedProvider = new GpsSpeedProvider.Builder((LocationManager) this.getSystemService(Context.LOCATION_SERVICE))
                .setOnSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        updateLargeSpeedView(speed);
                    }
                })
                .setOnNativeSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        updateSmallSpeedView(speed);
                    }
                })
                .setOnLocationChanged(new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        updateLocationViews(location);
                    }
                })
                .setOnGpsEnabled(new Runnable() {
                    @Override
                    public void run() {
                        BottomNavigationView view = findViewById(R.id.navigation);
                        Menu menu = view.getMenu();
                        menu.findItem(R.id.navigation_gps).setIcon(GPS_FIXED_ICON);
                    }
                })
                .setOnGpsDisabled(new Runnable() {
                    @Override
                    public void run() {
                        BottomNavigationView view = findViewById(R.id.navigation);
                        Menu menu = view.getMenu();
                        menu.findItem(R.id.navigation_gps).setIcon(GPS_NOT_FIXED_ICON);
                    }
                })
                .build();

        updateLargeSpeedView(0.0);
        updateSmallSpeedView(0.0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (speedProvider.isGpsEnabled()) {
            BottomNavigationView view = findViewById(R.id.navigation);
            view.getMenu().findItem(R.id.navigation_gps).setIcon(GPS_NOT_FIXED_ICON);
        }

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
