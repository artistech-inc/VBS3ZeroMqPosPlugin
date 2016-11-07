/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.math;

import java.io.Serializable;

/**
 *
 * @author matta
 * @param <T>
 */
public class Point<T> implements Serializable {

    private T _x;
    private T _y;

    public Point(T x, T y) {
        _x = x;
        _y = y;
    }

    public T getX() {
        return _x;
    }

    public T getY() {
        return _y;
    }
    
    public void setX(T value) {
        _x = value;
    }

    public void setY(T value) {
        _y = value;
    }

    @Override
    public String toString() {
        return _x.getClass().getName() + ": (" + _x.toString() + ", " + _y.toString() + ")";
    }

}
