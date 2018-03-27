package com.labs.vic.labspeedometer;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

interface Consumer<T> {
    void accept(T t);
}

class GpsSpeedProvider implements LocationListener {

    private static final int MIN_TIME = 0;
    private static final int MIN_DISTANCE = 0;
    private static final double NANOS_TO_SEC = Math.pow(10, -9);

    private Consumer<Double> onSpeedChanged;
    private Consumer<Double> onNativeSpeedChanged;
    private Consumer<Location> onLocationChanged;

    private LocationManager locationManager;

    private Location previousLocation;

    GpsSpeedProvider(LocationManager locationManager,
                     Consumer<Double> onSpeedChanged,
                     Consumer<Double> onNativeSpeedChanged,
                     Consumer<Location> onLocationChanged) {

        this.onSpeedChanged = onSpeedChanged;
        this.onNativeSpeedChanged = onNativeSpeedChanged;
        this.onLocationChanged = onLocationChanged;

        this.locationManager = locationManager;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        onLocationChanged.accept(location);

        if (previousLocation == null) {
            previousLocation = location;
            return;
        }

        double distance = previousLocation.distanceTo(location);
        double timeElapsed = NANOS_TO_SEC * (location.getElapsedRealtimeNanos() -
                previousLocation.getElapsedRealtimeNanos());

        double speed = distance / timeElapsed;
        double speed2 = location.getSpeed();

        onSpeedChanged.accept(speed);
        onNativeSpeedChanged.accept(speed2);
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
