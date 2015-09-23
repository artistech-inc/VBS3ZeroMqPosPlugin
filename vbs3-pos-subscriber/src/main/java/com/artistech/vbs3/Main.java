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
import com.artistech.geo.GridConversionPoint;
import com.artistech.utils.ArgumentOutOfRangeException;
import com.artistech.utils.logging.SingleLineFormatter;
import java.util.ArrayList;
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

        Options options = new Options();
        options.addOption("j", "jetty-port", true, "Jetty port to serve on. (DEFAULT -j 8888)");
        options.addOption("z", "zeromq-port", true, "ZeroMQ Server:Port to subscribe to. (-z localhost:5565)");
        options.addOption("q", "min-lat", true, "Min Latitude value.  (DEFAULT -q -90.0)");
        options.addOption("w", "min-lon", true, "Min Longitude value. (DEFAULT -w -180.0)");
        options.addOption("e", "max-lat", true, "Max Latitude value.  (DEFAULT -e 90.0)");
        options.addOption("r", "max-lon", true, "Max Longitude value. (DEFAULT -r 180.0)");
        options.addOption("c", "do-grid-conversion", false, "Do Grid Conversion. (DEFAULT OFF)");
        options.addOption("h", "help", false, "Show this message.");
        HelpFormatter formatter = new HelpFormatter();
        String[] zeroMqServers;

        Double min_lat = -90.0;
        Double min_lon = -180.0;
        Double max_lat = 90.0;
        Double max_lon = 180.0;
        boolean do_grid_conversion = false;

        try {
            CommandLineParser parser = new org.apache.commons.cli.BasicParser();
            CommandLine cmd = parser.parse(options, args);

            zeroMqServers = cmd.getOptionValues("z");

            if (cmd.hasOption("j") || cmd.hasOption("jetty-port")) {
                jettyPort = Integer.parseInt(cmd.getOptionValue("j"));
            }
            if (cmd.hasOption("c") || cmd.hasOption("do-grid-conversion")) {
                do_grid_conversion = true;
            }
            if (cmd.hasOption("q") || cmd.hasOption("min-lat")) {
                min_lat = Double.parseDouble(cmd.getOptionValue("q"));
            }
            if (cmd.hasOption("w") || cmd.hasOption("min-lon")) {
                min_lon = Double.parseDouble(cmd.getOptionValue("w"));
            }
            if (cmd.hasOption("e") || cmd.hasOption("max-lat")) {
                max_lat = Double.parseDouble(cmd.getOptionValue("e"));
            }
            if (cmd.hasOption("r") || cmd.hasOption("max-lon")) {
                max_lon = Double.parseDouble(cmd.getOptionValue("r"));
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

        //initialize LAT/LON for grid conversion...
        try {
            GridConversionPoint maxGridConversionPoint = GetPosSocket.getMaxGridConversionPoint();
            Coordinate c = new Coordinate(max_lon, max_lat);
            maxGridConversionPoint.setCoordinate(c);
            LOGGER.log(Level.INFO, "MAX COORDINATE:  {0}", GetPosSocket.getMaxGridConversionPoint().getCoordinate());
        } catch (ArgumentOutOfRangeException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            GridConversionPoint minGridConversionPoint = GetPosSocket.getMinGridConversionPoint();
            Coordinate c = new Coordinate(min_lon, min_lat);
            minGridConversionPoint.setCoordinate(c);
            LOGGER.log(Level.INFO, "MIN COORDINATE:  {0}", GetPosSocket.getMinGridConversionPoint().getCoordinate());
        } catch (ArgumentOutOfRangeException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        GetPosSocket.setDoGridConversion(do_grid_conversion);
        LOGGER.log(Level.INFO, "PERFORMING POINT CONVERSION:  {0}", GetPosSocket.getDoGridConversion());

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
                                Vbs3Protos.Position message = Vbs3Protos.Position.parseFrom(recv);
                                GetPosSocket.broadcastPosition(message);
                                success = true;
                                LOGGER.log(Level.FINEST, message.toString());
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
