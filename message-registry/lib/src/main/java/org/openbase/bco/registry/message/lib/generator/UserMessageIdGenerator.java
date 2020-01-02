package org.openbase.bco.registry.message.lib.generator;

/*
 * #%L
 * BCO Registry Message Library
 * %%
 * Copyright (C) 2014 - 2020 openbase.org
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

import org.openbase.bco.registry.lib.generator.UUIDGenerator;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.type.domotic.communication.UserMessageType.UserMessage;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class UserMessageIdGenerator extends UUIDGenerator<UserMessage> {

    @Override
    protected String getCustomTestId(final UserMessage userMessage) {
        final String customTestId = userMessage.getId();
        return StringProcessor.transformUpperCaseToPascalCase(userMessage.getMessageType().name()).toLowerCase() + (customTestId != null && !customTestId.isEmpty() ? "-" + customTestId.toLowerCase() : "");
    }
}
