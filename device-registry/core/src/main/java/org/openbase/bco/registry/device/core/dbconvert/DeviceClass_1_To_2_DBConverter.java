package org.openbase.bco.registry.device.core.dbconvert;

/*
 * #%L
 * BCO Registry Device Core
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
import com.google.gson.JsonObject;
import java.io.File;
import java.util.Map;
import org.openbase.bco.registry.device.lib.generator.DeviceClassIdGenerator;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.storage.registry.version.AbstractDBVersionConverter;
import org.openbase.jul.storage.registry.version.DBVersionControl;

/**
 * Converter which replaces the old DeviceClass IDs with UUIDs.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class DeviceClass_1_To_2_DBConverter extends AbstractDBVersionConverter {

    private static final String ID_FIELD = "id";

    private final DeviceClassIdGenerator idGenerator;

    public DeviceClass_1_To_2_DBConverter(DBVersionControl versionControl) {
        super(versionControl);
        idGenerator = new DeviceClassIdGenerator();
    }

    @Override
    public JsonObject upgrade(JsonObject deviceClass, Map<File, JsonObject> dbSnapshot) throws CouldNotPerformException {
        deviceClass.remove(ID_FIELD);
        deviceClass.addProperty(ID_FIELD, idGenerator.generateId(null));
        return deviceClass;
    }
}
