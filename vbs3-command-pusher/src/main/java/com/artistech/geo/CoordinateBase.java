/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.geo;

import com.artistech.math.AngleMeasure;
import com.artistech.utils.ArgumentOutOfRangeException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

/**
 *
 * @author matta
 */
public abstract class CoordinateBase implements Serializable {

    protected int _degree;
    protected double _minutes;

    /**
     * 
     * @param degrees 
     */
    public CoordinateBase(double degrees) {
        int factor = degrees < 0 ? -1 : 1;
        _degree = (int) Math.floor(Math.abs(degrees)) * factor;
        _minutes = Math.abs(degrees) - Math.abs(_degree);
    }

    /**
     * 
     * @param degree
     * @param minutes
     * @throws ArgumentOutOfRangeException 
     */
    public CoordinateBase(int degree, double minutes) throws ArgumentOutOfRangeException {
        _degree = degree;
        if (minutes >= 1 || minutes < 0) {
            throw new ArgumentOutOfRangeException("minutes", "minutes cannot be greater than or equal to 1 or less than 0");
        }
        _minutes = minutes;
    }

    /**
     * 
     * @return 
     */
    @JsonIgnore
    public int getWholeDegree() {
        return _degree;
    }

    /**
     * 
     * @return 
     */
    @JsonIgnore
    public double getMinutes() {
        return _minutes;
    }

    /**
     * 
     * @return 
     */
    public double getDegrees() {
        double factor = _degree < 0 ? -1.0 : 1.0;
        return factor * (Math.abs((double) _degree + (_minutes * factor)));
    }

    /**
     * 
     * @param value 
     */
    public void setDegrees(double value) {
        double degrees = value;
        _degree = (int) Math.floor(degrees);
        _minutes = Math.abs(degrees) - Math.abs(_degree);
   }

    /**
     * 
     * @return 
     */
    @JsonIgnore
    public double getRadians() {
        return AngleMeasure.deg2rad(this.getDegrees());
    }

    /**
     * 
     * @return 
     */
    @JsonIgnore
    public abstract CardinalPoints getCardinalMark();
}
