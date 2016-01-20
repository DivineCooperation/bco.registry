/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.registry.device.lib.generator;

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