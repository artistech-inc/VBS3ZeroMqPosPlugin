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

    public static void main(String[] args) {
        String zeromq_port;
        int jettyPort = 8888;

        Options options = new Options();
        options.addOption("j", "jetty-port", true, "Jetty port to serve on. (DEFAULT -j 8888)");
        options.addOption("z", "zeromq-port", true, "ZeroMQ Server:Port to subscribe to. (-z localhost:5565)");
        options.addOption("h", "help", false, "Show this message.");
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLineParser parser = new org.apache.commons.cli.BasicParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("j") || cmd.hasOption("jetty-port")) {
                jettyPort = Integer.parseInt(cmd.getOptionValue("j"));
            }

            if (cmd.hasOption("help")) {
                formatter.printHelp("tuio-mouse-driver", options);
                return;
            } else {
                if (cmd.hasOption("z") || cmd.hasOption("zeromq-port")) {
                    zeromq_port = cmd.getOptionValue("z");
                } else {
                    System.err.println("The zeromq-port value must be specified.");
                    formatter.printHelp("tuio-mouse-driver", options);
                    return;
                }
            }
        } catch (org.apache.commons.cli.ParseException ex) {
            System.err.println("Error Processing Command Options:");
            formatter.printHelp("vbs3-pos-subscriber", options);
            return;
        }

        Jetty.startServer(jettyPort);

        //create zeromq context
        ZMQ.Context context = ZMQ.context(1);

        // Connect our subscriber socket
        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
        subscriber.setIdentity(Main.class.getName().getBytes());

        //this could change I guess so we can get different data subscrptions.
        subscriber.subscribe("".getBytes());
        subscriber.connect("tcp://" + zeromq_port);

        System.out.println("Subscribed to " + zeromq_port + " for ZeroMQ messages.");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                Jetty.stopServer();
            }
        }));

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
                    System.out.println(message.toString());
                    System.out.flush();
                } catch (Exception ex) {
                    success = false;
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
