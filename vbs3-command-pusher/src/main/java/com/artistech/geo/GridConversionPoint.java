/*
 * Copyright 2015 ArtisTech, Inc.
 */
package com.artistech.geo;

import com.artistech.math.PointF;
import com.artistech.utils.ArgumentOutOfRangeException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public class GridConversionPoint {

    private final Coordinate _coord;
    private final PointF _point;

    /**
     * 
     */
    public GridConversionPoint() {
        _coord = new Coordinate();
        _point = new PointF(0, 0);
    }

    /**
     * 
     * @param coord
     * @param point 
     */
    public GridConversionPoint(Coordinate coord, PointF point) {
        _coord = coord;
        _point = point;
    }

    /**
     * 
     * @return 
     */
    public Coordinate getCoordinate() {
        return _coord;
    }

    /**
     * 
     * @param value 
     */
    public void setCoordinate(Coordinate value) {
        _coord.setAltitude(value.getAltitude());
        _coord.setLatitude(value.getLatitude());
        _coord.setLongitude(value.getLongitude());
    }

    /**
     * 
     * @return 
     */
    public PointF getPoint() {
        return _point;
    }

    /**
     * 
     * @param value 
     */
    public void setPoint(PointF value) {
        _point.setX(value.getX());
        _point.setY(value.getY());
    }

    /**
     * 
     * @return 
     */
    public GridConversionPoint copy() {
        GridConversionPoint ret = new GridConversionPoint();
        ret.setCoordinate(_coord);
        ret.setPoint(_point);
        return ret;
    }

    /**
     * @param min
     * @param max
     * @param pt
     * @return
     */
    public static PointF convert(GridConversionPoint min, GridConversionPoint max, Coordinate pt) {
        Coordinate minCoord = min.getCoordinate();
        Coordinate maxCoord = max.getCoordinate();

        double xDiff = Math.abs(maxCoord.getLongitude().getDegrees() - minCoord.getLongitude().getDegrees());
        double yDiff = Math.abs(maxCoord.getLatitude().getDegrees() - minCoord.getLatitude().getDegrees());

        double f = Math.abs(pt.getLongitude().getDegrees() - minCoord.getLongitude().getDegrees());
//        System.out.println("f: " + f);
        double f1 = f / xDiff;
//        System.out.println("f': " + f1);
        double xDiff1 = Math.abs(min.getPoint().getX() - max.getPoint().getX());
        double x = xDiff1 * f1;
//        System.out.println("xDiff1: " + xDiff1);
//        System.out.println("x: " + x);
        x += min.getPoint().getX();
//        System.out.println("x': " + x);

        f = Math.abs(pt.getLatitude().getDegrees() - minCoord.getLatitude().getDegrees());
//        System.out.println("f: " + f);
        f1 = f / yDiff;
//        System.out.println("f': " + f1);
        double yDiff1 = Math.abs(min.getPoint().getY() - max.getPoint().getY());
        double y = yDiff1 * f1;
//        System.out.println("yDiff1: " + yDiff1);
//        System.out.println("y: " + y);
        y += max.getPoint().getY();
//        System.out.println("y': " + y);

        PointF coord = new PointF((float) x, (float) y);
        return coord;
    }

    /**
     * This should work for all positive (>= 0) values of min/max
     *
     * @param min
     * @param max
     * @param pt
     * @return
     */
    public static Coordinate convert(GridConversionPoint min, GridConversionPoint max, PointF pt) {
        PointF minPt = min.getPoint();
        PointF maxPt = max.getPoint();

        float xDiff = Math.abs(maxPt.getX() - minPt.getX());
        float yDiff = Math.abs(maxPt.getY() - minPt.getY());

        float xScale = pt.getX() / xDiff;
        float yScale = pt.getY() / yDiff;

        Coordinate minC = min.getCoordinate();
        Coordinate maxC = max.getCoordinate();

        double latDiff = Math.abs(maxC.getLatitude().getDegrees() - minC.getLatitude().getDegrees());
        double lonDiff = Math.abs(maxC.getLongitude().getDegrees() - minC.getLongitude().getDegrees());

        latDiff *= yScale;
        lonDiff *= xScale;

        latDiff += minC.getLatitude().getDegrees();
        lonDiff += minC.getLongitude().getDegrees();

        Coordinate coord;
        try {
            coord = new Coordinate(lonDiff, latDiff);
            return coord;
        } catch (ArgumentOutOfRangeException ex) {
            Logger.getLogger(GridConversionPoint.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
