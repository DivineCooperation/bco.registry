/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.registry.location.core.dbconvert;

/*
 * #%L
 * REM LocationRegistry Core
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
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.util.Map;
import org.dc.jul.storage.registry.version.DBVersionConverter;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class LocationConfig_3_To_4_DBConverter implements DBVersionConverter {

    @Override
    public JsonObject upgrade(JsonObject locationConfig, final Map<File, JsonObject> dbSnapshot) {
        // remove position
        if (locationConfig.getAsJsonObject("position") != null) {
            locationConfig.remove("position");
        }

        if (locationConfig.getAsJsonPrimitive("parent_id") == null) {
            return locationConfig;
        }

        String parentId = locationConfig.getAsJsonPrimitive("parent_id").getAsString();
        JsonObject placement;
        if (locationConfig.getAsJsonObject("placement_config") == null) {
            placement = new JsonObject();
        } else {
            placement = locationConfig.getAsJsonObject("placement_config");
        }

        for (JsonObject location : dbSnapshot.values()) {
            if (location.getAsJsonPrimitive("id").getAsString().equals(parentId)) {
                //parent exists
                placement.remove("location_id");
                placement.add("location_id", new JsonPrimitive(parentId));
            }
        }
        locationConfig.remove("parent_id");
        locationConfig.remove("placement_config");
        locationConfig.add("placement_config", placement);

        return locationConfig;
    }
}
