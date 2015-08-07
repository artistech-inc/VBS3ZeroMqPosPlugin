/*
 * Copyright 2015 ArtisTech, Inc.
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
    private final static Logger log;

    static {
        log = Logger.getLogger(Mailbox.class.getName());
    }

    public Mailbox() {
        m_msgs = new ArrayList<>();
        m_halt = false;
    }
    
    public synchronized int size() {
        return m_msgs.size();
    }

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
     * Receive an AIMessage from the queue.
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
                log.log(Level.SEVERE, null, e);
                continue;
            }
            if (m_halt) {
                return null;
            }
        }
        T ret = m_msgs.remove(0);
        return ret;
    }

    public synchronized ArrayList<T> getMessages() {
        if (m_halt) {
            return null;
        }
        while (m_msgs.size() <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, null, e);
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
    
    public boolean isHalted() {
        return m_halt;
    }
}