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

import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * Can be used to monitor when a thread is halted/exits.
 * 
 * @author matta
 */
public final class HaltMonitor implements IHaltMonitor {

    private boolean _halted;
    private final EventListenerSupport<IHaltListener> _listeners
            = EventListenerSupport.create(IHaltListener.class);
    private String _name;

    /**
     * Initialize a new monitor.
     */
    public HaltMonitor() {
        _halted = false;
        _name = "";
    }

    /**
     * Get the name of the monitor.
     *
     * @return the name of the monitor.
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the monitor.
     *
     * @param value the name of the monitor.
     */
    public void setName(final String value) {
        _name = value;
    }

    /**
     * Add a new listener.
     *
     * @param listener The listener to add.
     */
    public synchronized void addListener(final IHaltListener listener) {
        _listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeListener(final IHaltListener listener) {
        _listeners.removeListener(listener);
    }

    /**
     * Is the thread halted?
     *
     * @return Is the thread halted?
     */
    @Override
    public boolean isHalted() {
        return _halted;
    }

    /**
     * Halt the thread.
     */
    public synchronized void halt() {
        _halted = true;
        _listeners.fire().halted(this);
        for(IHaltListener l : _listeners.getListeners()) {
            _listeners.removeListener(l);
        }
    }
}
