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
package com.artistech.vbs3;

import com.artistech.geo.Coordinate;
import com.artistech.geo.GridConversion;
import com.artistech.geo.GridConversionPoint;
import com.artistech.math.PointF;
import com.artistech.utils.HaltMonitor;
import com.artistech.utils.Mailbox;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public abstract class BroadcasterBaseImpl implements PositionBroadcaster {

    public abstract class BroadcasterThread extends Thread {

        protected final HaltMonitor monitor;
        protected final Mailbox<Vbs3Protos.Position> messages;

        /**
         * Constructor
         */
        public BroadcasterThread() {
            monitor = new HaltMonitor();
            messages = new Mailbox<>();
        }

        /**
         * Thread function.
         */
        @Override
        public final void run() {
            while (!monitor.isHalted()) {
                ArrayList<Vbs3Protos.Position> msgs = messages.getMessages();
                if (msgs != null && !msgs.isEmpty()) {
                    for (Vbs3Protos.Position pos : msgs) {
                        broadcastPosition(initPosition(pos));
                    }
                } else {
                    halt();
                }
            }
        }

        /**
         * Halt the thread.
         */
        public final void halt() {
            monitor.halt();
        }

        /**
         * Submit a message to be broadcast.
         *
         * @param pos
         */
        public final void submit(Vbs3Protos.Position pos) {
            messages.addMessage(pos);
        }

        /**
         * Perform broadcast.
         *
         * @param pos
         */
        protected abstract void broadcastPosition(Vbs3Protos.Position pos);

        /**
         * Pre-process the position object if necessary.
         *
         * @param pos
         * @return
         */
        protected Vbs3Protos.Position initPosition(Vbs3Protos.Position pos) {
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

            if (getDoGridConversion()) {
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

            return pos;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BroadcasterBaseImpl.class.getName());
    private final GridConversion CONVERSION;
    private boolean _conversionPointInitialized;
    protected final AtomicBoolean DO_GRID_CONVERSION;

    /**
     * Default Constructor
     */
    public BroadcasterBaseImpl() {
        //initialize all necessary objects
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

    /**
     * See if we are to do the grid conversion.
     *
     * @return
     */
    @Override
    public final boolean getDoGridConversion() {
        return DO_GRID_CONVERSION.get();
    }

    /**
     * Set the minimum conversion point.
     *
     * @param value
     */
    @Override
    public final void setMinGridConversionPoint(GridConversionPoint value) {
        CONVERSION.setMin(value);
    }

    /**
     * Set the maximum conversion point.
     *
     * @param value
     */
    @Override
    public final void setMaxGridConversionPoint(GridConversionPoint value) {
        CONVERSION.setMax(value);
    }

    /**
     * Get the minimum conversion point.
     *
     * @return
     */
    @Override
    public final GridConversionPoint getMinGridConversionPoint() {
        return CONVERSION.getMin();
    }

    /**
     * Get the maximum conversion point.
     *
     * @return
     */
    @Override
    public final GridConversionPoint getMaxGridConversionPoint() {
        return CONVERSION.getMax();
    }
}
