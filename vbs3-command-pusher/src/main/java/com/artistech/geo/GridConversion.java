/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.geo;

import com.artistech.math.PointF;

/**
 *
 * @author matta
 */
public class GridConversion {

    private GridConversionPoint min;
    private GridConversionPoint max;

    public GridConversion(GridConversionPoint min, GridConversionPoint max) {
        this.min = min;
        this.max = max;
    }

    /**
     * 
     */
    public GridConversion() {

    }

    /**
     * 
     * @return 
     */
    public final GridConversionPoint getMin() {
        return min;
    }

    /**
     * 
     * @param value 
     */
    public final void setMin(GridConversionPoint value) {
        min = value;
    }

    /**
     * 
     * @return 
     */
    public final GridConversionPoint getMax() {
        return max;
    }

    /**
     * 
     * @param value 
     */
    public final void setMax(GridConversionPoint value) {
        max = value;
    }

    /**
     * 
     * @param pt
     * @return 
     */
    public Coordinate convert(PointF pt) {
        return GridConversionPoint.convert(min, max, pt);
    }

    /**
     * 
     * @param pt
     * @return 
     */
    public PointF convert(Coordinate pt) {
        return GridConversionPoint.convert(min, max, pt);
    }
}
