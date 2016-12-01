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

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author matta This is a utility class for accessing random members of a
 * collection and also generating random numbers/values.
 *
 * In this class, there are PUID values which is for pseudo-unique-ID values.
 * These values are unique within a given simulation, but can be duplicated for
 * repeatability.
 */
public final class Random {

    /**
     * Enumeration for a 3 state system
     */
    public enum TriState {

        TRUE,
        FALSE,
        MAYBE
    }

    /**
     * Enumeration for how the UUID values should be incremented
     */
    public enum UuidIncrementType {

        USE_INSTANCE,
        USE_CLASS,
        USE_PACKAGE
    }

    private final static HashMap<Thread, java.util.Random> RANDOMS;
    private final static HashMap<Object, java.util.Random> SPECIFIC_RANDOMS;
    private static UuidIncrementType _incrementType = UuidIncrementType.USE_CLASS;
    private static boolean _doSeed;
    private static int _seedValue;
    private final static HashMap<String, AtomicLong> COUNTERS;
    private static final String DEFAULT_KEY = "puid";

    private Random() {
    }

    static {
        boolean legacy = false;

        COUNTERS = new HashMap<>();
        Properties prop = new Properties();
        String prop_file = com.artistech.utils.Random.class.getCanonicalName().replace('.', '/') + ".properties";
        try {
            prop.load(com.artistech.utils.Random.class.getClassLoader().getResourceAsStream(prop_file));
            _incrementType = UuidIncrementType.valueOf(prop.getProperty("uuidIncrementType"));
            _doSeed = Boolean.parseBoolean(prop.getProperty("doSeed"));
            _seedValue = Integer.parseInt(prop.getProperty("seedValue"));
            legacy = Boolean.parseBoolean(prop.getProperty("useLegacy"));
        } catch (NullPointerException | IOException ex) {
            Logger.getLogger(Random.class.getName()).log(Level.SEVERE, null, ex);
        }
        RANDOMS = new HashMap<>();
        SPECIFIC_RANDOMS = new HashMap<>();
    }

    private static java.util.Random getInstance() {
        Thread curr = Thread.currentThread();
        if (!RANDOMS.containsKey(curr)) {
            if (_doSeed) {
                RANDOMS.put(curr, new java.util.Random(_seedValue));
            } else {
                RANDOMS.put(curr, new java.util.Random());
            }
        }
        return RANDOMS.get(curr);
    }

    public static java.util.Random getInstance(final Object key) {
        if (!SPECIFIC_RANDOMS.containsKey(key)) {
            if (_doSeed) {
                SPECIFIC_RANDOMS.put(key, new java.util.Random(_seedValue));
            } else {
                SPECIFIC_RANDOMS.put(key, new java.util.Random());
            }
        }
        return SPECIFIC_RANDOMS.get(key);
    }

    /**
     * Reset the random object to initial state (only useful if the random
     * object is seeded). This will clear the PUID counters.
     */
    public synchronized static void reset() {
        reset(true);
    }

    public synchronized static void reset(Class<?> c, long value) {
        String key = DEFAULT_KEY;
        switch (_incrementType) {
            case USE_CLASS:
                key = c.getName();
                break;
            case USE_PACKAGE:
                key = c.getPackage().getName();
                break;
        }
        if (!COUNTERS.containsKey(key)) {
            COUNTERS.put(key, new AtomicLong(1));
        }
        AtomicLong l = COUNTERS.get(key);
        l.set(value + 1);
    }

    /**
     * Reset the random object to initial state (only useful if the random
     * object is seeded) However can be specified to leave the PUID counters
     * alone.
     *
     * @param clear_count specify if we want to clear the UUID values.
     */
    public synchronized static void reset(final boolean clear_count) {
        RANDOMS.clear();
        SPECIFIC_RANDOMS.clear();
        if (clear_count) {
            COUNTERS.clear();
        }
    }

    /**
     * get a new PUID value for a specified class type.
     *
     * @param c the class type
     * @return a new PUID value
     */
    public static String getRandomName(final Class<?> c) {
        return getPUID(c);
    }

    public synchronized static String getPUID(final Class<?> c, UuidIncrementType type) {
        String key = DEFAULT_KEY;
        switch (type) {
            case USE_CLASS:
                key = c.getName();
                break;
            case USE_PACKAGE:
                key = c.getPackage().getName();
                break;
        }
        if (!COUNTERS.containsKey(key)) {
            COUNTERS.put(key, new AtomicLong(1));
        }
        AtomicLong l = COUNTERS.get(key);

        Long l2 = l.getAndIncrement();
        return key + "-" + l2.toString();
    }

