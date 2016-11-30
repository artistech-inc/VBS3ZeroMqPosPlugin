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

/**
 * This is a wrapper class around an object's wait and notify methods. This is
 * used as a class for familiarity with .NET's event classes.
 *
 * @author matta
 */
public class AutoResetEvent {

    private final Object _monitor = new Object();
    private volatile boolean _isOpen = false;

    /**
     * Constructor.
     *
     * @param open
     */
    public AutoResetEvent(boolean open) {
        _isOpen = open;
    }

    /**
     * Wait until signaled.
     *
     * @throws InterruptedException
     */
    public void waitOne() throws InterruptedException {
        synchronized (_monitor) {
            while (!_isOpen) {
                _monitor.wait();
            }
            _isOpen = false;
        }
    }

    /**
     * Wait until signaled.
     *
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean waitOne(long timeout) throws InterruptedException {
        boolean ret = true;
        synchronized (_monitor) {
            long t = System.currentTimeMillis();
            while (!_isOpen) {
                _monitor.wait(timeout);
                // Check for timeout
                if (System.currentTimeMillis() - t >= timeout) {
                    ret = false;
                    break;
                }
            }
            _isOpen = false;
        }
        return ret;
    }

    /**
     * Signal.
     */
    public void set() {
        synchronized (_monitor) {
            _isOpen = true;
            _monitor.notify();
        }
    }

    /**
     * Reset.
     */
    public void reset() {
        _isOpen = false;
    }
}
