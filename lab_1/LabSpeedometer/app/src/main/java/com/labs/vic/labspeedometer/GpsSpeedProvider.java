package com.labs.vic.labspeedometer;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.labs.vic.labspeedometer.helpers.Consumer;
import com.labs.vic.labspeedometer.helpers.SpeedUnit;

import java.util.LinkedList;


class GpsSpeedProvider implements LocationListener {

    static class Builder {
        private LocationManager locationManager;

        private Runnable onGpsDisabled = new Runnable() {
            @Override
            public void run() {}
        };

        private Runnable onGpsEnabled = new Runnable() {
            @Override
            public void run() {}
        };

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

        Builder setOnGpsEnabled(Runnable onGpsEnabled) {
            this.onGpsEnabled = onGpsEnabled;
            return this;
        }

        Builder setOnGpsDisabled(Runnable onGpsDisabled) {
            this.onGpsDisabled = onGpsDisabled;
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
    private static final int BUFFER_SIZE = 2;

    private SpeedUnit speedUnit = SpeedUnit.MS;
    private Consumer<Double> onSpeedChanged;
    private Consumer<Double> onNativeSpeedChanged;
    private Consumer<Location> onLocationChanged;
    private Runnable onGpsEnabled;
    private Runnable onGpsDisabled;

    private LocationManager locationManager;

    private Location previousLocation;
    private LinkedList<Double> speedBuffer;

    private boolean isGpsUpdating = false;

    GpsSpeedProvider(Builder b) {
        this.onSpeedChanged = b.onSpeedChanged;
        this.onNativeSpeedChanged = b.onNativeSpeedChanged;
        this.onLocationChanged = b.onLocationChanged;
        this.locationManager = b.locationManager;
        this.onGpsEnabled = b.onGpsEnabled;
        this.onGpsDisabled = b.onGpsDisabled;

        this.speedBuffer = new LinkedList<>();
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

        if (!isGpsUpdating) {
            onGpsEnabled.run();
            isGpsUpdating = true;
        }

        onLocationChanged.accept(location);

        if (previousLocation == null) {
            previousLocation = location;
        } else {
            double speed = calculateSpeed(location);
            previousLocation = location;

            if (speedBuffer.size() == BUFFER_SIZE) {
                speedBuffer.remove();
            }
            speedBuffer.offer(speed);

            double averageSpeed = calculateAverageBufferSpeed();

            onSpeedChanged.accept(SpeedUnit.convertMS(speedUnit, averageSpeed));
        }

        double speedNative = location.getSpeed();

        onNativeSpeedChanged.accept(SpeedUnit.convertMS(speedUnit, speedNative));
    }

    private double calculateSpeed(Location currentLocation) {
        double distance = previousLocation.distanceTo(currentLocation);
        double timeElapsed = SECONDS_IN_NANO * (currentLocation.getElapsedRealtimeNanos() -
                previousLocation.getElapsedRealtimeNanos());

        return distance / timeElapsed;
    }

    private double calculateAverageBufferSpeed() {
        double speedMeasurementsSum = 0.0;
        int speedMeasurementsCount = speedBuffer.size();

        for (double speed : speedBuffer) {
            speedMeasurementsSum += speed;
        }

        return speedMeasurementsSum / speedMeasurementsCount;
    }

    boolean isGpsUpdating() {
        return isGpsUpdating;
    }

    boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LOCATION_PROVIDER);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LOCATION_PROVIDER)) {
            isGpsUpdating = false;
            onGpsDisabled.run();
        }
    }

    void resume() throws SecurityException {
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    void pause() {
        isGpsUpdating = false;
        locationManager.removeUpdates(this);
    }
}
