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
        private Context context;
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

        Builder(Context context) {
            this.context = context;
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

        GpsSpeedProvider build() {
            return new GpsSpeedProvider(this);
        }
    }

    private static final int MIN_TIME = 0;
    private static final int MIN_DISTANCE = 0;
    private static final double SECONDS_IN_NANO = Math.pow(10, -9);

    private SpeedUnit speedUnit = SpeedUnit.MS;
    private Consumer<Double> onSpeedChanged;
    private Consumer<Double> onNativeSpeedChanged;
    private Consumer<Location> onLocationChanged;

    private LocationManager locationManager;
    private Location previousLocation;

    private boolean isGpsUpdating = false;

    GpsSpeedProvider(Builder b) {
        this.onSpeedChanged = b.onSpeedChanged;
        this.onNativeSpeedChanged = b.onNativeSpeedChanged;
        this.onLocationChanged = b.onLocationChanged;
        this.locationManager = (LocationManager) b.context.getSystemService(Context.LOCATION_SERVICE);
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
        isGpsUpdating = false;
        locationManager.removeUpdates(this);
    }
}