    /**
     * get a new PUID value for a specified class type.
     *
     * @param c the class type
     * @return a new PUID value
     */
    public synchronized static String getPUID(final Class<?> c) {
        return getPUID(c, _incrementType);
    }

    /**
     *
     * get a new PUID value for a specified class type.
     *
     * @param c the class type
     * @param strength the strength of the PUID (unused for now)
     * @return a new PUID value
     */
    public synchronized static String getPUID(final Class<?> c, final int strength) {
        return getPUID(c);
    }

    /**
     * Return a random boolean value
     *
     * @param random
     * @return a random true/false value
     */
    public synchronized static boolean nextBoolean(java.util.Random random) {
        return random.nextBoolean();
    }

    /**
     * Return a random boolean value
     *
     * @return a random true/false value
     */
    public synchronized static boolean nextBoolean() {
        return nextBoolean(getInstance());
    }

    /**
     * return a random tri-state value
     *
     * @param random
     * @return a random tri-state value TRUE/FALSE/MAYBE
     */
    public synchronized static TriState nextTriState(java.util.Random random) {
        double d = random.nextDouble();
        if (d < (1.0 / 3.0)) {
            return TriState.FALSE;
        } else if (d <= (2.0 / 3.0)) {
            return TriState.MAYBE;
        } else {
            return TriState.TRUE;
        }
    }

    /**
     * return a random tri-state value
     *
     * @return a random tri-state value TRUE/FALSE/MAYBE
     */
    public synchronized static TriState nextTriState() {
        return nextTriState(getInstance());
    }

    /**
     * Returns a new double value from 0.0 inclusive to 1.0 exclusive.
     *
     * @param random
     * @return a new double value from 0.0 inclusive to 1.0 exclusive.
     */
    public synchronized static double nextDouble(java.util.Random random) {
        return random.nextDouble();
    }

    /**
     * Returns a new double value from 0.0 inclusive to 1.0 exclusive.
     *
     * @return a new double value from 0.0 inclusive to 1.0 exclusive.
     */
    public synchronized static double nextDouble() {
        return nextDouble(getInstance());
    }

    /**
     * Returns a new float value from 0.0 inclusive to 1.0 exclusive.
     *
     * @param random
     * @return a new float value from 0.0 inclusive to 1.0 exclusive.
     */
    public synchronized static float nextFloat(java.util.Random random) {
        return random.nextFloat();
    }

    /**
     * Returns a new float value from 0.0 inclusive to 1.0 exclusive.
     *
     * @return a new float value from 0.0 inclusive to 1.0 exclusive.
     */
    public synchronized static float nextFloat() {
        return nextFloat(getInstance());
    }

    /**
     * Returns a random integer
     *
     * @param random
     * @return a random integer
     */
    public synchronized static int nextInt(java.util.Random random) {
        return random.nextInt();
    }

    /**
     * Returns a random integer
     *
     * @return a random integer
     */
    public synchronized static int nextInt() {
        return nextInt(getInstance());
    }

    /**
     * Returns a random integer less than the specified value
     *
     * @param random
     * @param n the specified value
     * @return a random integer >= 0 and < n
     */
    public synchronized static int nextInt(java.util.Random random, final int n) {
        return random.nextInt(Math.max(1, n));
    }
    /**
     * Returns a random integer less than the specified value
     *
     * @param n the specified value
     * @return a random integer >= 0 and < n
     */
    public synchronized static int nextInt(final int n) {
        return nextInt(getInstance(), Math.max(1, n));
    }

    /**
     * Returns a random integer within the specified range.
     *
     * @param random
     * @param min the min value (inclusive)
     * @param max the max value (exclusive)
     * @return a random integer within the specified range.
     */
    public synchronized static int nextInt(java.util.Random random, final int min, final int max) {
        return (random.nextInt(Math.max(1, max - min)) + min);
    }
    /**
     * Returns a random integer within the specified range.
     *
     * @param min the min value (inclusive)
     * @param max the max value (exclusive)
     * @return a random integer within the specified range.
     */
    public synchronized static int nextInt(final int min, final int max) {
        return nextInt(getInstance(), min, max);
    }

    /**
     * Returns a random long value.
     *
     * @param random
     * @return a random long value.
     */
    public synchronized static long nextLong(java.util.Random random) {
        return random.nextLong();
    }

    /**
     * Returns a random long value.
     *
     * @return a random long value.
     */
    public synchronized static long nextLong() {
        return nextLong(getInstance());
    }

