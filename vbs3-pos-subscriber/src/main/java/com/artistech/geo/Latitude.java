/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.geo;

import com.artistech.utils.ArgumentOutOfRangeException;

/**
 *
 * @author matta
 */
public class Latitude extends CoordinateBase {

    public static final int MAX_LAT = 90;
    public static final int MIN_LAT = -90;

    /**
     * 
     * @throws ArgumentOutOfRangeException 
     */
    public Latitude() throws ArgumentOutOfRangeException {
        this(0);
    }

    /**
     * 
     * @param degrees
     * @throws ArgumentOutOfRangeException 
     */
    public Latitude(double degrees) throws ArgumentOutOfRangeException {
        super(degrees);

        while (super.getDegrees() < MIN_LAT) {
            double last = super.getDegrees();
            super._degree = (int) Math.floor((super.getDegrees() + (MAX_LAT * 2)));
            super._degree *= -1;
            //HACK: can cause some errors in UI
            if (last == super.getDegrees() && Math.abs(_degree) == MAX_LAT) {
                super._minutes = 0;
                break;
            }
        }
        while (super.getDegrees() > MAX_LAT) {
            double last = super.getDegrees();
            super._degree = (int) Math.floor((super.getDegrees() - (MAX_LAT * 2)));
            super._degree *= -1;
            //HACK: can cause some errors in UI
            if (last == super.getDegrees() && Math.abs(_degree) == MAX_LAT) {
                super._minutes = 0;
                break;
            }
        }

        if (super.getDegrees() < MIN_LAT || super.getDegrees() > MAX_LAT) {
            throw new ArgumentOutOfRangeException("degrees", "Latitude values cannot be less than -90 or greater than 90");
        }
    }

    /**
     * 
     * @param degree_whole
     * @param minutes
     * @throws ArgumentOutOfRangeException 
     */
    public Latitude(int degree_whole, double minutes) throws ArgumentOutOfRangeException {
        super(degree_whole, minutes);

        while (super.getDegrees() < MIN_LAT) {
            super._degree = (int) Math.floor((super.getDegrees() + (MAX_LAT * 2)));
        }
        while (super.getDegrees() > MAX_LAT) {
            super._degree = (int) Math.floor((super.getDegrees() - (MAX_LAT * 2)));
        }

        if (super.getDegrees() < MIN_LAT || super.getDegrees() > MAX_LAT) {
            throw new ArgumentOutOfRangeException("degrees", "Latitude values cannot be less than -90 or greater than 90");
        }
    }

    /**
     * 
     * @return 
     */
    @Override
    public CardinalPoints getCardinalMark() {
        return super.getDegrees() >= 0 ? CardinalPoints.N : CardinalPoints.S;
    }
}
