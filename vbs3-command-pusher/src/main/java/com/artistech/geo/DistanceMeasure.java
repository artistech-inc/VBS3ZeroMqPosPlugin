/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.geo;

import java.io.Serializable;

/**
 *
 * @author matta
 */
public class DistanceMeasure implements Serializable {

    public static final double KM_TO_MILES = 1.609344;
    public static final double MILES_TO_NAUTICAL = 0.868976;
    public static final double NAUTICAL_TO_MILES = 1.0 / MILES_TO_NAUTICAL;
    
    public static final DistanceMeasure RADIUS_OF_EARTH = new DistanceMeasure(3963.1676, DistanceUnit.MILE, true);

    private double _distance;
    private DistanceUnit _unit;
    private final boolean _readonly;

    /**
     * 
     * @param distance
     * @param unit
     * @param readonly 
     */
    private DistanceMeasure(double distance, DistanceUnit unit, boolean readonly) {
        _distance = distance;
        _unit = unit;
        _readonly = readonly;
    }

    /**
     * 
     */
    public DistanceMeasure() {
        _readonly = false;
        _unit = DistanceUnit.UNSPECIFIED;
        _distance = 0;
    }

    /**
     * 
     * @param distance
     * @param unit 
     */
    public DistanceMeasure(double distance, DistanceUnit unit) {
        this(distance, unit, false);
    }

    /**
     * 
     * @return 
     */
    public double getDistance() {
        return _distance;
    }

    /**
     * 
     * @param value 
     */
    public void setDistance(double value) {
        _distance = value;
    }

    /**
     * 
     * @return 
     */
    public DistanceUnit getUnit() {
        return _unit;
    }

    /**
     * 
     * @param value 
     */
    public void setUnit(DistanceUnit value) {
        if (_unit == DistanceUnit.UNSPECIFIED) {
            _unit = value;
        }
    }

    /**
     * 
     * @return 
     */
    public DistanceMeasure toMiles() {
        double distance = _distance;
        switch (_unit) {
            case KILOMETER:
                distance = distance / KM_TO_MILES;
                break;
            case MILE:
                break;
            case NAUTICAL_MILE:
                distance = distance / MILES_TO_NAUTICAL;
                break;
        }
        return new DistanceMeasure(distance, DistanceUnit.MILE);
    }

    /**
     * 
     * @return 
     */
    public DistanceMeasure toKilometers() {
        double distance = _distance;
        switch (_unit) {
            case KILOMETER:
                break;
            case MILE:
                distance = distance * KM_TO_MILES;
                break;
            case NAUTICAL_MILE:
                distance = distance / MILES_TO_NAUTICAL;    //miles
                distance = distance * KM_TO_MILES;          //km
                break;
        }
        return new DistanceMeasure(distance, DistanceUnit.KILOMETER);
    }

    /**
     * 
     * @return 
     */
    public DistanceMeasure toNauticalMiles() {
        double distance = _distance;
        switch (_unit) {
            case KILOMETER:
                distance = distance / KM_TO_MILES;          //miles
                distance = distance * MILES_TO_NAUTICAL;    //nautical
                break;
            case MILE:
                distance = distance / MILES_TO_NAUTICAL;
                break;
            case NAUTICAL_MILE:
                break;
        }
        return new DistanceMeasure(distance, DistanceUnit.NAUTICAL_MILE);
    }
}
