/*
 * Collectable.java
 *
 * Copyright (c) 2002-2016 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.inference.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ${CLASS_NAME}
 *
 * @author Andrew Rambaut
 * @version $Id$
 * <p/>
 * $HeadURL$
 * <p/>
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */
public interface Collectable {
    /**
     * This function should be called to store the model state to a map
     * @param stateMap
     */
    void saveModelState(Map<String, Map<String, Object>> stateMap);

    /**
     * This function should be called to load the model state from a map
     * @param stateMap
     */
    void loadModelState(Map<String, Map<String, Object>> stateMap);

    // set to store all created collectables
    Set<Collectable> FULL_SET = new HashSet<Collectable>();
}