    /**
     * Returns a random long value.
     *
     * @param random
     * @param max
     * @return
     */
    public synchronized static long nextLong(java.util.Random random, final long max) {
        // error checking and 2^x checking removed for simplicity.
        return nextLong(random, 0, Math.max(max, 1));
    }
    /**
     * Returns a random long value.
     *
     * @param max
     * @return
     */
    public synchronized static long nextLong(final long max) {
        // error checking and 2^x checking removed for simplicity.
        return nextLong(getInstance(), max);
    }

    /**
     * Returns a random long value.
     *
     * @param random
     * @param min
     * @param max
     * @return
     */
    public synchronized static long nextLong(java.util.Random random, final long min, final long max) {
        // error checking and 2^x checking removed for simplicity.
        long bits, val;
        java.util.Random rng = random;
        long max2 = Math.abs(max - min);
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % max2;
        } while (bits - val + (max2 - 1) < 0L);
        return val + min;
    }
    /**
     * Returns a random long value.
     *
     * @param min
     * @param max
     * @return
     */
    public synchronized static long nextLong(final long min, final long max) {
        // error checking and 2^x checking removed for simplicity.
        long bits, val;
        java.util.Random rng = getInstance();
        long max2 = Math.abs(max - min);
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % max2;
        } while (bits - val + (max2 - 1) < 0L);
        return val + min;
    }

    /**
     * Returns the next value in a Gaussian series.
     *
     * @param key the key for the Guassian series.
     * @return a random value with a Guassian distribution.
     */
    public synchronized static double nextGaussian(final Object key) {
        return getInstance(key).nextGaussian();
    }

    /**
     * Returns a new collection that has the specified number of items in the
     * list.
     *
     * @param <T> the type
     * @param list the collection to choose random objects from
     * @param count the number of items to select
     * @return a new collection
     */
    public static <T> Collection<T> getRandom(final Collection<T> list, final int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        if (list.size() > count && count > 0) {
            final int list_count = list.size();
            final int max_stride = list_count / count;
            java.util.Random instance = getInstance();
            final int start = com.artistech.utils.Random.nextInt(instance, list_count % count);
            final ArrayList<T> retVal = new ArrayList<>();
            final ArrayList<Integer> ints = new ArrayList<>();

            for (int ii = start; ints.size() < count; ii += max_stride) {
                if (count - 1 == retVal.size()) {
                    ints.add(com.artistech.utils.Random.nextInt(instance, ii, list_count));
                } else {
                    ints.add(com.artistech.utils.Random.nextInt(instance, ii, ii + max_stride));
                }
            }

            Iterator<T> it = list.iterator();
            Integer c = 0;
            while (it.hasNext()) {
                T ret = it.next();
                if (ints.contains(c)) {
                    retVal.add(ret);
                }
                c++;
            }
            return retVal;
        } else {
            return new ArrayList<>(list);
        }
    }

    /**
     * Returns a new collection that has the specified number of items in the
     * list.
     *
     * @param <T> the type
     * @param random
     * @param list the collection to choose random objects from
     * @param count the number of items to select
     * @return a new collection
     */
    public static <T> Collection<T> getRandom(java.util.Random random, final Collection<T> list, final int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        if (list.size() > count && count > 0) {
            final int list_count = list.size();
            final int max_stride = list_count / count;
            java.util.Random instance = random;
            final int start = com.artistech.utils.Random.nextInt(instance, list_count % count);
            final ArrayList<T> retVal = new ArrayList<>();
            final ArrayList<Integer> ints = new ArrayList<>();

            for (int ii = start; ints.size() < count; ii += max_stride) {
                if (count - 1 == retVal.size()) {
                    ints.add(com.artistech.utils.Random.nextInt(instance, ii, list_count));
                } else {
                    ints.add(com.artistech.utils.Random.nextInt(instance, ii, ii + max_stride));
                }
            }

            Iterator<T> it = list.iterator();
            Integer c = 0;
            while (it.hasNext()) {
                T ret = it.next();
                if (ints.contains(c)) {
                    retVal.add(ret);
                }
                c++;
            }
            return retVal;
        } else {
            return new ArrayList<>(list);
        }
    }

