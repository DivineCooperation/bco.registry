package org.openbase.bco.registry.unit.core.consistency.unitgroupconfig;

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
import java.util.ArrayList;
import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.unitgroup.UnitGroupConfigType.UnitGroupConfig;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class UnitGroupMemberListDuplicationConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, UnitConfig, UnitConfig.Builder> {

    @Override
    public void processData(String id, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry, ProtoBufMessageMap<String, UnitConfig, UnitConfig.Builder> entryMap, ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> registry) throws CouldNotPerformException, EntryModification {
        UnitConfig.Builder unitGroupUnitConfig = entry.getMessage().toBuilder();
        UnitGroupConfig.Builder unitGroupConfig = unitGroupUnitConfig.getUnitGroupConfigBuilder();

        unitGroupConfig.clearMemberId();
        boolean modification = false;
        List<String> memberIds = new ArrayList<>();
        for (String memberId : entry.getMessage().getUnitGroupConfig().getMemberIdList()) {
            if (!memberIds.contains(memberId)) {
                memberIds.add(memberId);
            } else {
                modification = true;
            }
        }

        if (modification) {
            unitGroupConfig.addAllMemberId(memberIds);
            throw new EntryModification(entry.setMessage(unitGroupUnitConfig, this), this);
        }
    }
}
