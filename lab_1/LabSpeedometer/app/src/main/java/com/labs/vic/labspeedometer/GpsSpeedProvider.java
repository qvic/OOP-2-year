package com.labs.vic.labspeedometer;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.labs.vic.labspeedometer.helpers.Consumer;
import com.labs.vic.labspeedometer.helpers.SpeedUnit;


class GpsSpeedProvider implements LocationListener {

    static class Builder {
        private LocationManager locationManager;
        private String locationProvider = LOCATION_PROVIDER;

        private Consumer<Double> onSpeedChanged = new Consumer<Double>() {
            @Override
            public void accept(Double aDouble) {}
        };

        private Consumer<Double> onNativeSpeedChanged = new Consumer<Double>() {
            @Override
            public void accept(Double aDouble) {}
        };

        private Consumer<Location> onLocationChanged = new Consumer<Location>() {
            @Override
            public void accept(Location location) {}
        };

        Builder(LocationManager locationManager) {
            this.locationManager = locationManager;
        }

        Builder setOnSpeedChanged(Consumer<Double> onSpeedChanged) {
            this.onSpeedChanged = onSpeedChanged;
            return this;
        }

        Builder setOnNativeSpeedChanged(Consumer<Double> onNativeSpeedChanged) {
            this.onNativeSpeedChanged = onNativeSpeedChanged;
            return this;
        }

        Builder setOnLocationChanged(Consumer<Location> onLocationChanged) {
            this.onLocationChanged = onLocationChanged;
            return this;
        }

        Builder setLocationProvider(String locationProvider) {
            this.locationProvider = locationProvider;
            return this;
        }

        GpsSpeedProvider build() {
            return new GpsSpeedProvider(this);
        }
    }

    private static final int MIN_TIME = 0;
    private static final int MIN_DISTANCE = 0;
    private static final double SECONDS_IN_NANO = Math.pow(10, -9);
    private static final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    private SpeedUnit speedUnit = SpeedUnit.MS;
    private Consumer<Double> onSpeedChanged;
    private Consumer<Double> onNativeSpeedChanged;
    private Consumer<Location> onLocationChanged;

    private LocationManager locationManager;
    private Location previousLocation;
    private String locationProvider;

    private boolean isGpsUpdating = false;

    GpsSpeedProvider(Builder b) {
        this.onSpeedChanged = b.onSpeedChanged;
        this.onNativeSpeedChanged = b.onNativeSpeedChanged;
        this.onLocationChanged = b.onLocationChanged;
        this.locationManager = b.locationManager;
        this.locationProvider = b.locationProvider;
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
        onLocationChanged.accept(location);

        if (previousLocation == null) {
            previousLocation = location;
            return;
        }

        double speed = calculateSpeed(location);
        double speedNative = location.getSpeed();

        onSpeedChanged.accept(SpeedUnit.convertMS(speedUnit, speed));
        onNativeSpeedChanged.accept(SpeedUnit.convertMS(speedUnit, speedNative));
    }

    private double calculateSpeed(Location location) {
        double distance = previousLocation.distanceTo(location);
        double timeElapsed = SECONDS_IN_NANO * (location.getElapsedRealtimeNanos() -
                previousLocation.getElapsedRealtimeNanos());

        return distance / timeElapsed;
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
        locationManager.requestLocationUpdates(locationProvider, MIN_TIME, MIN_DISTANCE, this);
    }

    void pause() {
        isGpsUpdating = false;
        locationManager.removeUpdates(this);
    }
}
