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

import com.artistech.math.PointF;

/**
 * Class providing functionality for mapping a screen point into world space.
 *
 * @author matta
 */
public class GridConversion {

    private GridConversionPoint min;
    private GridConversionPoint max;

    /**
     * Constructor.
     *
     * @param min
     * @param max
     */
    public GridConversion(GridConversionPoint min, GridConversionPoint max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Constructor.
     */
    public GridConversion() {

    }

    /**
     * Get the minimum conversion point.
     *
     * @return
     */
    public final GridConversionPoint getMin() {
        return min;
    }

    /**
     * Set the minimum conversion point.
     *
     * @param value
     */
    public final void setMin(GridConversionPoint value) {
        min = value;
    }

    /**
     * Get the maximum conversion point.
     *
     * @return
     */
    public final GridConversionPoint getMax() {
        return max;
    }

    /**
     * Set the maximum conversion point.
     *
     * @param value
     */
    public final void setMax(GridConversionPoint value) {
        max = value;
    }

    /**
     * Convert a screen point to world coordinates.
     *
     * @param pt
     * @return
     */
    public Coordinate convert(PointF pt) {
        return GridConversionPoint.convert(min, max, pt);
    }

    /**
     * Convert a world coordinate to screen point.
     *
     * @param pt
     * @return
     */
    public PointF convert(Coordinate pt) {
        return GridConversionPoint.convert(min, max, pt);
    }
}
