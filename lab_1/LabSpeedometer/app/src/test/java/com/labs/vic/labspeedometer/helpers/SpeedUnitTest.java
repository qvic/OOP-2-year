package com.labs.vic.labspeedometer.helpers;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpeedUnitTest {

    private static final double DELTA = 0.000001;

    @Test
    public void convertMStoKMH() {
        assertEquals(444.06, SpeedUnit.convertMS(SpeedUnit.KMH, 123.35), DELTA);
        assertEquals(36.0, SpeedUnit.convertMS(SpeedUnit.KMH, 10.0), DELTA);
        assertEquals(0.0, SpeedUnit.convertMS(SpeedUnit.KMH, 0.0), DELTA);
        assertEquals(20.0, SpeedUnit.convertMS(SpeedUnit.MS, 20.0), DELTA);
    }

    @Test
    public void convertMStoMS() {
        assertEquals(20.0, SpeedUnit.convertMS(SpeedUnit.MS, 20.0), DELTA);
    }
}