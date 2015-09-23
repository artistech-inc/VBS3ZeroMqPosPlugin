/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.geo;

import com.artistech.utils.ArgumentOutOfRangeException;

/**
 *
 * @author matta
 */
public class Longitude extends CoordinateBase {

    public static final int MAX_LON = 180;
    public static final int MIN_LON = -180;

    /**
     * 
     * @throws ArgumentOutOfRangeException 
     */
    public Longitude() throws ArgumentOutOfRangeException {
        this(0);
    }

    /**
     * 
     * @param degrees
     * @throws ArgumentOutOfRangeException 
     */
    public Longitude(double degrees) throws ArgumentOutOfRangeException {
        super(degrees);
        while (super.getDegrees() < MIN_LON) {
            super._degree = (int) Math.floor((super.getDegrees() + (MAX_LON * 2)));
        }
        while (super.getDegrees() > MAX_LON) {
            super._degree = (int) Math.floor((super.getDegrees() - (MAX_LON * 2)));
        }

        if (super.getDegrees() < MIN_LON || super.getDegrees() > MAX_LON) {
            throw new ArgumentOutOfRangeException("degrees", "Longitude values cannot be less than -180 or greater than 180");
        }
    }

    /**
     * 
     * @param degree_whole
     * @param minutes
     * @throws ArgumentOutOfRangeException 
     */
    public Longitude(int degree_whole, double minutes) throws ArgumentOutOfRangeException {
        super(degree_whole, minutes);

        while (super.getDegrees() < MIN_LON) {
            super._degree = (int) Math.floor((super.getDegrees() + (MAX_LON * 2)));
        }
        while (super.getDegrees() > MAX_LON) {
            super._degree = (int) Math.floor((super.getDegrees() - (MAX_LON * 2)));
        }

        if (super.getDegrees() < MIN_LON || super.getDegrees() > MAX_LON) {
            throw new ArgumentOutOfRangeException("degrees", "Longitude values cannot be less than -180 or greater than 180");
        }
    }

    /**
     * 
     * @return 
     */
    @Override
    public CardinalPoints getCardinalMark() {
        return super.getDegrees() >= 0 ? CardinalPoints.E : CardinalPoints.W;
    }
}
