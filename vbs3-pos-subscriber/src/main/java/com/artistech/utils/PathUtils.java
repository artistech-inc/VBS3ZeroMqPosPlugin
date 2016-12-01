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
 * Path access utils.
 *
 * @author matta
 */
public final class PathUtils {

    /**
     * Hidden Constructor.
     */
    private PathUtils() {
    }

    /**
     * Get the specified home directory. This value is read from the System
     * property "artistech.home" first. If this value is not specified, it uses
     * "user.home". If this value cannot be written to (in the case of being
     * used by tomcat), then the "java.io.tmpdir" value is used.
     *
     * @return A string value for a path that can be written to.
     */
    public static String getHomeDir() {
        return getDir("artistech.home");
    }

    /**
     * Get the specified property directory.
     *
     * @param prop
     * @return
     */
    public static String getDir(String prop) {
        String dir = System.getProperty(prop);
        if (dir == null) {
            dir = System.getProperty("user.home");
        }
        java.io.File f = new java.io.File(dir);
        if (!f.canWrite()) {
            dir = System.getProperty("java.io.tmpdir");
        }
        return dir;
    }

    /**
     * Get the temp dir.
     *
     * @return
     */
    public static String getTempDir() {
        String dir = System.getProperty("algolink.temp");
        if (dir == null) {
            dir = System.getProperty("java.io.tmpdir");
        }
        return dir;
    }

    /**
     * Get the specified Application directory. This directory is based on the
     * getHomeDir() return value.
     *
     * @param bi Application description.
     * @return A string value for a path that can be written to for application
     * specific data.
     */
    public static String getApplicationDirectory(IBuildInfo bi) {
        String home = System.getProperty("aglolink.home");
        String ret;
        if (home == null) {
            home = getHomeDir();
            String sep = System.getProperty("file.separator");
            //this should come out to something like: /home/matta/.ArtisTech/AlgoLink
            ret = String.format("%s%s.%s%s%s", home, sep, bi.getCompany(), sep, bi.getApplicationName());
        } else {
            ret = home;
        }
        java.io.File dir = new java.io.File(ret);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return ret;
    }

    /**
     * Get the etc dir.
     *
     * @param bi
     * @return
     */
    public static String getApplicationEtcDirectory(IBuildInfo bi) {
        String home = System.getProperty("algolink.etc");
        String ret;
        if (home == null) {
            home = getHomeDir();
            String sep = System.getProperty("file.separator");
            //this should come out to something like: /home/matta/.ArtisTech/AlgoLink
            ret = String.format("%s%s.%s%s%s", home, sep, bi.getCompany(), sep, bi.getApplicationName());
        } else {
            ret = home;
        }
        java.io.File dir = new java.io.File(ret);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return ret;
    }

    /**
     * Get the lib dir.
     * @param bi
     * @return 
     */
    public static String getApplicationLibDirectory(IBuildInfo bi) {
        String home = System.getProperty("algolink.lib");
        String ret;
        if (home == null) {
            home = getApplicationEtcDirectory(bi);
            String sep = System.getProperty("file.separator");
            //this should come out to something like: /home/matta/.ArtisTech/AlgoLink
            ret = String.format("%s%slib", home, sep);
        } else {
            ret = home;
        }
        java.io.File dir = new java.io.File(ret);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return ret;
    }

    /**
     * Get the Conf directory. This directory is based on the
     * getApplicationDir() return value.
     *
     * @param bi Application description.
     * @return A string value for a path that can be written to for log files.
     */
    public static String getApplicationConfigDirectory(IBuildInfo bi) {
        String home = System.getProperty("aglolink.conf");
        String ret;
        if (home == null) {
            home = getApplicationEtcDirectory(bi);
            String sep = System.getProperty("file.separator");
            //this should come out to something like: /home/matta/.ArtisTech/AlgoLink
            ret = String.format("%s%sconf", home, sep);
        } else {
            ret = home;
        }
        java.io.File dir = new java.io.File(ret);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return ret;
    }

    /**
     * Get the Log directory. This directory is based on the getApplicationDir()
     * return value.
     *
     * @param bi Application description.
     * @return A string value for a path that can be written to for log files.
     */
    public static String getLogDirectory(IBuildInfo bi) {
        String home = System.getProperty("aglolink.log");
        String ret;
        if (home == null) {
            home = getApplicationDirectory(bi);
            String sep = System.getProperty("file.separator");
            //this should come out to something like: /home/matta/.ArtisTech/AlgoLink
            ret = String.format("%s%slog", home, sep);
        } else {
            ret = home;
        }
        java.io.File dir = new java.io.File(ret);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return ret;
    }

}
