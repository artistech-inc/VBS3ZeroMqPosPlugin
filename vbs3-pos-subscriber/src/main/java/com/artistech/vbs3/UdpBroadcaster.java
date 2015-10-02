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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public class UdpBroadcaster implements PositionBroadcaster {

    public class BroadcasterThread extends Thread {

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

            String name = "Player_" + pos.getId().replaceAll("\"", "").replaceAll(" ", "_");

            //send the data to all listeners
            //the data to be sent is not this, but is a string I think...
            try {
                String msg = "node " + name + " position " + pos.getX() + "," + pos.getY() + NEW_LINE;
                DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, UDP_PORT);
                socket.send(hi);
//                msg = "node " + name + " orientation " + pos.getDir() + NEW_LINE;
//                hi = new DatagramPacket(msg.getBytes(), msg.length(), group, port);
//                socket.send(hi);
            } catch (IOException ex) {
                Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(UdpBroadcaster.class.getName());

    public static int UDP_PORT = 5000;
    public static String UDP_IP_ADDRESS = "228.5.6.7";

    private BroadcasterThread BROADCASTER;
    private final GridConversion CONVERSION;
    private boolean _conversionPointInitialized;
    private final AtomicBoolean DO_GRID_CONVERSION;
    private InetAddress group;
    private MulticastSocket socket;
    private static final String NEW_LINE = System.getProperty("line.separator");

    public UdpBroadcaster() {
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
    public boolean getDoGridConversion() {
        return DO_GRID_CONVERSION.get();
    }

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
        BROADCASTER.submit(pos);
    }

    @Override
    public void initialize() {
        try {
            group = InetAddress.getByName(UDP_IP_ADDRESS);
            socket = new MulticastSocket(UDP_PORT);
            socket.joinGroup(group);
        } catch (UnknownHostException ex) {
            Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
        }

        LOGGER.log(Level.INFO, "UDP_IP_ADDRESS: {0}", UDP_IP_ADDRESS);
        LOGGER.log(Level.INFO, "UDP_PORT:       {0}", UDP_PORT);

        BROADCASTER = new UdpBroadcaster.BroadcasterThread();
        BROADCASTER.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                BROADCASTER.halt();
            }
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    socket.leaveGroup(group);
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        BROADCASTER.start();
    }

}
