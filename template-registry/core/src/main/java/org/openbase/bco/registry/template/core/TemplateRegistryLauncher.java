package org.openbase.bco.registry.template.core;

/*
 * #%L
 * BCO Registry Scene Core
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

import org.openbase.bco.registry.lib.BCO;
import org.openbase.bco.registry.lib.launch.AbstractRegistryLauncher;
import org.openbase.bco.registry.template.lib.TemplateRegistry;
import org.openbase.bco.registry.template.lib.jp.JPActivityTemplateDatabaseDirectory;
import org.openbase.bco.registry.template.lib.jp.JPServiceTemplateDatabaseDirectory;
import org.openbase.bco.registry.template.lib.jp.JPTemplateRegistryScope;
import org.openbase.bco.registry.template.lib.jp.JPUnitTemplateDatabaseDirectory;
import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jps.preset.JPForce;
import org.openbase.jps.preset.JPReadOnly;
import org.openbase.jul.extension.rsb.com.jp.JPRSBHost;
import org.openbase.jul.extension.rsb.com.jp.JPRSBPort;
import org.openbase.jul.extension.rsb.com.jp.JPRSBTransport;
import org.openbase.jul.pattern.launch.AbstractLauncher;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TemplateRegistryLauncher extends AbstractRegistryLauncher<TemplateRegistryController> {

    public TemplateRegistryLauncher() throws org.openbase.jul.exception.InstantiationException {
        super(TemplateRegistry.class, TemplateRegistryController.class);
    }

    @Override
    public void loadProperties() {
        JPService.registerProperty(JPTemplateRegistryScope.class);
        JPService.registerProperty(JPActivityTemplateDatabaseDirectory.class);
        JPService.registerProperty(JPServiceTemplateDatabaseDirectory.class);
        JPService.registerProperty(JPUnitTemplateDatabaseDirectory.class);
        JPService.registerProperty(JPReadOnly.class);
        JPService.registerProperty(JPForce.class);
        JPService.registerProperty(JPDebugMode.class);

        JPService.registerProperty(JPRSBHost.class);
        JPService.registerProperty(JPRSBPort.class);
        JPService.registerProperty(JPRSBTransport.class);
    }

    public static void main(String args[]) throws Throwable {
        BCO.printLogo();
        AbstractLauncher.main(args, TemplateRegistry.class, TemplateRegistryLauncher.class);
    }
}