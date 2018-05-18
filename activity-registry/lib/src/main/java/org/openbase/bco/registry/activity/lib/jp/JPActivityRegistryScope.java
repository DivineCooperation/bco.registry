package org.openbase.bco.registry.activity.lib.jp;

/*
 * #%L
 * BCO Registry Activity Library
 * %%
 * Copyright (C) 2014 - 2018 openbase.org
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

import org.openbase.jul.extension.rsb.scope.jp.JPScope;
import rsb.Scope;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPActivityRegistryScope extends JPScope {
    
	public final static String[] COMMAND_IDENTIFIERS = {"--activity-registry-scope"};

	public JPActivityRegistryScope() {
		super(COMMAND_IDENTIFIERS);
	}

    @Override
    protected Scope getPropertyDefaultValue() {
        return super.getPropertyDefaultValue().concat(new Scope("/registry/activity"));
    }
    
    @Override
	public String getDescription() {
		return "Setup the activity registry scope which is used for the rsb communication.";
    }
}