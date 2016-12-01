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
package com.artistech.vbs3.kml;

import com.artistech.vbs3.Vbs3Protos;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author matta
 */
@SuppressWarnings("serial")
@WebServlet(name = "Vbs3Pos", urlPatterns = {"/vbs3Pos/*"})
public class Vbs3Pos extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Collection<Vbs3Protos.Position> positions = KmlBroadcaster.getPositions();

        response.setContentType("application/vnd.google-earth.kml+xml");
        try (PrintWriter out = response.getWriter()) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">");
            out.println("<Document>");
            for (Vbs3Protos.Position pos : positions) {
                String[] lat = pos.getLat().replaceAll("\"", "").split(" ");
                String[] lon = pos.getLon().replaceAll("\"", "").split(" ");
                Double latd = Double.parseDouble(lat[0]);
                //South is negative
                latd = lat[1].toLowerCase().equals("n") ? latd : latd * -1.0;
                //East is negative
                Double lond = Double.parseDouble(lon[0]);
                lond = lon[1].toLowerCase().equals("w") ? lond : lond * -1.0;

                String name = pos.getId().replaceAll("\"", "");
                out.println("<Placemark>");
                out.println("<name>Player: " + name + "</name>");
                out.println("<Point><coordinates>" +lond + "," + latd + "</coordinates></Point>");
                out.println("</Placemark>");
            }
            out.println("</Document>");
            out.println("</kml>");
            out.flush();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
