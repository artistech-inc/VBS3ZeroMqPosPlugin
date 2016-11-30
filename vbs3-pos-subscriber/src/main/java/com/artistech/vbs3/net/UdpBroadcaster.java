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
package com.artistech.vbs3.net;

import com.artistech.vbs3.BroadcasterBaseImpl;
import com.artistech.vbs3.Vbs3Protos;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public class UdpBroadcaster extends BroadcasterBaseImpl {

    public class BroadcasterThread extends BroadcasterBaseImpl.BroadcasterThread {

        /**
         * Perform broadcast.
         *
         * @param pos
         */
        @Override
        protected void broadcastPosition(Vbs3Protos.Position pos) {
            String name = "Player_" + pos.getId().replaceAll("\"", "").replaceAll(" ", "_");

            //send the data to all listeners
            //the data to be sent is not this, but is a string I think...
            try {
                String msg = "node " + name + " position " + pos.getX() + "," + pos.getY() + NEW_LINE;
                DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), group, UDP_PORT);
//                LOGGER.log(Level.FINEST, msg.trim());
                socket.send(hi);
            } catch (IOException ex) {
                Logger.getLogger(UdpBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(UdpBroadcaster.class.getName());

    public static int UDP_PORT = 5000;
    public static String UDP_IP_ADDRESS = "228.5.6.7";

    private BroadcasterThread BROADCASTER;
    private InetAddress group;
    private MulticastSocket socket;
    private static final String NEW_LINE = System.getProperty("line.separator");

    public UdpBroadcaster() {
        super();
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
