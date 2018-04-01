package com.labs.vic.labspeedometer.helpers;

import com.labs.vic.labspeedometer.R;

public enum SpeedUnit {
    MS (1, R.string.speed_m_s),
    KMH (3.6, R.string.speed_km_h);

    private final double multiplier;
    private final int stringResource;

    SpeedUnit(double multiplier, int stringResource) {
        this.multiplier = multiplier;
        this.stringResource = stringResource;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getStringResource() {
        return stringResource;
    }

    public static double convertMS(SpeedUnit to, double speedMS) {
        return speedMS * to.getMultiplier();
    }
}

