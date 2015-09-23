/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.math;

/**
 *
 * @author matta
 */
public class PointL extends Point<Long> {
    public PointL() {
        this(0, 0);
    }

    public PointL(java.awt.Point pt) {
        this((long)pt.x, (long)pt.y);
    }

    public PointL(long x, long y) {
        super(x, y);
    }
    public double DistanceTo(PointL other) {
        return Math.sqrt(Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2));
    }
}
