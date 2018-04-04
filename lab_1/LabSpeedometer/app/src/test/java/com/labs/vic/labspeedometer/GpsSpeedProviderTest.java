package com.labs.vic.labspeedometer;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.labs.vic.labspeedometer.helpers.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class GpsSpeedProviderTest {
    private static final double DELTA = 0.0000001;

    @Mock
    private LocationManager locationManager;

    @Test
    public void locationChangeTest() {
        final GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
                .setOnLocationChanged(new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        assertNotNull(location);

                        assertNotEquals(location.getLatitude(), 0.0, DELTA);
                        assertNotEquals(location.getLongitude(), 0.0, DELTA);

                        assertEquals(location.getLatitude(), 50.0, DELTA);
                        assertEquals(location.getLongitude(), 30.0, DELTA);
                    }
                })
                .setOnSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        assertEquals(speed, 13.22, DELTA);
                    }
                })
                .build();

        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Location loc1 = mock(Location.class);

                doReturn(50.0).when(loc1).getLatitude();
                doReturn(30.0).when(loc1).getLongitude();
                doReturn(1.0f).when(loc1).getAccuracy();
                doReturn(1000000L).when(loc1).getElapsedRealtimeNanos();

                gpsSpeedProvider.onLocationChanged(loc1);

                Location loc2 = mock(Location.class);

                doReturn(50.00001).when(loc2).getLatitude();
                doReturn(30.00001).when(loc2).getLongitude();
                doReturn(1.0f).when(loc2).getAccuracy();
                doReturn(2000000L).when(loc2).getElapsedRealtimeNanos();

                gpsSpeedProvider.onLocationChanged(loc2);
                // TODO: fix testing speed, gpsSpeedProvider won't change 'cause it's final

                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Integer.class), any(Integer.class), any(LocationListener.class));

        gpsSpeedProvider.resume();
    }
}