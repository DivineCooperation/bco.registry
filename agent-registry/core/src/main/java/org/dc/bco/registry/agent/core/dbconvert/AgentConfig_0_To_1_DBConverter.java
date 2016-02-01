/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.registry.agent.core.dbconvert;

/*
 * #%L
 * REM AgentRegistry Core
 * %%
 * Copyright (C) 2014 - 2016 DivineCooperation
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

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Map;
import org.dc.jul.storage.registry.version.DBVersionConverter;
import rst.homeautomation.state.ActivationStateType.ActivationState;
import rst.homeautomation.state.EnablingStateType.EnablingState;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class AgentConfig_0_To_1_DBConverter implements DBVersionConverter {

    @Override
    public JsonObject upgrade(JsonObject agentConfig, final Map<File, JsonObject> dbSnapshot) {
        // remove activation state and use it to set up the enabling state
        if (agentConfig.has("activation_state")) {
            JsonObject activationState = agentConfig.getAsJsonObject("activation_state");
            agentConfig.remove("activation_state");
            JsonObject enablingState = new JsonObject();
            if (activationState.has("value")) {
                EnablingState.State enablingValue = EnablingState.State.ENABLED;
                ActivationState.State activationValue = ActivationState.State.valueOf(activationState.getAsJsonPrimitive("value").getAsString());
                switch (activationValue) {
                    case ACTIVE:
                        enablingValue = EnablingState.State.ENABLED;
                        break;
                    case DEACTIVE:
                        enablingValue = EnablingState.State.DISABLED;
                        break;
                    case UNKNOWN:
                        enablingValue = EnablingState.State.UNKNOWN;
                }
                enablingState.addProperty("value", enablingValue.toString());
            }
            agentConfig.add("enabling_state", enablingState);
        }

        return agentConfig;
    }
}
