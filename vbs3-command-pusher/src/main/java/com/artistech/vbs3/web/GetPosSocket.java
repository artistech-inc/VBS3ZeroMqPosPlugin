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
package com.artistech.vbs3.web;

import com.artistech.vbs3.BroadcasterBaseImpl;
import com.artistech.vbs3.Vbs3Protos;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class GetPosSocket extends BroadcasterBaseImpl {

    public static class Adapter extends WebSocketAdapter {

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

    public class BroadcasterThread extends BroadcasterBaseImpl.BroadcasterThread {

        /**
         * Perform broadcast.
         *
         * @param pos
         */
        @Override
        protected void broadcastPosition(Vbs3Protos.Position pos) {
            ArrayList<Adapter> copy = new ArrayList<>();
            synchronized (INSTANCES) {
                copy.addAll(INSTANCES);
            }

            //send the data to all listeners
            byte[] data = pos.toByteArray();
            for (Adapter sock : copy) {
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

    private final static ArrayList<Adapter> INSTANCES = new ArrayList<>();
    private BroadcasterThread BROADCASTER;

    /**
     * Default Constructor
     */
    public GetPosSocket() {
        super();
        DO_GRID_CONVERSION.set(false);
    }

    @Override
    public void initialize() {
        BROADCASTER = new BroadcasterThread();
        BROADCASTER.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                BROADCASTER.halt();
            }
        }));
        BROADCASTER.start();
    }

    @Override
    public void broadcastPosition(Vbs3Protos.Position pos) {
        BROADCASTER.submit(pos);
    }
}
