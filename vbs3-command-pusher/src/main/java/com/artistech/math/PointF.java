/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.math;

/**
 *
 * @author matta
 */
public class PointF extends Point<Float> {

    public PointF() {
        this(0, 0);
    }

    public PointF(java.awt.Point pt) {
        this(pt.x, pt.y);
    }
    
    public PointF(float x, float y) {
        super(x, y);
    }

    public double distanceTo(PointD other) {
        return Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
    }
}
