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
import com.artistech.utils.ArgumentOutOfRangeException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A point matching a screen point and world coordinate.
 *
 * @author matta
 */
public class GridConversionPoint {

    private final Coordinate _coord;
    private final PointF _point;

    /**
     * Constructor
     */
    public GridConversionPoint() {
        _coord = new Coordinate();
        _point = new PointF(0, 0);
    }

    /**
     * Constructor.
     *
     * @param coord
     * @param point
     */
    public GridConversionPoint(Coordinate coord, PointF point) {
        _coord = coord;
        _point = point;
    }

    /**
     * Get the coordinate.
     *
     * @return
     */
    public Coordinate getCoordinate() {
        return _coord;
    }

    /**
     * Set the coordinate.
     *
     * @param value
     */
    public void setCoordinate(Coordinate value) {
        _coord.setAltitude(value.getAltitude());
        _coord.setLatitude(value.getLatitude());
        _coord.setLongitude(value.getLongitude());
    }

    /**
     * Get the point.
     *
     * @return
     */
    public PointF getPoint() {
        return _point;
    }

    /**
     * Set the point.
     *
     * @param value
     */
    public void setPoint(PointF value) {
        _point.setX(value.getX());
        _point.setY(value.getY());
    }

    /**
     * Create a copy of this object.
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
     * Perform conversion from world coordinates to screen coordinates.
     *
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

        f = Math.abs(pt.getLatitude().getDegrees() - Math.max(minCoord.getLatitude().getDegrees(), maxCoord.getLatitude().getDegrees()));
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
     * Convert screen coordinate to world coordinate.
     * 
     * This should work for all positive (>= 0) values of min/max.
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

        //adapt for screen to world coordinates.
        //macC.lat is -90
        latDiff -= Math.max(minC.getLatitude().getDegrees(), maxC.getLatitude().getDegrees());
        //flip latitude
        latDiff *= -1.0;
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
