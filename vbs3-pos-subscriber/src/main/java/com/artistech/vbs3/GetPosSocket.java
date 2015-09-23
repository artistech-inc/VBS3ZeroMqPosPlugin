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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class GetPosSocket extends WebSocketAdapter {

    public static class BroadcasterThread extends Thread {

        private final HaltMonitor monitor;
        private final Mailbox<Vbs3Protos.Position> messages;

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
        public void run() {
            while (!monitor.isHalted()) {
                ArrayList<Vbs3Protos.Position> msgs = messages.getMessages();
                if (msgs != null && !msgs.isEmpty()) {
                    for (Vbs3Protos.Position pos : msgs) {
                        broadcastPosition(pos);
                    }
                } else {
                    halt();
                }
            }
        }

        /**
         * Halt the thread.
         */
        public void halt() {
            monitor.halt();
        }

        /**
         * Submit a message to be broadcast.
         *
         * @param pos
         */
        public void submit(Vbs3Protos.Position pos) {
            messages.addMessage(pos);
        }

        /**
         * Perform broadcast.
         *
         * @param pos
         */
        private void broadcastPosition(Vbs3Protos.Position pos) {
            ArrayList<GetPosSocket> copy = new ArrayList<>();
            synchronized (INSTANCES) {
                copy.addAll(INSTANCES);
            }

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

//                PointF p2 = new PointF(0f, 0f);
//                Coordinate convert = CONVERSION.convert(p2);
//                LOGGER.log(Level.INFO, "TEST POINT1: {0}", p2.toString());
//                LOGGER.log(Level.INFO, "TEST POINT1: {0}", convert.toString());
//                p2 = new PointF(5000f, 5000f);
//                convert = CONVERSION.convert(p2);
//                LOGGER.log(Level.INFO, "TEST POINT2: {0}", p2.toString());
//                LOGGER.log(Level.INFO, "TEST POINT2: {0}", convert.toString());
//
//                p2 = new PointF(2500f, 2500f);
//                convert = CONVERSION.convert(p2);
//                LOGGER.log(Level.INFO, "TEST POINT3: {0}", p2.toString());
//                LOGGER.log(Level.INFO, "TEST POINT3: {0}", convert.toString());
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

            //send the data to all listeners
            byte[] data = pos.toByteArray();
            for (GetPosSocket sock : copy) {
                try {
                    sock.getSession().getRemote().sendBytesByFuture(ByteBuffer.wrap(data));
                } catch (org.eclipse.jetty.websocket.api.WebSocketException ex) {
                    LOGGER.log(Level.WARNING, "WebSocket error; removing socket from listeners: {0}", ex.getMessage());
                    synchronized (INSTANCES) {
                        INSTANCES.remove(sock);
                    }
                }
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(GetPosSocket.class.getName());

    private static final ArrayList<GetPosSocket> INSTANCES = new ArrayList<>();
    private static final BroadcasterThread BROADCASTER;
    private static final GridConversion CONVERSION;
    private static boolean _conversionPointInitialized;
    private static final AtomicBoolean DO_GRID_CONVERSION;

    /**
     * Static Constructor
     */
    static {
        //initialize all necessary objects
        DO_GRID_CONVERSION = new AtomicBoolean(false);
        BROADCASTER = new BroadcasterThread();
        BROADCASTER.setDaemon(true);
        CONVERSION = new GridConversion();
        GridConversionPoint gcp = new GridConversionPoint();
        gcp.setCoordinate(Coordinate.MIN);
        gcp.setPoint(new PointF(0, 0));
        CONVERSION.setMin(gcp);

        gcp = new GridConversionPoint();
        gcp.setCoordinate(Coordinate.MAX);
        gcp.setPoint(new PointF(1, 1));
        CONVERSION.setMax(gcp);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                BROADCASTER.halt();
            }
        }));
        BROADCASTER.start();
    }

    /**
     * See if we are to do the grid conversion.
     *
     * @return
     */
    public static boolean getDoGridConversion() {
        return DO_GRID_CONVERSION.get();
    }

    /**
     * Set if we are to do the grid conversion.
     *
     * @param value
     */
    public static void setDoGridConversion(boolean value) {
        DO_GRID_CONVERSION.set(value);
    }

    /**
     * Set the minimum conversion point.
     *
     * @param value
     */
    public static void setMinGridConversionPoint(GridConversionPoint value) {
        CONVERSION.setMin(value);
    }

    /**
     * Set the maximum conversion point.
     *
     * @param value
     */
    public static void setMaxGridConversionPoint(GridConversionPoint value) {
        CONVERSION.setMax(value);
    }

    /**
     * Get the minimum conversion point.
     *
     * @return
     */
    public static GridConversionPoint getMinGridConversionPoint() {
        return CONVERSION.getMin();
    }

    /**
     * Get the maximum conversion point.
     *
     * @return
     */
    public static GridConversionPoint getMaxGridConversionPoint() {
        return CONVERSION.getMax();
    }

    public static void broadcastPosition(Vbs3Protos.Position pos) {
        BROADCASTER.submit(pos);
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        LOGGER.log(Level.FINEST, "Socket Connected: {0}", sess);
        synchronized (INSTANCES) {
            INSTANCES.add(this);
        }
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        LOGGER.log(Level.FINEST, "Received TEXT message: {0}", message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        LOGGER.log(Level.FINEST, "Socket Closed: [{0}] {1}", new Object[]{statusCode, reason});
        synchronized (INSTANCES) {
            INSTANCES.remove(this);
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        LOGGER.log(Level.WARNING, null, cause);
        synchronized (INSTANCES) {
            INSTANCES.remove(this);
        }
    }
}
