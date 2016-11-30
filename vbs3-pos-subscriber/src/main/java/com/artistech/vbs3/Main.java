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

import com.artistech.utils.logging.SingleLineFormatter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.zeromq.ZMQ;

/**
 *
 * @author matta
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        int jettyPort = 8888;

        Logger logger = Logger.getLogger("");
        for (Handler h : logger.getHandlers()) {
            h.setFormatter(new SingleLineFormatter());
        }

        //TODO: possibly make it so each component can push it's options to this menu.
        Options options = new Options();
        options.addOption("j", "jetty-port", true, "Jetty port to serve on. (DEFAULT -j 8888)");
        options.addOption("z", "zeromq-port", true, "ZeroMQ Server:Port to subscribe to. (-z localhost:5565)");
        options.addOption("p", "udp-port", true, "UDP Port for Broadcasting. (DEFAULT 5000)");
        options.addOption("i", "udp-ip", true, "UDP Server Address. (DEFAULT 228.5.6.7)");
        options.addOption("h", "help", false, "Show this message.");
        HelpFormatter formatter = new HelpFormatter();
        String[] zeroMqServers;

        try {
            CommandLineParser parser = new org.apache.commons.cli.BasicParser();
            CommandLine cmd = parser.parse(options, args);

            zeroMqServers = cmd.getOptionValues("z");

            if (cmd.hasOption("j") || cmd.hasOption("jetty-port")) {
                jettyPort = Integer.parseInt(cmd.getOptionValue("j"));
            }
            Field ip_field = null;
            Field port_field = null;
            Class<?> forName = null;
            try {
                //set UDP options...
                forName = Class.forName("com.artistech.vbs3.net.UdpBroadcaster");
                ip_field = forName.getField("UDP_IP_ADDRESS");
                port_field = forName.getField("UDP_PORT");
            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (ip_field != null && (cmd.hasOption("i") || cmd.hasOption("udp-ip"))) {
                //UdpBroadcaster.UDP_IP_ADDRESS = cmd.getOptionValue("i");
                try {
                    ip_field.set(forName, cmd.getOptionValue("i"));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (port_field != null && (cmd.hasOption("p") || cmd.hasOption("udp-port"))) {
//                UdpBroadcaster.UDP_PORT = Integer.parseInt(cmd.getOptionValue("p"));
                try {
                    port_field.set(forName, Integer.parseInt(cmd.getOptionValue("p")));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if (cmd.hasOption("help")) {
                formatter.printHelp("tuio-mouse-driver", options);
                return;
            } else if (zeroMqServers == null || zeroMqServers.length <= 0) {
                System.err.println("The zeromq-port value(s) must be specified.");
                formatter.printHelp("vbs3-pos-subscriber", options);
                return;
            }
        } catch (org.apache.commons.cli.ParseException ex) {
            System.err.println("Error Processing Command Options:");
            formatter.printHelp("vbs3-pos-subscriber", options);
            return;
        }

        Jetty.startServer(jettyPort);

        //create zeromq context
        ZMQ.Context context = ZMQ.context(1);

        int counter = 1;
        final ArrayList<Thread> threads = new ArrayList<>();

        final ServiceLoader<PositionBroadcaster> broadcasters = ServiceLoader.load(PositionBroadcaster.class);
        for (PositionBroadcaster b : broadcasters) {
            b.initialize();
            LOGGER.log(Level.INFO, "BROADCASTER:  {0}", b.getClass().getName());
        }

        for (String zeroMqServer : zeroMqServers) {
            // Connect our subscriber socket
            final ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
            subscriber.setIdentity((Main.class.getName() + Integer.toString(counter++)).getBytes());
            //this could change I guess so we can get different data subscrptions.
            subscriber.subscribe("".getBytes());
            subscriber.connect("tcp://" + zeroMqServer);
            LOGGER.log(Level.INFO, "Subscribed to {0} for ZeroMQ messages.", zeroMqServer);

            final Thread recvThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // Get updates, expect random Ctrl-C death
                    boolean success = true;
                    while (success) {
                        success = false;
                        byte[] recv = subscriber.recv();
                        if (recv.length > 0) {
                            try {
                                Vbs3Protos.Position pos = Vbs3Protos.Position.parseFrom(recv);
                                LOGGER.log(Level.FINEST, "X: {0}", pos.getX());
                                LOGGER.log(Level.FINEST, "Y: {0}", pos.getY());
                                LOGGER.log(Level.FINEST, "Lat: {0}", pos.getLat());
                                LOGGER.log(Level.FINEST, "Lon: {0}", pos.getLon());
                                for (PositionBroadcaster b : broadcasters) {
                                    b.broadcastPosition(pos);
                                }
                                success = true;
                            } catch (Exception ex) {
                                success = false;
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            });
            threads.add(recvThread);
            recvThread.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                Jetty.stopServer();
                for (Thread t : threads) {
                    try {
                        t.interrupt();
                    } catch (Exception ex) {
                    }
                }
            }
        }));
    }
}
