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
public class ManualResetEvent {

    private final Object monitor = new Object();
    private volatile boolean open = false;

    /**
     * Constructor.
     *
     * @param open
     */
    public ManualResetEvent(boolean open) {
        this.open = open;
    }

    /**
     * Wait for signal.
     *
     * @throws InterruptedException
     */
    public void waitOne() throws InterruptedException {
        synchronized (monitor) {
            while (open == false) {
                monitor.wait();
            }
        }
    }

    /**
     * Wait for signal.
     *
     * @param milliseconds
     * @return
     * @throws InterruptedException
     */
    public boolean waitOne(long milliseconds) throws InterruptedException {
        synchronized (monitor) {
            if (open) {
                return true;
            }
            monitor.wait(milliseconds);
            return open;
        }
    }

    /**
     * Signal.
     */
    public void set() {//open start
        synchronized (monitor) {
            open = true;
            monitor.notifyAll();
        }
    }

    /**
     * Reset.
     */
    public void reset() {//close stop
        open = false;
    }

    /**
     * Is the event open?
     *
     * @return
     */
    public boolean isOpen() {
        return open;
    }
}
