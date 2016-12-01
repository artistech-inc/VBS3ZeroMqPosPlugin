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

/**
 * Represents strange attractor function.
 *
 * @author matta
 */
public class StrangeAttactor {

    private double _x;
    private double _y;

    /**
     * Constructor.
     *
     * @param x
     * @param y
     */
    public StrangeAttactor(double x, double y) {
        _x = x;
        _y = y;
    }

    /**
     * Constructor.
     *
     * @param pt
     */
    public StrangeAttactor(PointD pt) {
        this(pt.getX(), pt.getY());
    }

    /**
     * Get the next point.
     *
     * @return
     */
    public PointD next() {
        double x = _y + 1 - (1.4 * Math.pow(_x, 2));
        double y = 0.3 * _x;

        _x = x;
        _y = y;

        return new PointD(x, y);
    }
}
