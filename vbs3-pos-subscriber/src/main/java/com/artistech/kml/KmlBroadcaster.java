/*
 * Copyright 2015 ArtisTech, Inc.
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
package com.artistech.kml;

import com.artistech.geo.Coordinate;
import com.artistech.geo.GridConversion;
import com.artistech.geo.GridConversionPoint;
import com.artistech.math.PointF;
import com.artistech.vbs3.PositionBroadcaster;
import com.artistech.vbs3.Vbs3Protos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public class KmlBroadcaster implements PositionBroadcaster {

    private static final Logger LOGGER = Logger.getLogger(KmlBroadcaster.class.getName());
    private final GridConversion CONVERSION;
    private boolean _conversionPointInitialized;
    private final AtomicBoolean DO_GRID_CONVERSION;

    private static final HashMap<String, Vbs3Protos.Position> CURRENT;

    public KmlBroadcaster() {
        DO_GRID_CONVERSION = new AtomicBoolean(true);
        CONVERSION = new GridConversion();
        GridConversionPoint gcp = new GridConversionPoint();
        gcp.setCoordinate(Coordinate.MIN);
        gcp.setPoint(new PointF(0, 0));
        CONVERSION.setMin(gcp);

        gcp = new GridConversionPoint();
        gcp.setCoordinate(Coordinate.MAX);
        gcp.setPoint(new PointF(1, 1));
        CONVERSION.setMax(gcp);
    }

    static {
        CURRENT = new HashMap<>();
    }

    /**
     * See if we are to do the grid conversion.
     *
     * @return
     */
    @Override
    public boolean getDoGridConversion() {
        return DO_GRID_CONVERSION.get();
    }

//    /**
//     * Set if we are to do the grid conversion.
//     *
//     * @param value
//     */
//    @Override
//    public void setDoGridConversion(boolean value) {
////        DO_GRID_CONVERSION.set(value);
//    }

    /**
     * Set the minimum conversion point.
     *
     * @param value
     */
    @Override
    public void setMinGridConversionPoint(GridConversionPoint value) {
        CONVERSION.setMin(value);
    }

    /**
     * Set the maximum conversion point.
     *
     * @param value
     */
    @Override
    public void setMaxGridConversionPoint(GridConversionPoint value) {
        CONVERSION.setMax(value);
    }

    /**
     * Get the minimum conversion point.
     *
     * @return
     */
    @Override
    public GridConversionPoint getMinGridConversionPoint() {
        return CONVERSION.getMin();
    }

    /**
     * Get the maximum conversion point.
     *
     * @return
     */
    @Override
    public GridConversionPoint getMaxGridConversionPoint() {
        return CONVERSION.getMax();
    }

    @Override
    public void broadcastPosition(Vbs3Protos.Position pos) {
        if (!_conversionPointInitialized) {
            //initialize the MAX point only once.
            //max point is 2x the center point.
            float worldCenterX = pos.getWorldCenterX() * 2.0f;
            float worldCenterY = pos.getWorldCenterY() * 2.0f;

            CONVERSION.getMin().setPoint(new PointF(0, worldCenterY));
            CONVERSION.getMax().setPoint(new PointF(worldCenterX, 0));

            _conversionPointInitialized = true;

            LOGGER.log(Level.INFO, "MAX POINT: {0}", CONVERSION.getMax().getPoint().toString());
            LOGGER.log(Level.INFO, "MIN POINT: {0}", CONVERSION.getMin().getPoint().toString());
        }

        if (DO_GRID_CONVERSION.get()) {
            //do grid conversion on pos
            LOGGER.log(Level.FINEST, "X {0}, Y: {1}", new Object[]{pos.getX(), pos.getY()});

            PointF p = new PointF(pos.getX(), pos.getY());
            Coordinate convert = CONVERSION.convert(p);
            Vbs3Protos.Position.Builder toBuilder = pos.toBuilder();
            toBuilder.setX((float) convert.getLongitude().getDegrees());
            toBuilder.setY((float) convert.getLatitude().getDegrees());
            pos = toBuilder.build();

            LOGGER.log(Level.FINEST, "LON: {0}, LAT: {1}", new Object[]{pos.getX(), pos.getY()});
        }

        //push as the current position
        synchronized (CURRENT) {
            CURRENT.put(pos.getId(), pos);
        }
    }

    @Override
    public void initialize() {
        //nothing to initialize for this class
    }

    public static Collection<Vbs3Protos.Position> getPositions() {
        synchronized (CURRENT) {
            return new ArrayList<>(CURRENT.values());
        }
    }
}
