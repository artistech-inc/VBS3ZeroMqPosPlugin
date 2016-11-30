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

import com.artistech.math.AngleMeasure;
import com.artistech.math.AngleUnit;
import com.artistech.utils.ArgumentOutOfRangeException;
import com.artistech.utils.Random;
import java.io.Serializable;

/**
 * Coordinate Data.
 *
 * Latitude, Longitude, and Altitude
 *
 * @author matta
 */
public class Coordinate implements Serializable {

    public static final double NAUTICAL_MILES_PER_DEGREE_LATITUDE = 60.0;
    public static final double MINUTES_PER_DEGREE = 60.0;
    public static final Coordinate MAX = new Coordinate((double) Longitude.MAX_LON, (double) Latitude.MAX_LAT, true);
    public static final Coordinate MIN = new Coordinate((double) Longitude.MIN_LON, (double) Latitude.MIN_LAT, true);

    private boolean _readonly;
    private Latitude _lat;
    private Longitude _lon;
    private DistanceMeasure _alt;

    /**
     *
     * @param lon
     * @param lat
     * @throws ArgumentOutOfRangeException
     */
    public Coordinate(double lon, double lat) throws ArgumentOutOfRangeException {
        _lat = new Latitude(lat);
        _lon = new Longitude(lon);
        _readonly = false;
    }

    /**
     *
     */
    public Coordinate() {
        _readonly = false;
        _lat = new Latitude(0);
        _lon = new Longitude(0);
    }

    /**
     *
     * @param lon
     * @param lat
     * @param readonly
     */
    protected Coordinate(double lon, double lat, boolean readonly) {
        _readonly = readonly;
        _lat = new Latitude(lat);
        _lon = new Longitude(lon);
    }

    /**
     *
     * @param lon
     * @param lat
     * @param readonly
     */
    protected Coordinate(Longitude lon, Latitude lat, boolean readonly) {
        _lon = lon;
        _lat = lat;
        _readonly = readonly;
    }

    /**
     *
     * @param lon
     * @param lat
     */
    public Coordinate(Longitude lon, Latitude lat) {
        this(lon, lat, false);
    }

    /**
     *
     * @return
     */
    public Longitude getLongitude() {
        return _lon;
    }

    /**
     *
     * @param value
     */
    public void setLongitude(Longitude value) {
        if (!_readonly) {
            _lon = value;
        }
    }

    /**
     *
     * @param value
     */
    public void setLatitude(Latitude value) {
        if (!_readonly) {
            _lat = value;
        }
    }

    /**
     *
     * @return
     */
    public Latitude getLatitude() {
        return _lat;
    }

    /**
     *
     * @return
     */
    public DistanceMeasure getAltitude() {
        return _alt;
    }

    /**
     *
     * @param value
     */
    public void setAltitude(DistanceMeasure value) {
        if (!_readonly) {
            _alt = value;
        }
    }

    /**
     * http://stackoverflow.com/questions/389211/geospatial-coordinates-and-distance-in-kilometers
     *
     * @param coord1
     * @param coord2
     * @param unit
     * @return
     */
    public static DistanceMeasure distance(Coordinate coord1, Coordinate coord2, DistanceUnit unit) {
        double theta = coord1.getLongitude().getDegrees() - coord2.getLongitude().getDegrees();
        double dist = Math.sin(coord1.getLatitude().getRadians()) * Math.sin(coord2.getLatitude().getRadians()) + Math.cos(coord1.getLatitude().getRadians()) * Math.cos(coord2.getLatitude().getRadians()) * Math.cos(AngleMeasure.deg2rad(theta));
        dist = Math.acos(dist);
        dist = AngleMeasure.rad2deg(dist);
        dist = dist * MINUTES_PER_DEGREE * DistanceMeasure.NAUTICAL_TO_MILES;
        if (unit == DistanceUnit.KILOMETER) {
            dist = dist * DistanceMeasure.KM_TO_MILES;
        } else if (unit == DistanceUnit.NAUTICAL_MILE) {
            dist = dist * DistanceMeasure.MILES_TO_NAUTICAL;
        }
        return new DistanceMeasure(dist, unit);
    }

    /**
     * Calculate the distance to the given coordinate
     *
     * @param coord
     * @param unit
     * @return
     */
    public DistanceMeasure distance(Coordinate coord, DistanceUnit unit) {
        return distance(this, coord, unit);
    }

    /**
     * Get the distance between longitude lines at a given latitude
     * http://answers.google.com/answers/threadview?id=577262
     *
     * @param lat
     * @param unit
     * @return
     */
    public static DistanceMeasure distanceBetweenLongitude(Latitude lat, DistanceUnit unit) {
        double dist = (Math.PI / 180.0) * DistanceMeasure.RADIUS_OF_EARTH.getDistance() * Math.cos(lat.getDegrees());

        if (unit == DistanceUnit.KILOMETER) {
            dist *= DistanceMeasure.KM_TO_MILES;
        } else if (unit == DistanceUnit.NAUTICAL_MILE) {
            dist *= DistanceMeasure.MILES_TO_NAUTICAL;
        }

        return new DistanceMeasure(dist, unit);
    }

