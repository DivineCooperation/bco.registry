package org.openbase.bco.registry.unit.core.consistency.authorizationgroup;

/*
 * #%L
 * BCO Registry Unit Core
 * %%
 * Copyright (C) 2014 - 2018 openbase.org
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
import java.util.HashMap;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class AuthorizationGroupConfigLabelConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, UnitConfig, UnitConfig.Builder> {

    private final Map<String, UnitConfig> authorizatoinGroupMap;

    public AuthorizationGroupConfigLabelConsistencyHandler() {
        this.authorizatoinGroupMap = new HashMap<>();
    }

    @Override
    public void processData(String id, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry, ProtoBufMessageMap<String, UnitConfig, UnitConfig.Builder> entryMap, ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> registry) throws CouldNotPerformException, EntryModification {
        UnitConfig authorizationGroupUnitConfig = entry.getMessage();

        if (!authorizationGroupUnitConfig.hasLabel() || authorizationGroupUnitConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("authorizationGroup.label");
        }

        if (!authorizatoinGroupMap.containsKey(authorizationGroupUnitConfig.getLabel())) {
            authorizatoinGroupMap.put(authorizationGroupUnitConfig.getLabel(), authorizationGroupUnitConfig);
        } else {
            throw new InvalidStateException("AuthorizationGroup [" + authorizationGroupUnitConfig + "] and authorizationGroup [" + authorizatoinGroupMap.get(authorizationGroupUnitConfig.getLabel()) + "] are registered with the same label!");
        }
    }

    @Override
    public void reset() {
        authorizatoinGroupMap.clear();
    }
}
