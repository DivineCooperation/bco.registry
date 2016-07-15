package org.openbase.bco.registry.device.core.consistency;

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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import org.openbase.jul.storage.registry.ProtoBufRegistryInterface;
import rst.homeautomation.device.DeviceRegistryDataType.DeviceRegistryData;
import rst.homeautomation.unit.UnitTemplateType;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine
 * Threepwood</a>
 */
public class UnitTemplateValidationConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, UnitTemplate, UnitTemplate.Builder> {

    private final ProtoBufFileSynchronizedRegistry<String, UnitTemplate, UnitTemplate.Builder, DeviceRegistryData.Builder> unitTemplateRegistry;

    public UnitTemplateValidationConsistencyHandler(ProtoBufFileSynchronizedRegistry<String, UnitTemplateType.UnitTemplate, UnitTemplateType.UnitTemplate.Builder, DeviceRegistryData.Builder> unitTemplateRegistry) {
        this.unitTemplateRegistry = unitTemplateRegistry;
    }

    @Override
    public void processData(String id, IdentifiableMessage<String, UnitTemplate, UnitTemplate.Builder> entry, ProtoBufMessageMapInterface<String, UnitTemplate, UnitTemplate.Builder> entryMap, ProtoBufRegistryInterface<String, UnitTemplate, UnitTemplate.Builder> registry) throws CouldNotPerformException, EntryModification {
        UnitTemplate.Builder unitTemplate = entry.getMessage().toBuilder();

        // remove invalid unit template
        if (!unitTemplate.getId().equals(unitTemplateRegistry.getIdGenerator().generateId(unitTemplate.build()))) {
            registry.remove(entry);
            throw new EntryModification(entry, this);
        }
    }
}
