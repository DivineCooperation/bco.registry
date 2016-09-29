package org.openbase.bco.registry.unit.core.consistency.dal;

/*
 * #%L
 * REM DeviceRegistryData Core
 * %%
 * Copyright (C) 2014 - 2016 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.bco.registry.lib.util.DeviceConfigUtils;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import org.openbase.jul.storage.registry.Registry;
import rst.homeautomation.device.DeviceClassType.DeviceClass;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.homeautomation.unit.UnitRegistryDataType.UnitRegistryData;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class DalUnitLabelConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, UnitConfig, UnitConfig.Builder> {

    private final Registry<String, IdentifiableMessage<String, DeviceClass, DeviceClass.Builder>> deviceClassRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> deviceRegistry;

    public DalUnitLabelConsistencyHandler(final Registry<String, IdentifiableMessage<String, DeviceClass, DeviceClass.Builder>> deviceClassRegistry,
            final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> deviceRegistry) {
        this.deviceClassRegistry = deviceClassRegistry;
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public void processData(String id, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry, ProtoBufMessageMap<String, UnitConfig, UnitConfig.Builder> entryMap, ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> registry) throws CouldNotPerformException, EntryModification {
        UnitConfig.Builder dalUnitConfig = entry.getMessage().toBuilder();

        if (!dalUnitConfig.hasUnitHostId() || dalUnitConfig.getUnitHostId().isEmpty()) {
            throw new NotAvailableException("unitConfig.unitHostId");
        }

        UnitConfig deviceUnitConfig = deviceRegistry.getMessage(dalUnitConfig.getUnitHostId());

        boolean hasDuplicatedUnitType = DeviceConfigUtils.checkDuplicatedUnitType(deviceUnitConfig, registry);

        // Setup device label if unit has no label configured.
        if (!dalUnitConfig.hasLabel() || dalUnitConfig.getLabel().isEmpty()) {
            if (DeviceConfigUtils.setupUnitLabelByDeviceConfig(dalUnitConfig, deviceUnitConfig, deviceClassRegistry.get(deviceUnitConfig.getDeviceConfig().getDeviceClassId()).getMessage(), hasDuplicatedUnitType)) {
                throw new EntryModification(entry.setMessage(dalUnitConfig), this);
            }
        }
    }
}
