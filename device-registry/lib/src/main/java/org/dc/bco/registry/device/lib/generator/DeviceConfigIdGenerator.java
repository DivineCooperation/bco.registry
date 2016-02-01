/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.registry.device.lib.generator;

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

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.extension.protobuf.IdGenerator;
import org.dc.jul.processing.StringProcessor;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;

/**
 *
 * @author mpohling
 */
public class DeviceConfigIdGenerator implements IdGenerator<String, DeviceConfig> {

    @Override
    public String generateId(DeviceConfig message) throws CouldNotPerformException {
        try {
            if (!message.hasDeviceClassId()) {
                throw new InvalidStateException("Field [DeviceClassId] is missing!");
            }

            if (message.getDeviceClassId().isEmpty()) {
                throw new InvalidStateException("Field [DeviceClass.id] is empty!");
            }

            if (!message.hasSerialNumber()) {
                throw new InvalidStateException("Field [SerialNumber] is missing!");
            }

            if (message.getSerialNumber().isEmpty()) {
                throw new InvalidStateException("Field [SerialNumber] is empty!");
            }

            String id;

            id = message.getDeviceClassId();
            id += "_";
            id += message.getSerialNumber();
            return StringProcessor.transformToIdString(id);

        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate id!", ex);
        }
    }

}
