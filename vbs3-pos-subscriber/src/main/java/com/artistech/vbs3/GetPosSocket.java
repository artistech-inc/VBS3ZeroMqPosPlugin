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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class GetPosSocket extends WebSocketAdapter {

    private static final Logger logger = Logger.getLogger(GetPosSocket.class.getName());

    private static final ArrayList<GetPosSocket> instances = new ArrayList<>();

    public static void broadcastPosition(Vbs3Protos.Position pos) {
        ArrayList<GetPosSocket> copy = new ArrayList<>();
        synchronized (instances) {
            copy.addAll(instances);
        }

        ByteBuffer bb = ByteBuffer.wrap(pos.toByteArray());
        for (GetPosSocket sock : copy) {
            try {
                sock.getSession().getRemote().sendBytesByFuture(bb);
            } catch (org.eclipse.jetty.websocket.api.WebSocketException ex) {
                logger.log(Level.WARNING, "WebSocket error; removing socket from listeners: {0}", ex.getMessage());
                synchronized (instances) {
                    instances.remove(sock);
                }
            }
        }
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        logger.log(Level.FINEST, "Socket Connected: {0}", sess);
        synchronized (instances) {
            instances.add(this);
        }
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        logger.log(Level.FINEST, "Received TEXT message: {0}", message);
//        System.out.println("Received TEXT message: " + message);
//        super.getSession().getRemote().sendStringByFuture(message + " - echo!");
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        logger.log(Level.FINEST, "Socket Closed: [{0}] {1}", new Object[]{statusCode, reason});
        synchronized (instances) {
            instances.remove(this);
        }
//        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        logger.log(Level.WARNING, null, cause);
        synchronized (instances) {
            instances.remove(this);
        }
//        cause.printStackTrace(System.err);
    }
}
