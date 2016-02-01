package org.dc.bco.registry.device.lib.provider;

/*
 * #%L
 * REM DeviceRegistry Library
 * %%
 * Copyright (C) 2014 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.dc.bco.registry.device.lib.DeviceRegistry;
import org.dc.jul.exception.NotAvailableException;

/**
 * Interface provides a globally managed device registry instance.
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface DeviceRegistryProvider {

    /**
     * Returns the globally managed device registry instance.
     * @return
     * @throws NotAvailableException 
     */
    public DeviceRegistry getDeviceRegistry() throws NotAvailableException;
}
