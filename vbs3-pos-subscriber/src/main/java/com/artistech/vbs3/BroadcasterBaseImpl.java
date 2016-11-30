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
package com.artistech.vbs3;

import com.artistech.utils.HaltMonitor;
import com.artistech.utils.Mailbox;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author matta
 */
public abstract class BroadcasterBaseImpl implements PositionBroadcaster {

    public abstract class BroadcasterThread extends Thread {

        protected final HaltMonitor monitor;
        protected final Mailbox<Vbs3Protos.Position> messages;

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
        public final void run() {
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
        public final void halt() {
            monitor.halt();
        }

        /**
         * Submit a message to be broadcast.
         *
         * @param pos
         */
        public final void submit(Vbs3Protos.Position pos) {
            messages.addMessage(pos);
        }

        /**
         * Perform broadcast.
         *
         * @param pos
         */
        protected abstract void broadcastPosition(Vbs3Protos.Position pos);

    }

    private static final Logger LOGGER = Logger.getLogger(BroadcasterBaseImpl.class.getName());

    /**
     * Default Constructor
     */
    public BroadcasterBaseImpl() {
    }

}
