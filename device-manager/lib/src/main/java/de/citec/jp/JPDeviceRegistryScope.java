/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jp;

import de.citec.jul.extension.rsb.scope.jp.JPScope;
import rsb.Scope;

/**
 *
 * @author mpohling
 */
public class JPDeviceRegistryScope extends JPScope {
    
	public final static String[] COMMAND_IDENTIFIERS = {"--device-registry-scope"};

	public JPDeviceRegistryScope() {
		super(COMMAND_IDENTIFIERS);
	}

    @Override
    protected Scope getPropertyDefaultValue() {
        return new Scope("/devicemanager/registry");
    }
    
    @Override
	public String getDescription() {
		return "Setup the device registry scope which is used for the rsb communication.";
    }
}