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
package com.artistech.vbs3.kml;

import com.artistech.vbs3.BroadcasterBaseImpl;
import com.artistech.vbs3.Vbs3Protos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public class KmlBroadcaster extends BroadcasterBaseImpl {

    public class BroadcasterThread extends BroadcasterBaseImpl.BroadcasterThread {

        /**
         * Perform broadcast.
         *
         * @param pos
         */
        @Override
        protected void broadcastPosition(Vbs3Protos.Position pos) {
            //push as the current position
            synchronized (CURRENT) {
                CURRENT.put(pos.getId(), pos);
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(KmlBroadcaster.class.getName());
    private static final HashMap<String, Vbs3Protos.Position> CURRENT;
    private BroadcasterThread BROADCASTER;

    public KmlBroadcaster() {
        super();
        DO_GRID_CONVERSION.set(true);
    }

    static {
        CURRENT = new HashMap<>();
    }

    @Override
    public void broadcastPosition(Vbs3Protos.Position pos) {
        BROADCASTER.submit(pos);
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

    public static Collection<Vbs3Protos.Position> getPositions() {
        synchronized (CURRENT) {
            return new ArrayList<>(CURRENT.values());
        }
    }
}
