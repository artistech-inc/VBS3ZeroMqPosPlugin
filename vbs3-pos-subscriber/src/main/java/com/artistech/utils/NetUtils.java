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
package com.artistech.utils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides to net level functionality.
 *
 * @author matta
 */
public class NetUtils {

    private static InetAddress _ip;
    private static InetAddress[] _ips;

    /**
     * Static Constructor.
     */
    static {
        try {
            _ip = getIP();
        } catch (SocketException ex) {
            Logger.getLogger(NetUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Hidden Constructor.
     */
    private NetUtils() {
    }

    /**
     * Get an IP.
     *
     * @return
     * @throws SocketException
     */
    public static InetAddress getIP() throws SocketException {
        if (_ip != null) {
            return _ip;
        }

        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        while (nis.hasMoreElements()) {
            ni = nis.nextElement();
            if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    //filter for ipv4/ipv6
                    if (ia.getAddress().getAddress().length == 4) {
                        //4 for ipv4, 16 for ipv6
                        _ip = ia.getAddress();
                        return _ip;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get all IPs.
     *
     * @return
     * @throws SocketException
     */
    public static InetAddress[] getIPs() throws SocketException {
        if (_ips != null) {
            return _ips;
        }

        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        ArrayList<InetAddress> ret = new ArrayList<>();
        while (nis.hasMoreElements()) {
            ni = nis.nextElement();
            if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    //filter for ipv4/ipv6
                    if (ia.getAddress().getAddress().length == 4) {
                        //4 for ipv4, 16 for ipv6
                        _ip = ia.getAddress();
                        ret.add(_ip);
//                        return _ip;
                    }
                }
            }
        }
        _ips = ret.toArray(new InetAddress[]{});
        return _ips;
    }
}