//    /**
//     * Gets a random item from a collection that is NOT equal to the specified
//     * object
//     *
//     * @param <T> the type
//     * @param list the collection to select from
//     * @param not the object which we do not want a duplicate selection of
//     * @return a new randomly selected object which is not equal to the
//     * specified value
//     */
//    public static <T> T getRandom(final Collection<T> list, final T not) {
//        if (list.isEmpty()) {
//            return null;
//        }
//        if (_legacy) {
//            Collection<T> not_list = new ArrayList<>();
//            not_list.add(not);
//            return getRandomNotInList(list, not_list);
//        } else {
//            final T elem = getRandom(list);
//            if (elem != null && elem.equals(not)) {
//                Logger.getLogger(Random.class.getName()).log(Level.FINEST, "Creating array copy for finding random item.");
//                ArrayList<T> l = new ArrayList<>(list);
//                l.remove(not);
//                return getRandom(l);
//            }
//            return elem;
//        }
//    }
//
//    /**
//     * Gets a random item from a collection that is NOT equal to the specified
//     * object
//     *
//     * @param <T> the type
//     * @param random
//     * @param list the collection to select from
//     * @param not the object which we do not want a duplicate selection of
//     * @return a new randomly selected object which is not equal to the
//     * specified value
//     */
//    public static <T> T getRandom(java.util.Random random, final Collection<T> list, final T not) {
//        if (list.isEmpty()) {
//            return null;
//        }
//        if (_legacy) {
//            Collection<T> not_list = new ArrayList<>();
//            not_list.add(not);
//            return getRandomNotInList(random, list, not_list);
//        } else {
//            final T elem = getRandom(random, list);
//            if (elem != null && elem.equals(not)) {
//                Logger.getLogger(Random.class.getName()).log(Level.FINEST, "Creating array copy for finding random item.");
//                ArrayList<T> l = new ArrayList<>(list);
//                l.remove(not);
//                return getRandom(random, l);
//            }
//            return elem;
//        }
//    }
//
//    /**
//     * Gets a random item from a collection that is NOT equal to the specified
//     * collection
//     *
//     * @param <T> the type
//     * @param list the collection to select from
//     * @param not the collection from which we do not want a duplicate selection
//     * of
//     * @return a new randomly selected object which is not equal to the
//     * specified value
//     */
//    public static <T> T getRandomNotInList(final Collection<T> list, final Collection<T> not) {
//        if (_legacy) {
//            ArrayList<T> l = new ArrayList<>(list);
//            l.removeAll(not);
//            return getRandom(l);
//        } else {
//            return getRandom(org.apache.commons.collections4.CollectionUtils.subtract(list, not));
//        }
//    }
//
//    /**
//     * Gets a random item from a collection that is NOT equal to the specified
//     * collection
//     *
//     * @param <T> the type
//     * @param random
//     * @param list the collection to select from
//     * @param not the collection from which we do not want a duplicate selection
//     * of
//     * @return a new randomly selected object which is not equal to the
//     * specified value
//     */
//    public static <T> T getRandomNotInList(java.util.Random random, final Collection<T> list, final Collection<T> not) {
//        if (_legacy) {
//            ArrayList<T> l = new ArrayList<>(list);
//            l.removeAll(not);
//            return getRandom(random, l);
//        } else {
//            return getRandom(random, org.apache.commons.collections4.CollectionUtils.subtract(list, not));
//        }
//    }
//
//    /**
//     * Gets a random item from the specified collection
//     *
//     * @param <T> the type
//     * @param list the specified collection
//     * @return a randomly selected object from the collection
//     */
//    public static <T> T getRandom(final Collection<T> list) {
//        if (list.isEmpty()) {
//            return null;
//        }
//        if (_legacy) {
//            java.util.Random instance = getInstance();
//            int index = com.artistech.utils.Random.nextInt(instance, list.size());
//            Iterator<T> it = list.iterator();
//            int count = 0;
//            while (it.hasNext()) {
//                T ret = it.next();
//                if (count == index) {
//                    return ret;
//                }
//                count++;
//            }
//            return null;
//        } else {
//            java.util.Random instance = getInstance();
//            final int index = com.artistech.utils.Random.nextInt(instance, list.size());
//            return org.apache.commons.collections4.CollectionUtils.get(list, index);
//        }
//    }
//
//    public static <T> T getRandom(java.util.Random random, final Collection<T> list) {
//        if (list.isEmpty()) {
//            return null;
//        }
//        if (_legacy) {
//            int index = com.artistech.utils.Random.nextInt(random, list.size());
//            Iterator<T> it = list.iterator();
//            int count = 0;
//            while (it.hasNext()) {
//                T ret = it.next();
//                if (count == index) {
//                    return ret;
//                }
//                count++;
//            }
//            return null;
//        } else {
//            final int index = com.artistech.utils.Random.nextInt(random, list.size());
//            return org.apache.commons.collections4.CollectionUtils.get(list, index);
//        }
//    }

    /**
     * Is the random object specified to be seeded for repeatability.
     *
     * @return Is the random object specified to be seeded for repeatability.
     */
    public static boolean isSeeded() {
        return _doSeed;
    }

    public static void setSeeded(boolean value) {
        _doSeed = value;
    }

    public static int getSeed() {
        return _seedValue;
    }

    public static void setSeed(int value) {
        _seedValue = value;
    }
}