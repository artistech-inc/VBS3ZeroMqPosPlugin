/*
 * Copyright 2015 ArtisTech, Inc.
 */

package com.artistech.math;

/**
 *
 * @author matta
 */
public class StrangeAttactor {
    private double _x;
    private double _y;
    
    public StrangeAttactor(double x, double y) {
        _x = x;
        _y = y;
    }

    public StrangeAttactor(PointD pt) {
        this(pt.getX(), pt.getY());
    }
    
    public PointD next() {
        double x = _y + 1 - (1.4 * Math.pow(_x, 2));
        double y = 0.3 * _x;
        
        _x = x;
        _y = y;
        
        return new PointD(x, y);
    }
}
