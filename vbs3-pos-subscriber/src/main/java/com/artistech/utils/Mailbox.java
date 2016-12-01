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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mailbox: A synchronized class for high-performance message handling. Since
 * Suspend and Resume are deprecated from the Java API, we need a way to halt a
 * sending/receiving thread without the need for polling. This solves that
 * problem.
 *
 * @param <T>
 */
public class Mailbox<T> {

    private final java.util.ArrayList<T> m_msgs;
    private boolean m_halt;
    private final static Logger LOGGER;

    /**
     * Static Constructor.
     */
    static {
        LOGGER = Logger.getLogger(Mailbox.class.getName());
    }

    /**
     * Constructor.
     */
    public Mailbox() {
        m_msgs = new ArrayList<>();
        m_halt = false;
    }

    /**
     * Get the size.
     *
     * @return
     */
    public synchronized int size() {
        return m_msgs.size();
    }

    /**
     * Halt the mailbox.
     */
    public synchronized void halt() {
        if (!m_halt) {
            m_halt = true;
            notifyAll();
        }
    }

    /**
     * Add a message to the queue, notifying any waiting processes that a
     * message is available.
     *
     * @param inMsg
     */
    public synchronized void addMessage(T inMsg) {
        if (!m_halt) {
            m_msgs.add(inMsg);
            notifyAll();
        }
    }

    /**
     * Receive a message from the queue.
     *
     * This is a blocking call and will wait until the mailbox is halted or a
     * message is available.
     *
     * @return
     */
    public synchronized T getMessage() {
        if (m_halt) {
            return null;
        }
        while (m_msgs.size() <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, null, e);
                continue;
            }
            if (m_halt) {
                return null;
            }
        }
        T ret = m_msgs.remove(0);
        return ret;
    }

    /**
     * Get all messages in the mailbox.
     *
     * This is a blocking call and will wait until the mailbox is halted or a
     * message is available.
     *
     * @return
     */
    public synchronized ArrayList<T> getMessages() {
        if (m_halt) {
            return null;
        }
        while (m_msgs.size() <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, null, e);
                continue;
            }
            if (m_halt) {
                return null;
            }
        }
        ArrayList<T> ret = new ArrayList<>(m_msgs);
        m_msgs.clear();
        return ret;
    }

    /**
     * Check to see if the mailbox is halted.
     *
     * @return
     */
    public boolean isHalted() {
        return m_halt;
    }
}
