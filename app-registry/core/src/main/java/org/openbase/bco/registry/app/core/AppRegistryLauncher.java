package org.openbase.bco.registry.app.core;

/*
 * #%L
 * REM AppRegistry Core
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
import org.openbase.bco.registry.app.lib.AppRegistry;
import org.openbase.bco.registry.app.lib.jp.JPAppClassDatabaseDirectory;
import org.openbase.bco.registry.app.lib.jp.JPAppRegistryScope;
import static org.openbase.bco.registry.lib.launch.AbstractLauncher.main;
import org.openbase.bco.registry.lib.launch.AbstractRegistryLauncher;
import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jps.preset.JPForce;
import org.openbase.jps.preset.JPReadOnly;
import org.openbase.jul.storage.registry.jp.JPGitRegistryPlugin;
import org.openbase.jul.storage.registry.jp.JPGitRegistryPluginRemoteURL;
import org.openbase.jul.storage.registry.jp.JPInitializeDB;
import org.openbase.jul.storage.registry.jp.JPRecoverDB;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class AppRegistryLauncher extends AbstractRegistryLauncher<AppRegistryController> {

    public AppRegistryLauncher() throws org.openbase.jul.exception.InstantiationException {
        super(AppRegistry.class, AppRegistryController.class);
    }

    @Override
    public void loadProperties() {
        JPService.registerProperty(JPAppRegistryScope.class);
        JPService.registerProperty(JPReadOnly.class);
        JPService.registerProperty(JPForce.class);
        JPService.registerProperty(JPDebugMode.class);
        JPService.registerProperty(JPRecoverDB.class);
        JPService.registerProperty(JPInitializeDB.class);
        JPService.registerProperty(JPAppClassDatabaseDirectory.class);
        JPService.registerProperty(JPGitRegistryPlugin.class);
        JPService.registerProperty(JPGitRegistryPluginRemoteURL.class);
    }

    public static void main(String args[]) throws Throwable {
        main(args, AppRegistry.class, AppRegistryLauncher.class);
    }
}
