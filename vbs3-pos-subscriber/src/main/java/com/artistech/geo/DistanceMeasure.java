/*
 * Copyright 2015-2016 ArtisTech, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artistech.geo;

import java.io.Serializable;

/**
 * Class for representing distance.
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
    private final boolean READ_ONLY;

    /**
     *
     * @param distance
     * @param unit
     * @param readonly
     */
    private DistanceMeasure(double distance, DistanceUnit unit, boolean readonly) {
        _distance = distance;
        _unit = unit;
        READ_ONLY = readonly;
    }
    
    /**
     * Constructor
     */
    public DistanceMeasure() {
        READ_ONLY = false;
        _unit = DistanceUnit.UNSPECIFIED;
        _distance = 0;
    }

    /**
     * Constructor.
     *
     * @param distance
     * @param unit
     */
    public DistanceMeasure(double distance, DistanceUnit unit) {
        this(distance, unit, false);
    }

    /**
     * Get the distance.
     *
     * @return
     */
    public double getDistance() {
        return _distance;
    }

    /**
     * Set the distance.
     *
     * @param value
     */
    public void setDistance(double value) {
        _distance = value;
    }

    /**
     * Get the unit of measure.
     *
     * @return
     */
    public DistanceUnit getUnit() {
        return _unit;
    }

    /**
     * Set the unit of measure.
     *
     * @param value
     */
    public void setUnit(DistanceUnit value) {
        if (_unit == DistanceUnit.UNSPECIFIED) {
            _unit = value;
        }
    }

    /**
     * Convert the current distance to miles.
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
     * Convert the current distance to kilometers.
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
     * Convert the current distance to nautical miles.
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

//    public static void main(String[] args) {
//        System.out.println(1.0 / MILES_TO_NAUTICAL);
//    }
}
