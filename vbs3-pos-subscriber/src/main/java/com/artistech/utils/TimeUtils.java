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

import java.io.Serializable;

/**
 * Time values.
 *
 * @author matta Useful constants
 */
public final class TimeUtils implements Serializable {

    public static final int SECONDS_IN_MINUTE = 60;
    public static final long SECONDS_IN_MINUTE_L = 60;
    public static final double SECONDS_IN_MINUTE_D = 60.0;

    public static final int MINUTES_IN_HOUR = 60;
    public static final long MINUTES_IN_HOUR_L = 60;
    public static final double MINUTES_IN_HOUR_D = 60.0;

    public static final int HOURS_IN_DAY = 24;
    public static final long HOURS_IN_DAY_L = 24;
    public static final double HOURS_IN_DAY_D = 24.0;

    public static final int DAYS_IN_WEEK = 7;
    public static final long DAYS_IN_WEEK_L = 7;
    public static final double DAYS_IN_WEEK_D = 7.0;

    public static final int MILLI = 1000;
    public static final long MILLI_L = 1000;
    public static final double MILLI_D = 1000.0;

    public static final int MILLISECONDS_IN_MINUTE = MILLI * SECONDS_IN_MINUTE;
    public static final long MILLISECONDS_IN_MINUTE_L = MILLI_L * SECONDS_IN_MINUTE_L;
    public static final double MILLISECONDS_IN_MINUTE_D = MILLI_D * SECONDS_IN_MINUTE_D;

    public static final int MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * MINUTES_IN_HOUR;
    public static final long MILLISECONDS_IN_HOUR_L = MILLISECONDS_IN_MINUTE_L * MINUTES_IN_HOUR_L;
    public static final double MILLISECONDS_IN_HOUR_D = MILLISECONDS_IN_MINUTE_D * MINUTES_IN_HOUR_D;

    public static final int MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * HOURS_IN_DAY;
    public static final long MILLISECONDS_IN_DAY_L = MILLISECONDS_IN_HOUR_L * HOURS_IN_DAY_L;
    public static final double MILLISECONDS_IN_DAY_D = MILLISECONDS_IN_HOUR_D * HOURS_IN_DAY_D;

    public static final long NANO = 1000000000L;
    public static final double NANO_D = 1000000000.0;

    /**
     * Hidden Constructor.
     */
    private TimeUtils() {
    }
}
