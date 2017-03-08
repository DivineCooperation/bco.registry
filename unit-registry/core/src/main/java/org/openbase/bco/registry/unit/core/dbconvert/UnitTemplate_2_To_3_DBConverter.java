/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.registry.unit.core.dbconvert;

/*-
 * #%L
 * BCO Registry Unit Core
 * %%
 * Copyright (C) 2014 - 2017 openbase.org
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.Map;
import org.openbase.bco.registry.unit.lib.generator.UnitConfigIdGenerator;
import org.openbase.bco.registry.unit.lib.jp.JPUnitTemplateDatabaseDirectory;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.storage.file.filter.JSonFileFilter;
import org.openbase.jul.storage.registry.version.AbstractGlobalDBVersionConverter;
import org.openbase.jul.storage.registry.version.DBVersionControl;
import org.openbase.jul.storage.registry.version.DatabaseEntryDescriptor;

/**
 *
 * @author pleminoq
 */
public class UnitTemplate_2_To_3_DBConverter extends AbstractGlobalDBVersionConverter {

    private static final String DEVICE_CLASS_DB_ID = "device-class-db";
    private static final String DAL_UNIT_CONFIG_DB_ID = "dal-unit-config-db";
    private static final String UNIT_TEMPLAET_CONFIG_DB_ID = "unit-template-config-db";

    private static final String LIGHT_SENSOR_TYPE = "LIGHT_SENSOR";
    private static final String BRIGHTNESS_SENSOR_TYPE = "BRIGHTNESS_SENSOR";
    private static final String BRIGHTNESS_STATE_TYPE = "BRIGHTNESS_STATE_SERVICE";
    private static final String ILLUMINANCE_STATE_TYPE = "ILLUMINANCE_STATE_SERVICE";
    private static final String PROVIDER_PATTERN = "PROVIDER";

    private static final String TYPE_FIELD = "type";
    private static final String SERVICE_TYPE_FIELD = "service_type";
    private static final String ID_FIELD = "id";
    private static final String PATTERN_FIELD = "pattern";

    private static final String SERVICE_TEMPLATE_FIELD = "service_template";
    private static final String UNIT_TEMPLATE_CONFIG_FIELD = "unit_template_config";
    private static final String SERVICE_TEMPLATE_CONFIG_FIELD = "service_template_config";
    private static final String SERVICE_CONFIG_FIELD = "service_config";

    private boolean init;
    private final UnitConfigIdGenerator idGenerator;

    public UnitTemplate_2_To_3_DBConverter(DBVersionControl versionControl) {
        super(versionControl);
        init = false;
        idGenerator = new UnitConfigIdGenerator();
    }

    @Override
    public JsonObject upgrade(JsonObject outdatedDBEntry, Map<File, JsonObject> dbSnapshot, Map<String, Map<File, DatabaseEntryDescriptor>> globalDbSnapshots) throws CouldNotPerformException {
        if (!init) {
            // Add a unitTemplate with unitType LIGHT_SENSOR and serviceTemplate ILLUMINANCE_STATE_SERVICE, PROVIDER
            JsonObject unitTemplate = new JsonObject();
            String id = idGenerator.generateId(null);
            unitTemplate.addProperty(ID_FIELD, id);
            unitTemplate.addProperty(TYPE_FIELD, LIGHT_SENSOR_TYPE);

            JsonArray serviceTemplates = new JsonArray();
            JsonObject serviceTemplate = new JsonObject();
            serviceTemplate.addProperty(TYPE_FIELD, ILLUMINANCE_STATE_TYPE);
            serviceTemplate.addProperty(PATTERN_FIELD, PROVIDER_PATTERN);
            serviceTemplates.add(serviceTemplate);
            unitTemplate.add(SERVICE_TEMPLATE_FIELD, serviceTemplates);

            try {
                File unitTemplateDir = JPService.getProperty(JPUnitTemplateDatabaseDirectory.class).getValue();
                globalDbSnapshots.get(DAL_UNIT_CONFIG_DB_ID).put(new File(unitTemplateDir, id + JSonFileFilter.FILE_SUFFIX), new DatabaseEntryDescriptor(unitTemplate, getVersionControl()));
            } catch (JPNotAvailableException ex) {
                throw new CouldNotPerformException("Could not acces unitTemplate directory!", ex);
            }

            // Find all deviceClasses with unitTemplateConfig for unitType BRIGHTNESS_SENSOR and adjust their type and serviceTemplateConfig
            // TODO: this does not seem to work yet... does the unit registry just not save changes for this?
            if (!globalDbSnapshots.get(DEVICE_CLASS_DB_ID).isEmpty()) {
                for (DatabaseEntryDescriptor entry : globalDbSnapshots.get(DEVICE_CLASS_DB_ID).values()) {
                    JsonObject deviceClass = entry.getEntry();
                    if (!deviceClass.has(UNIT_TEMPLATE_CONFIG_FIELD)) {
                        continue;
                    }

                    for (JsonElement unitTemplateConfigElem : deviceClass.getAsJsonArray(UNIT_TEMPLATE_CONFIG_FIELD)) {
                        JsonObject unitTemplateConfig = unitTemplateConfigElem.getAsJsonObject();
                        if (unitTemplateConfig.get(TYPE_FIELD).getAsString().equals(BRIGHTNESS_SENSOR_TYPE)) {
                            unitTemplateConfig.remove(TYPE_FIELD);
                            unitTemplateConfig.addProperty(TYPE_FIELD, LIGHT_SENSOR_TYPE);

                            JsonObject serviceTemplateConfig = unitTemplateConfig.getAsJsonArray(SERVICE_TEMPLATE_CONFIG_FIELD).get(0).getAsJsonObject();
                            serviceTemplateConfig.remove(SERVICE_TYPE_FIELD);
                            serviceTemplateConfig.addProperty(SERVICE_TYPE_FIELD, ILLUMINANCE_STATE_TYPE);
                        }
                    }
                }
            }

            // Find all dalUnitConfigs with unitType BRIGHTNESS_SENSOR and adjust their type and serviceTemplateConfig
            if (!globalDbSnapshots.get(DAL_UNIT_CONFIG_DB_ID).isEmpty()) {
                for (DatabaseEntryDescriptor entry : globalDbSnapshots.get(DAL_UNIT_CONFIG_DB_ID).values()) {
                    JsonObject unitConfig = entry.getEntry();
                    if (unitConfig.get(TYPE_FIELD).getAsString().equals(BRIGHTNESS_SENSOR_TYPE)) {
                        unitConfig.remove(TYPE_FIELD);
                        unitConfig.addProperty(TYPE_FIELD, LIGHT_SENSOR_TYPE);

                        JsonObject serviceConfig = unitConfig.getAsJsonArray(SERVICE_CONFIG_FIELD).get(0).getAsJsonObject();
                        JsonObject serviceTemplate2 = serviceConfig.getAsJsonObject(SERVICE_TEMPLATE_FIELD);
                        serviceTemplate2.remove(TYPE_FIELD);
                        serviceTemplate2.addProperty(TYPE_FIELD, ILLUMINANCE_STATE_TYPE);
                    }
                }
            }

            init = true;
        }

        return outdatedDBEntry;
    }
}
