package org.openbase.bco.registry.user.activity.lib.jp;

/*-
 * #%L
 * BCO Registry User Activity Library
 * %%
 * Copyright (C) 2014 - 2017 openbase.org
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

import java.io.File;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.storage.registry.jp.AbstractJPDatabaseDirectory;
import org.openbase.jul.storage.registry.jp.JPDatabaseDirectory;
import org.openbase.jul.storage.registry.jp.JPInitializeDB;

/**
 *
 * @author <a href="mailto:pLeminoq@openbase.org">Tamino Huxohl</a>
 */
public class JPUserActivityConfigDatabaseDirectory extends AbstractJPDatabaseDirectory {

    public final static String[] COMMAND_IDENTIFIERS = {"--user-activity-config-db"};

    public JPUserActivityConfigDatabaseDirectory() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public File getParentDirectory() throws JPNotAvailableException {
        return JPService.getProperty(JPDatabaseDirectory.class).getValue();
    }

    @Override
    protected File getPropertyDefaultValue() {
        return new File("user-activity-config-db");
    }

    @Override
    public String getDescription() {
        return "Specifies the user activity config database directory. Use  " + JPInitializeDB.COMMAND_IDENTIFIERS[0] + " to auto create database directories.";
    }
}