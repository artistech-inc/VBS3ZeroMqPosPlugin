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

import com.artistech.utils.NetUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
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
    private static final String WEBROOT_INDEX = "/com/artistech/jetty/content/";

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

    private static File getScratchDir() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

        if (!scratchDir.exists()) {
            if (!scratchDir.mkdirs()) {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        return scratchDir;
    }

    /**
     * Ensure the jsp engine is initialized correctly
     */
    private static List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }

    /**
     * Set Classloader of Context to be sane (needed for JSTL) JSP requires a
     * non-System classloader, this simply wraps the embedded System classloader
     * in a way that makes it suitable for JSP to use
     */
    private static ClassLoader getUrlClassLoader() {
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], Jetty.class.getClassLoader());
        return jspClassLoader;
    }

    /**
     * Create JSP Servlet (must be named "jsp")
     */
    private static ServletHolder jspServletHolder() {
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.7");
        holderJsp.setInitParameter("compilerSourceVM", "1.7");
        holderJsp.setInitParameter("keepgenerated", "true");
        return holderJsp;
    }

    public static void startServer(int port) {
        // Set JSP to use Standard JavaC always
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
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

            //This bean is used in the JSP to allow access back to this server.
            String uri = NetUtils.getIP().toString() + ":" + Integer.toString(_port);
            JettyBean jb = new JettyBean();
            jb.setServer(uri.replace("/", ""));
            
            webapp = new WebAppContext();
            server.setHandler(webapp);

            webapp.setServer(server);
            String webDir = ClassLoader.getSystemClassLoader().getResource("com/artistech/jetty/content").toExternalForm();
            webapp.setResourceBase(webDir);
            webapp.setContextPath("/");

            //init support for JSP
            webapp.setAttribute("javax.servlet.context.tempdir", getScratchDir());
            webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                    ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/.*taglibs.*\\.jar$");
            webapp.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
            webapp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
            webapp.addBean(new ServletContainerInitializersStarter(webapp), true);
            webapp.setClassLoader(getUrlClassLoader());
            webapp.addServlet(jspServletHolder(), "*.jsp");

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
