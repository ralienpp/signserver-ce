/*************************************************************************
 *                                                                       *
 *  SignServer: The OpenSource Automated Signing Server                  *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.signserver.common;

/**
 * Statistics collector for the QoS web filter.
 * 
 * @author Marcus Lundblad
 * @version $Id$
 */
public abstract class AbstractQoSFilterStatistics {

    protected static AbstractQoSFilterStatistics instance;

    public static AbstractQoSFilterStatistics getInstance() {
        return instance;
    }
    
    public abstract int getMaxPriorityLevel();

    public abstract int getMaxRequests();

    public abstract int getQueueSizeForPriorityLevel(final int priorityLevel);

    public abstract boolean getFilterEnabled();
}