    /**
     * Get the distance between latitude lines
     *
     * @param unit
     * @return This value is constant, only the value of the unit varies
     */
    public static DistanceMeasure distanceBetweenLatitude(DistanceUnit unit) {
        double dist = NAUTICAL_MILES_PER_DEGREE_LATITUDE;
        //nauticle miles per degree latitude
        if (unit == DistanceUnit.KILOMETER) {
            dist = dist / DistanceMeasure.MILES_TO_NAUTICAL;
            dist = dist * DistanceMeasure.KM_TO_MILES;
        } else if (unit == DistanceUnit.MILE) {
            dist = dist / DistanceMeasure.MILES_TO_NAUTICAL;
        }
        return new DistanceMeasure(dist, unit);
    }

    /**
     * Get a random bearing. Bearing is clockwise with 0 being north.
     *
     * @param unit
     * @return
     */
    public static AngleMeasure randomBearing(AngleUnit unit) {
        return AngleMeasure.getRandomAngle(unit);
    }

    /**
     * Bearing is clockwise with 0 being north.
     * http://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param distance
     * @param bearing
     * @return
     */
    public Coordinate add(DistanceMeasure distance, AngleMeasure bearing) {
        double d = distance.toMiles().getDistance();
        double R = DistanceMeasure.RADIUS_OF_EARTH.toMiles().getDistance();
        double lat2 = Math.asin(Math.sin(this.getLatitude().getRadians()) * Math.cos(d / R)
                + Math.cos(this.getLatitude().getRadians()) * Math.sin(d / R) * Math.cos(bearing.toRadians().getAngle()));
        double lon2 = this.getLongitude().getRadians() +
                Math.atan2(Math.sin(bearing.toRadians().getAngle()) * Math.sin(d / R) * Math.cos(this.getLatitude().getRadians()),
                            Math.cos(d / R) - Math.sin(this.getLatitude().getRadians()) * Math.sin(lat2));
        AngleMeasure lat2_angle = new AngleMeasure(lat2, AngleUnit.RADIANS);
        AngleMeasure lon2_angle = new AngleMeasure(lon2, AngleUnit.RADIANS);
        return new Coordinate(new Longitude(lon2_angle.toDegrees().getAngle()), new Latitude(lat2_angle.toDegrees().getAngle()));
    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    public static Coordinate getRandomCoordinate(Coordinate min, Coordinate max) {
        double diff = Math.max(min.getLatitude().getDegrees(), max.getLatitude().getDegrees()) - Math.min(min.getLatitude().getDegrees(), max.getLatitude().getDegrees());
        double factor = Random.nextDouble();
        double latdeg = Math.min(min.getLatitude().getDegrees(), max.getLatitude().getDegrees()) + (diff * factor);

        diff = Math.max(min.getLongitude().getDegrees(), max.getLongitude().getDegrees()) - Math.min(min.getLongitude().getDegrees(), max.getLongitude().getDegrees());
        factor = Random.nextDouble();
        double londeg = Math.min(min.getLongitude().getDegrees(), max.getLongitude().getDegrees()) + (diff * factor);

        Latitude lat = new Latitude(latdeg);
        Longitude lon = new Longitude(londeg);

        return new Coordinate(lon, lat);
    }

    /**
     *
     * @param center
     * @param max_radius
     * @return
     */
    public static Coordinate getRandomCoordinate(Coordinate center, DistanceMeasure max_radius) {
        AngleMeasure angle = getRandomBearing(AngleUnit.DEGREES);
        double dist = com.artistech.utils.Random.nextDouble() * max_radius.getDistance();
        Coordinate retval = center.add(new DistanceMeasure(dist, max_radius.getUnit()), angle);
        return retval;
    }

    /**
     *
     * @param unit
     * @return
     */
    public static AngleMeasure getRandomBearing(AngleUnit unit) {
        return AngleMeasure.getRandomAngle(unit);
    }

    /**
     * http://www.movable-type.co.uk/scripts/latlong.html
     * @param pt1
     * @param pt2
     * @return 
     */
    public static AngleMeasure calculateBearing(Coordinate pt1, Coordinate pt2) {
       double deltaLamda = pt2.getLongitude().getRadians() - pt1.getLongitude().getRadians();
       double y = Math.sin(deltaLamda) * Math.cos(pt2.getLatitude().getRadians());
       double x = (Math.cos(pt1.getLatitude().getRadians()) * Math.sin(pt2.getLatitude().getRadians())) -
                    (Math.sin(pt1.getLatitude().getRadians()) * Math.cos(pt2.getLatitude().getRadians()) * Math.cos(deltaLamda));
       double brng = Math.atan2(y, x);
       AngleMeasure am = new AngleMeasure(brng, AngleUnit.RADIANS);
//    var y = Math.sin(λ2-λ1) * Math.cos(φ2);
//    var x = Math.cos(φ1)*Math.sin(φ2) -
//            Math.sin(φ1)*Math.cos(φ2)*Math.cos(λ2-λ1);
//    var brng = Math.atan2(y, x).toDegrees();
        return am.toDegrees();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "Lat: " + getLatitude().getDegrees() + ",Lon: " + getLongitude().getDegrees();
    }
}
