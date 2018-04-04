package com.labs.vic.labspeedometer;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.labs.vic.labspeedometer.helpers.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GpsSpeedProviderTest {
    @Mock
    LocationManager locationManager;

    @Test
    public void locationChangeTest() {
        final Location mockedLocation = mock(Location.class);
//        final ArgumentCaptor<Double> latitudeCapture = ArgumentCaptor.forClass(Double.class);
//        final ArgumentCaptor<Double> longitudeCapture = ArgumentCaptor.forClass(Double.class);

//        when(mockedLocation.getLatitude()).thenCallRealMethod();
//        doCallRealMethod().when(mockedLocation).setLatitude(any(Double.class));

//        doNothing().when(mockedLocation).setLatitude(latitudeCapture.capture());
//        doNothing().when(mockedLocation).setLongitude(longitudeCapture.capture());

        final GpsSpeedProvider gpsSpeedProvider = new GpsSpeedProvider.Builder(locationManager)
        .setOnLocationChanged(new Consumer<Location>() {
            @Override
            public void accept(Location location) {
                assertEquals(location.getLatitude(), 12.0, 0.0001);
                assertEquals(location.getLongitude(), 11.0, 0.0001);
            }
        })
        .build();

        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Location location = mock(Location.class);
                doReturn(12.0).when(location).getLatitude();
                doReturn(11.0).when(location).getLongitude();
                gpsSpeedProvider.onLocationChanged(location);
                return null;
            }
        }).when(locationManager).requestLocationUpdates(any(String.class),
                any(Integer.class), any(Integer.class), any(LocationListener.class));

        gpsSpeedProvider.resume();
    }
}