package org.openbase.bco.registry.location.core.consistency;

/*
 * #%L
 * REM LocationRegistry Core
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufRegistryInterface;
import rst.spatial.LocationConfigType.LocationConfig;

/**
 *
 * @author mpohling
 */
public class ChildWithSameLabelConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, LocationConfig, LocationConfig.Builder> {

    private final Map<String, String> labelConsistencyMap;

    public ChildWithSameLabelConsistencyHandler() {
        labelConsistencyMap = new HashMap<>();
    }

    @Override
    public void processData(String id, IdentifiableMessage<String, LocationConfig, LocationConfig.Builder> entry, ProtoBufMessageMapInterface<String, LocationConfig, LocationConfig.Builder> entryMap, ProtoBufRegistryInterface<String, LocationConfig, LocationConfig.Builder> registry) throws CouldNotPerformException, EntryModification {
        LocationConfig locationConfig = entry.getMessage();

        for (String childLocationId : new ArrayList<>(locationConfig.getChildIdList())) {
            LocationConfig childLocationConfig = registry.getMessage(childLocationId);

            if (labelConsistencyMap.containsKey(childLocationConfig.getLabel()) && !labelConsistencyMap.get(childLocationConfig.getLabel()).equals(childLocationId)) {
                throw new InvalidStateException("Location [" + locationConfig.getId() + "," + locationConfig.getLabel() + "] has more than on child with the same label [" + childLocationConfig.getLabel() + "]");
            } else {
                labelConsistencyMap.put(childLocationConfig.getLabel(), childLocationId);
            }
        }
    }

    @Override
    public void reset() {
        labelConsistencyMap.clear();
    }
}