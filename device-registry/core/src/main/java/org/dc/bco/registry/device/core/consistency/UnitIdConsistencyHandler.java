/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.registry.device.core.consistency;

import org.dc.bco.registry.device.lib.generator.UnitConfigIdGenerator;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import org.dc.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.dc.jul.storage.registry.EntryModification;
import org.dc.jul.storage.registry.ProtoBufRegistryInterface;
import java.util.Map;
import java.util.TreeMap;
import rst.homeautomation.device.DeviceConfigType;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author mpohling
 */
public class UnitIdConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, DeviceConfig, DeviceConfig.Builder> {

    private final Map<String, UnitConfig> unitMap;

    public UnitIdConsistencyHandler() throws InstantiationException {
        this.unitMap = new TreeMap<>();
    }

    @Override
    public void processData(String id, IdentifiableMessage<String, DeviceConfig, DeviceConfig.Builder> entry, ProtoBufMessageMapInterface<String, DeviceConfig, DeviceConfig.Builder> entryMap, ProtoBufRegistryInterface<String, DeviceConfig, DeviceConfig.Builder> registry) throws CouldNotPerformException, EntryModification {
        DeviceConfigType.DeviceConfig.Builder deviceConfig = entry.getMessage().toBuilder();

        deviceConfig.clearUnitConfig();
        boolean modification = false;
        for (UnitConfig.Builder unitConfig : entry.getMessage().toBuilder().getUnitConfigBuilderList()) {
            if (!unitConfig.hasId() || unitConfig.getId().isEmpty() || !unitConfig.getId().equals(UnitConfigIdGenerator.getInstance().generateId(unitConfig.build()))) {
                unitConfig.setId(UnitConfigIdGenerator.getInstance().generateId(unitConfig.build()));
                modification = true;
            }

            // check if unit id is unique.
            if (unitMap.containsKey(unitConfig.getId())) {
                throw new InvalidStateException("Two units with same Id[" + unitConfig.getId() + "] detected provided by Device[" + deviceConfig.getId() + "] and Device[" + unitMap.get(unitConfig.getId()).getDeviceId() + "]!");
            }
            unitMap.put(unitConfig.getId(), unitConfig.build());
            deviceConfig.addUnitConfig(unitConfig);
        }

        if (modification) {
            throw new EntryModification(entry.setMessage(deviceConfig), this);
        }
    }

    @Override
    public void reset() {
        unitMap.clear();
    }
}