package com.labs.vic.labspeedometer;

import android.location.Location;
import android.location.LocationManager;

import com.labs.vic.labspeedometer.helpers.Consumer;
import com.labs.vic.labspeedometer.helpers.SpeedUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class GpsSpeedProviderTest {

    private static final double DELTA_L = 0.0000001;
    private static final long ONE_SECOND = 1000000000L;
    private static final double DELTA_S = 0.000001;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private LocationManager locationManager;

    private abstract class TestConsumer<T> implements Consumer<T> {
        private int callsCounter = 0;

        @Override
        public void accept(T t) {
            acceptTest(t);
            callsCounter++;
        }

        int getCallsCounter() {
            return callsCounter;
        }

        abstract void acceptTest(T t);
    }

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

                        assertEquals(50.0, location.getLatitude(), DELTA_L);
                        assertEquals(30.0, location.getLongitude(), DELTA_L);
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
                any(Long.class), any(Float.class), any(GpsSpeedProvider.class));

        gpsSpeedProvider.resume();

        verify(locationManager, times(1)).requestLocationUpdates(any(String.class),
                any(Long.class), any(Float.class), any(GpsSpeedProvider.class));
    }

    @Test
    public void speedChangeTest() {
        GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
                .setOnSpeedChanged(new TestConsumer<Double>() {
                    private double[] expectedValues = {10.0, 7.5, 10.0};

                    @Override
                    void acceptTest(Double speed) {
                        assertEquals(expectedValues[getCallsCounter()], speed, DELTA_S);
                    }
                })
                .build();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GpsSpeedProvider gpsSpeedProvider = invocation.getArgument(3);

                Location loc1 = mock(Location.class);

                doReturn(ONE_SECOND).when(loc1).getElapsedRealtimeNanos();


                Location loc2 = mock(Location.class);

                doReturn(2 * ONE_SECOND).when(loc2).getElapsedRealtimeNanos();
                doReturn(10.0f).when(loc1).distanceTo(loc2);


                Location loc3 = mock(Location.class);

                doReturn(6 * ONE_SECOND).when(loc3).getElapsedRealtimeNanos();
                doReturn(20.0f).when(loc2).distanceTo(loc3);


                Location loc4 = mock(Location.class);

                doReturn(8 * ONE_SECOND).when(loc4).getElapsedRealtimeNanos();
                doReturn(30.0f).when(loc3).distanceTo(loc4);

                gpsSpeedProvider.onLocationChanged(loc1);
                gpsSpeedProvider.onLocationChanged(loc2);
                gpsSpeedProvider.onLocationChanged(loc3);
                gpsSpeedProvider.onLocationChanged(loc4);

                verify(loc1, times(1)).distanceTo(any(Location.class));
                verify(loc2, times(1)).distanceTo(any(Location.class));
                verify(loc3, times(1)).distanceTo(any(Location.class));
                verify(loc4, times(0)).distanceTo(any(Location.class));

                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Long.class), any(Float.class), any(GpsSpeedProvider.class));

        gpsSpeedProvider.resume();

        verify(locationManager, times(1)).requestLocationUpdates(any(String.class),
                any(Long.class), any(Float.class), any(GpsSpeedProvider.class));
    }

    @Test
    public void speedChangeKmhTest() {
        GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
                .setOnSpeedChanged(new Consumer<Double>() {
                    @Override
                    public void accept(Double speed) {
                        assertEquals(36.0, speed, DELTA_S);
                    }
                })
                .build();

        gpsSpeedProvider.setSpeedUnit(SpeedUnit.KMH);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                GpsSpeedProvider gpsSpeedProvider = invocation.getArgument(3);

                Location loc1 = mock(Location.class);

                doReturn(ONE_SECOND).when(loc1).getElapsedRealtimeNanos();


                Location loc2 = mock(Location.class);

                doReturn(2 * ONE_SECOND).when(loc2).getElapsedRealtimeNanos();
                doReturn(10.0f).when(loc1).distanceTo(loc2);

                gpsSpeedProvider.onLocationChanged(loc1);
                gpsSpeedProvider.onLocationChanged(loc2);

                verify(loc1, times(1)).distanceTo(any(Location.class));
                verify(loc2, times(0)).distanceTo(any(Location.class));

                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Long.class), any(Float.class), any(GpsSpeedProvider.class));

        gpsSpeedProvider.resume();

        verify(locationManager, times(1)).requestLocationUpdates(any(String.class),
                any(Long.class), any(Float.class), any(GpsSpeedProvider.class));
    }
}