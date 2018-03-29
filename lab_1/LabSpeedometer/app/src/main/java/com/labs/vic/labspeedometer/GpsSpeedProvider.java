package com.labs.vic.labspeedometer;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

interface Consumer<T> {
    void accept(T t);
}

enum SpeedUnit {
    MS (1, R.string.speed_m_s),
    KMH (3.6, R.string.speed_km_h);

    private final double multiplier;
    private final int stringResource;

    SpeedUnit(double multiplier, int stringResource) {
        this.multiplier = multiplier;
        this.stringResource = stringResource;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getStringResource() {
        return stringResource;
    }

    public static double convertMS(SpeedUnit to, double speed) {
        return speed * to.getMultiplier();
    }
}

class GpsSpeedProvider implements LocationListener {

    private static final int MIN_TIME = 0;
    private static final int MIN_DISTANCE = 0;
    private static final double SECONDS_IN_NANO = Math.pow(10, -9);

    private SpeedUnit speedUnit = SpeedUnit.MS;

    private Consumer<Double> onSpeedChanged;
    private Consumer<Double> onNativeSpeedChanged;
    private Consumer<Location> onLocationChanged;

    private LocationManager locationManager;

    private Location previousLocation;

    private Context context;

    private boolean isGpsUpdating = false;

    GpsSpeedProvider(Context context,
                     Consumer<Double> onSpeedChanged,
                     Consumer<Double> onNativeSpeedChanged,
                     Consumer<Location> onLocationChanged) {

        this.onSpeedChanged = onSpeedChanged;
        this.onNativeSpeedChanged = onNativeSpeedChanged;
        this.onLocationChanged = onLocationChanged;

        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        isGpsUpdating = true;

        Log.i("Location", String.valueOf(location.getExtras().getInt("satellites")));

        onLocationChanged.accept(location);

        // weighted average or smth
        if (previousLocation == null) {
            previousLocation = location;
            return;
        }

        double distance = previousLocation.distanceTo(location);
        double timeElapsed = SECONDS_IN_NANO * (location.getElapsedRealtimeNanos() -
                previousLocation.getElapsedRealtimeNanos());

        double speed = distance / timeElapsed;
        double speedNative = location.getSpeed();

        onSpeedChanged.accept(SpeedUnit.convertMS(speedUnit, speed));
        onNativeSpeedChanged.accept(SpeedUnit.convertMS(speedUnit, speedNative));
    }

    boolean isGpsUpdating() {
        return isGpsUpdating;
    }

    boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    void resume() throws SecurityException {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    void pause() {
        locationManager.removeUpdates(this);
    }
}
