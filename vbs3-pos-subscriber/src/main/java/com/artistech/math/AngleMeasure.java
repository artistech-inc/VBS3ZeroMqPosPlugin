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
package com.artistech.math;

import java.io.Serializable;

/**
 * Represents the measure of an angle.
 *
 * @author matta
 */
public class AngleMeasure implements Serializable {

    public static final AngleMeasure MIN_RADIANS = new AngleMeasure(0.0, AngleUnit.RADIANS, true);
    public static final AngleMeasure MIN_DEGREES = new AngleMeasure(0.0, AngleUnit.DEGREES, true);
    public static final AngleMeasure MAX_RADIANS = new AngleMeasure(2 * Math.PI, AngleUnit.RADIANS, true);
    public static final AngleMeasure MAX_DEGREES = new AngleMeasure(360.0, AngleUnit.DEGREES, true);

    private boolean _readonly;
    private double _angle;
    private AngleUnit _unit;

    /**
     * Hidden read-only constructor.
     *
     * @param angle
     * @param unit
     * @param readonly
     */
    private AngleMeasure(double angle, AngleUnit unit, boolean readonly) {
        this(angle, unit);
        _readonly = readonly;
    }

    /**
     * Default Constructor.
     */
    public AngleMeasure() {
    }

    /**
     * Constructor.
     *
     * @param angle
     * @param unit
     */
    public AngleMeasure(double angle, AngleUnit unit) {
        _angle = angle;
        _unit = unit;
    }

    /**
     * Get the current angle value.
     *
     * @return
     */
    public double getAngle() {
        return _angle;
    }

    /**
     * Set the current angle value.
     *
     * @param value
     */
    public void setAngle(double value) {
        if (!_readonly) {
            _angle = value;
        }
    }

    /**
     * Get the current angle unit.
     *
     * @return
     */
    public AngleUnit getUnit() {
        return _unit;
    }

    /**
     * Set the current angle unit.
     *
     * @param value
     */
    public void setUnit(AngleUnit value) {
        _unit = value;
    }

    /**
     * Convert to radians.
     *
     * @return
     */
    public AngleMeasure toRadians() {
        if (_unit == AngleUnit.RADIANS) {
            return this.copy();
        } else {
            return new AngleMeasure(deg2rad(_angle), AngleUnit.RADIANS);
        }
    }

    /**
     * Convert to degrees.
     *
     * @return
     */
    public AngleMeasure toDegrees() {
        if (_unit == AngleUnit.DEGREES) {
            return this.copy();
        } else {
            return new AngleMeasure(rad2deg(_angle), AngleUnit.DEGREES);
        }
    }

    /**
     * Get a random angle.
     *
     * @param min
     * @param max
     * @param unit
     * @return
     */
    public static AngleMeasure getRandomAngle(double min, double max, AngleUnit unit) {
        double temp_min = min;
        double temp_max = max;
        if (unit == AngleUnit.RADIANS) {
            temp_min = rad2deg(temp_min);
            temp_max = rad2deg(temp_max);
        }

        int trunc_min = (int) Math.floor(temp_min);
        int trunc_max = (int) Math.floor(temp_max);

        //min degrees cannot equal 360 since that would then involve going back around again.
        if (trunc_min >= MIN_DEGREES.getAngle() && trunc_max >= MIN_DEGREES.getAngle() && trunc_min < MAX_DEGREES.getAngle() && trunc_max <= MAX_DEGREES.getAngle()) {
            double degree;
            if (trunc_min != trunc_max) {
                degree = com.artistech.utils.Random.nextInt(trunc_min, trunc_max);
            } else {
                degree = trunc_min;
            }
            double minutes = com.artistech.utils.Random.nextDouble();

            AngleMeasure measure = new AngleMeasure(degree + minutes, AngleUnit.DEGREES);
            if (unit == AngleUnit.RADIANS) {
                measure = measure.toRadians();
            }
            return measure;
        }
        return null;
    }

    /**
     * If the unit is radians, the doubles will be converted to degrees, then
     * truncated and these values will be used as the min/max values. A random
     * double value will also be added to further provide variation in the
     * angles. if the target unit is radians, then the degrees will be converted
     * back to radians for the return value.
     *
     * @param min
     * @param max
     * @param unit
     * @return
     */
    public static AngleMeasure getRandomAngle(AngleMeasure min, AngleMeasure max, AngleUnit unit) {
        double temp_min = min.toDegrees().getAngle();
        double temp_max = max.toDegrees().getAngle();

        AngleMeasure measure = getRandomAngle(temp_min, temp_max, AngleUnit.DEGREES);
        if (measure != null && unit == AngleUnit.RADIANS) {
            measure = measure.toRadians();
        }

        return measure;
    }

    /**
     * Get a random angle in the given units
     *
     * @param unit
     * @return
     */
    public static AngleMeasure getRandomAngle(AngleUnit unit) {
        AngleMeasure measure = getRandomAngle(MIN_DEGREES, MAX_DEGREES, AngleUnit.DEGREES);
        if (unit == AngleUnit.RADIANS) {
            measure = measure.toRadians();
        }
        return measure;
    }

    /**
     * Create a copy.
     *
     * @return
     */
    public AngleMeasure copy() {
        return new AngleMeasure(_angle, _unit);
    }

    /**
     * Convert degree to radian.
     *
     * @param degree
     * @return
     */
    public static double deg2rad(double degree) {
        return (degree * (Math.PI / 180.0));
    }

    /**
     * Convert radian to degree
     *
     * @param rad
     * @return
     */
    public static double rad2deg(double rad) {
        return ((rad / Math.PI) * 180.0);
    }
}
