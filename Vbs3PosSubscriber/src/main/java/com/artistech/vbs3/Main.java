/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

        Options options = new Options();
        options.addOption("z", "zeromq-port", true, "ZeroMQ Server:Port to subscribe to. (-z localhost:5565)");
        options.addOption("h", "help", false, "Show this message.");
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLineParser parser = new org.apache.commons.cli.BasicParser();
            CommandLine cmd = parser.parse(options, args);

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

        //create zeromq context
        ZMQ.Context context = ZMQ.context(1);

        // Connect our subscriber socket
        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
        subscriber.setIdentity(Main.class.getName().getBytes());

        //this could change I guess so we can get different data subscrptions.
        //subscriber.subscribe("TuioCursor".getBytes());
        //subscriber.subscribe("TuioTime".getBytes());
        subscriber.subscribe("".getBytes());
        subscriber.connect("tcp://" + zeromq_port);

        System.out.println("Subscribed to " + zeromq_port + " for ZeroMQ messages.");

        // Get updates, expect random Ctrl-C death
        boolean success = true;
        while (success) {
            success = false;
            byte[] recv = subscriber.recv();
            if (recv.length > 0) {
                try {
                    Vbs3Protos.Position message = Vbs3Protos.Position.parseFrom(recv);
                    success = true;
                    System.out.println(message.toString());
                    System.out.flush();
                }
                catch(Exception ex) {
                    success = false;
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
