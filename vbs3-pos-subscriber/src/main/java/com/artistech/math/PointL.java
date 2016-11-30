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
 * Represents a point of long values.
 *
 * @author matta
 */
public class PointL extends Point<Long> {

    /**
     * Constructor.
     */
    public PointL() {
        this(0, 0);
    }

    /**
     * Constructor.
     *
     * @param pt
     */
    public PointL(java.awt.Point pt) {
        this((long) pt.x, (long) pt.y);
    }

    /**
     * Constructor.
     *
     * @param x
     * @param y
     */
    public PointL(long x, long y) {
        super(x, y);
    }

    /**
     * Calculate distance.
     *
     * @param other
     * @return
     */
    public double DistanceTo(PointL other) {
        return Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
    }
}
