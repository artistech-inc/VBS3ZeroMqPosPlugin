/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.math;

/**
 *
 * @author matta
 */
public class PointD extends Point<Double> {
    public PointD() {
        this(0, 0);
    }

    public PointD(java.awt.Point pt) {
        this((double)pt.x, (double)pt.y);
    }

    public PointD(double x, double y) {
        super(x, y);
    }
    
    public double distanceTo(PointD other) {
        return Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
    }
}
