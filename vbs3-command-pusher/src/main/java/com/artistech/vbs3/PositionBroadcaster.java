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

import com.artistech.geo.GridConversionPoint;

/**
 *
 * @author matta
 */
public interface PositionBroadcaster {

    void broadcastPosition(Vbs3Protos.Position pos);

    /**
     * See if we are to do the grid conversion.
     *
     * @return
     */
    boolean getDoGridConversion();

    /**
     * Get the maximum conversion point.
     *
     * @return
     */
    GridConversionPoint getMaxGridConversionPoint();

    /**
     * Get the minimum conversion point.
     *
     * @return
     */
    GridConversionPoint getMinGridConversionPoint();

    void initialize();

//    /**
//     * Set if we are to do the grid conversion.
//     *
//     * @param value
//     */
//    void setDoGridConversion(boolean value);

    /**
     * Set the maximum conversion point.
     *
     * @param value
     */
    void setMaxGridConversionPoint(GridConversionPoint value);

    /**
     * Set the minimum conversion point.
     *
     * @param value
     */
    void setMinGridConversionPoint(GridConversionPoint value);
    
}
