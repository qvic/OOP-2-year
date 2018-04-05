package com.labs.vic.labspeedometer;

import android.location.Location;
import android.location.LocationManager;

import com.labs.vic.labspeedometer.helpers.Consumer;
import com.labs.vic.labspeedometer.helpers.SpeedUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class GpsSpeedProviderTest {

    private static final double DELTA_L = 0.0000001;
    private static final long ONE_SECOND = 1000000000L;
    private static final double DELTA_S = 0.01;

    @Mock
    private LocationManager locationManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void locationChangeTest() {
        GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
                .setOnLocationChanged(new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        assertNotNull(location);

                        assertEquals(location.getLatitude(), 50.0, DELTA_L);
                        assertEquals(location.getLongitude(), 30.0, DELTA_L);
                    }
                })
                .build();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GpsSpeedProvider gpsSpeedProvider = invocation.getArgument(3);

                Location location = mock(Location.class);

                doReturn(50.0).when(location).getLatitude();
                doReturn(30.0).when(location).getLongitude();

                gpsSpeedProvider.onLocationChanged(location);

                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Integer.class), any(Integer.class), any(GpsSpeedProvider.class));

        gpsSpeedProvider.resume();
    }

    @Test
    public void speedChangeTest() {
        GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
                .setOnSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        assertEquals(speed, 13.22, DELTA_S);
                    }
                })
                .build();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GpsSpeedProvider gpsSpeedProvider = invocation.getArgument(3);

                Location loc1 = mock(Location.class);

                doReturn(50.0).when(loc1).getLatitude();
                doReturn(30.0).when(loc1).getLongitude();
                doReturn(1.0f).when(loc1).getAccuracy();
                doReturn(ONE_SECOND).when(loc1).getElapsedRealtimeNanos();

                Location loc2 = mock(Location.class);

                doReturn(50.0001).when(loc2).getLatitude();
                doReturn(30.0001).when(loc2).getLongitude();
                doReturn(2 * ONE_SECOND).when(loc2).getElapsedRealtimeNanos();
                // assuming Location calculates distance correctly
                doReturn(13.22f).when(loc1).distanceTo(loc2);


                Location loc3 = mock(Location.class);

                doReturn(50.0005).when(loc3).getLatitude();
                doReturn(30.0005).when(loc3).getLongitude();
                doReturn(6 * ONE_SECOND).when(loc3).getElapsedRealtimeNanos();
                doReturn(52.87f).when(loc2).distanceTo(loc3);

                gpsSpeedProvider.onLocationChanged(loc1);
                gpsSpeedProvider.onLocationChanged(loc2);
                gpsSpeedProvider.onLocationChanged(loc3);

                verify(loc1, times(1)).distanceTo(any(Location.class));
                verify(loc2, times(1)).distanceTo(any(Location.class));
                verify(loc3, times(0)).distanceTo(any(Location.class));

                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Integer.class), any(Integer.class), any(GpsSpeedProvider.class));

        gpsSpeedProvider.resume();
    }

    @Test
    public void speedChangeKmhTest() {
        GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
                .setOnSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        assertEquals(speed, 47.592, DELTA_S);
                    }
                })
                .build();

        gpsSpeedProvider.setSpeedUnit(SpeedUnit.KMH);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GpsSpeedProvider gpsSpeedProvider = invocation.getArgument(3);

                Location loc1 = mock(Location.class);

                doReturn(50.0).when(loc1).getLatitude();
                doReturn(30.0).when(loc1).getLongitude();
                doReturn(1.0f).when(loc1).getAccuracy();
                doReturn(ONE_SECOND).when(loc1).getElapsedRealtimeNanos();


                Location loc2 = mock(Location.class);

                doReturn(50.0001).when(loc2).getLatitude();
                doReturn(30.0001).when(loc2).getLongitude();
                doReturn(2 * ONE_SECOND).when(loc2).getElapsedRealtimeNanos();
                // assuming Location calculates distance correctly
                doReturn(13.22f).when(loc1).distanceTo(loc2);


                Location loc3 = mock(Location.class);

                doReturn(50.0005).when(loc3).getLatitude();
                doReturn(30.0005).when(loc3).getLongitude();
                doReturn(6 * ONE_SECOND).when(loc3).getElapsedRealtimeNanos();
                doReturn(52.87f).when(loc2).distanceTo(loc3);

                gpsSpeedProvider.onLocationChanged(loc1);
                gpsSpeedProvider.onLocationChanged(loc2);
                gpsSpeedProvider.onLocationChanged(loc3);

                verify(loc1, times(1)).distanceTo(any(Location.class));
                verify(loc2, times(1)).distanceTo(any(Location.class));
                verify(loc3, times(0)).distanceTo(any(Location.class));

                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Integer.class), any(Integer.class), any(GpsSpeedProvider.class));

        gpsSpeedProvider.resume();
    }
}