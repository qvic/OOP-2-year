package com.labs.vic.labspeedometer.helpers;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpeedUnitTest {

    private static final double DELTA = 0.000001;

    @Test
    public void convertMStoKMH() {
        assertEquals(SpeedUnit.convertMS(SpeedUnit.KMH, 123.35), 444.06, DELTA);
        assertEquals(SpeedUnit.convertMS(SpeedUnit.KMH, 10.0), 36.0, DELTA);
        assertEquals(SpeedUnit.convertMS(SpeedUnit.KMH, 0.0), 0.0, DELTA);
        assertEquals(SpeedUnit.convertMS(SpeedUnit.MS, 20.0), 20.0, DELTA);
    }

    @Test
    public void convertMStoMS() {
        assertEquals(SpeedUnit.convertMS(SpeedUnit.MS, 20.0), 20.0, DELTA);
    }
}