/*
 * Copyright 2015 ArtisTech, Inc.
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

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
/**
 *
 * @author matta
 */
public class Jetty {

    private static Server server = null;//new Server(PORT);
    private static final Logger logger = Logger.getLogger(Jetty.class.getName());
    private static WebAppContext webapp;
    private static int _port;

    public static int getPort() {
        return _port;
    }

    public static void addJettyServlet(Class<? extends HttpServlet> x) {
        addJettyServlet(x, "");
    }

    public static void addJettyServlet(Class<? extends HttpServlet> x, String suffix) {
        try {
            addJettyServlet(x.newInstance(), suffix);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.log(Level.WARNING, "Could Not Load: {0}", x.getName());
        }
    }

    public static void addJettyServlet(HttpServlet servelet) {
        addJettyServlet(servelet, "");
    }

    public static void addJettyServlet(HttpServlet servelet, String suffix) {
        if (webapp != null) {
            Class<? extends HttpServlet> x = servelet.getClass();
            WebServlet annotation = x.getAnnotation(WebServlet.class);
            if (annotation != null) {
                logger.log(Level.FINER, "Loading Servlet: {0}", x.getName());
                ServletHolder mainHolder2 = new ServletHolder(servelet);
                MultipartConfig mc = x.getAnnotation(MultipartConfig.class);
                if (mc != null) {
                    mainHolder2.getRegistration().setMultipartConfig(new MultipartConfigElement(mc.location(), mc.maxFileSize(), mc.maxRequestSize(), mc.fileSizeThreshold()));
                } else {
                    mainHolder2.getRegistration().setMultipartConfig(new MultipartConfigElement("data/tmp", 1048576, 1048576, 262144));
                }
                for (String y : annotation.urlPatterns()) {
                    logger.log(Level.FINER, "Loading URL: {0}", y + suffix);
                    webapp.addServlet(mainHolder2, y + suffix);
                }
            }
        }
    }

    public static void startServer(int port) {
        try {
            boolean success = false;
            for (int ii = 0; ii < 10 && !success; ii++) {
                try {
                    server = new Server(ii + port);
                    server.start();
                    server.stop();

                    success = true;
                    _port = ii + port;
                    //JettyConfig.Instance.setPort(ii + port);
                } catch (java.net.BindException ex2) {
                    server = null;
                }
            }

            if (server == null) {
                //logger.log(Level.SEVERE, "Unable to start JeTTY, all ports in range are used: ({0} - {1})", new Object[]{JettyConfig.Instance.getPort(), JettyConfig.Instance.getPort() + JettyConfig.Instance.getMaxPortsToTry()});
                return;
            }

            webapp = new WebAppContext();
            server.setHandler(webapp);

            webapp.setServer(server);
            String webDir = ClassLoader.getSystemClassLoader().getResource("com/artistech/jetty/content").toExternalForm();
            webapp.setResourceBase(webDir);
            webapp.setContextPath("/");

            ServiceLoader<ServletContextListener> listeners = ServiceLoader.load(ServletContextListener.class);

            for (ServletContextListener x : listeners) {
                logger.log(Level.INFO, "Loading Listener: {0}", x.getClass().getName());
                webapp.addEventListener(x);
            }

            ServiceLoader<HttpServlet> servlets = ServiceLoader.load(HttpServlet.class);

            for (HttpServlet x : servlets) {
                addJettyServlet(x);
            }

            server.start();
        } catch (Exception ex) {
            Logger.getLogger(Jetty.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

        }
    }

    public static void stopServer() {
        try {
            server.stop();
        } catch (Exception ex) {
            Logger.getLogger(Jetty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
