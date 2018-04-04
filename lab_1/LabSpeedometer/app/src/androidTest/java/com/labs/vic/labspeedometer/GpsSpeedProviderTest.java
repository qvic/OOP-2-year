package com.labs.vic.labspeedometer;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.labs.vic.labspeedometer.helpers.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GpsSpeedProviderTest {

    private static final double DELTA = 0.000001;
    private static final String TEST_PROVIDER = "testProvider";

    private LocationManager locationManager;

    @Before
    public void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        assertNotNull(locationManager);

        locationManager.addTestProvider(TEST_PROVIDER, false, false,
                false, false, false, true, false,
                Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(TEST_PROVIDER, true);
    }

    @After
    public void tearDown() throws Exception {
        locationManager.removeTestProvider(TEST_PROVIDER);
    }

    @Test
    public void testLocationChange() {
        HandlerThread handlerThread = new HandlerThread("testLocation");
        handlerThread.start();

        Location location = new Location(TEST_PROVIDER);
        location.setLatitude(10.0);
        location.setLongitude(11.0);
        location.setAccuracy(10);
        location.setTime(2000);
        location.setElapsedRealtimeNanos(1000);

//        locationManager.setTestProviderLocation(TEST_PROVIDER, location);

//        locationManager.requestLocationUpdates(TEST_PROVIDER, 0, 0, new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                assertEquals(1, 2);
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//                assertEquals(1, 2);
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//                assertEquals(1, 2);
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//                assertEquals(1, 2);
//
//            }
//        }, handlerThread.getLooper());

//
//        GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
//                .setLocationProvider(TEST_PROVIDER)
//                .setOnLocationChanged(new Consumer<Location>() {
//                    @Override
//                    public void accept(Location location) {
//                        assertEquals(location.getLatitude(), 10.0, DELTA);
//                        assertEquals(location.getLongitude(), 10.0, DELTA);
//                    }
//                })
//                .build();
//        gpsSpeedProvider.resume();
    }
}