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

import com.artistech.utils.ArgumentOutOfRangeException;

/**
 * Latitude is built to bounce back.
 *
 * @author matta
 */
public class Latitude extends CoordinateBase {

    public static final int MAX_LAT = 90;
    public static final int MIN_LAT = -90;

    /**
     * Constructor.
     */
    public Latitude() {
        this(0);
    }

    /**
     * Constructor.
     *
     * @param degrees
     */
    public Latitude(double degrees) {
        super(degrees);

        while (super.getDegrees() < MIN_LAT * 2) {
            super.setDegrees(super.getDegrees() + (MAX_LAT * 2));
            super.factor *= -1;
        }
        while (super.getDegrees() > MAX_LAT * 2) {
            super.setDegrees(super.getDegrees() - (MAX_LAT * 2));
            super.factor *= -1;
        }

        while (super.getDegrees() < MIN_LAT) {
            super._degree = Math.abs((int) Math.floor((super.getDegrees() + (MAX_LAT * 2))));

            if (Math.abs(super._minutes) > 0) {
                super._minutes = 1.0 - Math.abs(super._minutes);
            }
        }
        while (super.getDegrees() > MAX_LAT) {
            super._degree = Math.abs((int) Math.ceil((super.getDegrees() - (MAX_LAT * 2))));

            //HACK: can cause some errors in UI
            if (Math.abs(super._minutes) > 0) {
                super._minutes = 1.0 - Math.abs(super._minutes);
            }
        }
    }

    /**
     * Constructor.
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
     * Get the cardinal point.
     * 
     * Basically hemisphere.
     *
     * @return
     */
    @Override
    public CardinalPoints getCardinalMark() {
        return super.getDegrees() >= 0 ? CardinalPoints.N : CardinalPoints.S;
    }
}
