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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used as a wrapper around org.reflections.Reflections so that we
 * can easily add new jar files to the class path that can be analyzed by the
 * reflections package for loading new classes/types.
 *
 * @author matta
 */
public final class ReflectionsFactory {

    private static final Class[] PARAMETERS = new Class[]{URL.class};

    /**
     * Hidden Constructor.
     */
    private ReflectionsFactory() {
    }

    /**
     * Adds a new JAR file to the CLASSPATH
     *
     * @param s
     * @throws IOException
     */
    public static void addFile(final String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Adds a new JAR file to the CLASSPATH
     *
     * @param f
     * @throws IOException
     */
    public static void addFile(final File f) throws IOException {
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            for (File f2 : files) {
                if (f2.getName().toLowerCase().endsWith(".jar")) {
                    addURL(f2.toURI().toURL());
                }
            }
        } else if (f.exists()) {
            addURL(f.toURI().toURL());
        }
    }

    /**
     * Adds a new URL to a JAR file to the CLASSPATH
     *
     * @param u
     * @throws IOException
     */
    public static void addURL(final URL u) throws IOException {

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", PARAMETERS);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
            Logger.getLogger(ReflectionsFactory.class.getName()).log(Level.INFO, "Loaded Jar: {0}", new Object[]{u});
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException t) {
            Logger.getLogger(ReflectionsFactory.class.getName()).log(Level.SEVERE, null, t);
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
