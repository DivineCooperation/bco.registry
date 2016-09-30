package org.openbase.bco.registry.unit.core.consistency.agent;

/*
 * #%L
 * BCO Registry Agent Core
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
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import rst.homeautomation.control.agent.AgentClassType.AgentClass;
import rst.homeautomation.control.agent.AgentConfigType.AgentConfig;
import rst.homeautomation.control.agent.AgentRegistryDataType.AgentRegistryData;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class AgentConfigAgentClassIdConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, UnitConfig, UnitConfig.Builder> {

    private final ProtoBufFileSynchronizedRegistry<String, AgentClass, AgentClass.Builder, AgentRegistryData.Builder> agentClassRegistry;

    public AgentConfigAgentClassIdConsistencyHandler(final ProtoBufFileSynchronizedRegistry<String, AgentClass, AgentClass.Builder, AgentRegistryData.Builder> agentClassRegistry) {
        this.agentClassRegistry = agentClassRegistry;
    }

    @Override
    public void processData(final String id, final IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry, final ProtoBufMessageMap<String, UnitConfig, UnitConfig.Builder> entryMap, final ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> registry) throws CouldNotPerformException, EntryModification {
        AgentConfig agentConfig = entry.getMessage().getAgentConfig();

        if (!agentConfig.hasAgentClassId() || agentConfig.getAgentClassId().isEmpty()) {
            throw new NotAvailableException("agentclass.id");
        }

        // get throws a CouldNotPerformException if the agent class with the id does not exists
        agentClassRegistry.get(agentConfig.getAgentClassId());
    }
}
