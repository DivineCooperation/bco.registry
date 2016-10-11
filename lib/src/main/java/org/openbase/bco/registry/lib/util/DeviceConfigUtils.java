package org.openbase.bco.registry.lib.util;

/*
 * #%L
 * BCO Registry Lib
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
import java.util.ArrayList;
import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import rst.domotic.unit.device.DeviceClassType.DeviceClassOrBuilder;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitConfigType.UnitConfigOrBuilder;
import rst.domotic.unit.UnitTemplateConfigType.UnitTemplateConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate;

/**
 * A collection of utils to manipulate or analyse unit device configs.
 *
 UnitConfig
 */
public class DeviceConfigUtils {

    /**
     * Check if the given device configuration contains one unit template type more than once.
     *
     * @param deviceUnitConfig
     * @return true if a duplicated unit type is detected.
     */
    public static boolean checkDuplicatedUnitType(final UnitConfigOrBuilder deviceUnitConfig, ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> dalUnitRegistry) throws CouldNotPerformException {

        List<UnitTemplate.UnitType> unitTypeList = new ArrayList<>();
        for (String unitId : deviceUnitConfig.getDeviceConfig().getUnitIdList()) {
            UnitConfig dalUnitConfig = dalUnitRegistry.getMessage(unitId);
            if (unitTypeList.contains(dalUnitConfig.getType())) {
                return true;
            }
            unitTypeList.add(dalUnitConfig.getType());
        }
        return false;
    }

    /**
     * Method setups a non defined unit label field or setups the unit label with the device label if the unit is bound to the device.
     *
     * @param unitConfig the unit config to setup the label.
     * @param deviceConfig the device config to lookup device label.
     * @param deviceClass the device class to lookup the unit template.
     * @return true if the unitConfig was modified otherwise false.
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public static boolean setupUnitLabelByDeviceConfig(final UnitConfig.Builder unitConfig, final UnitConfigOrBuilder deviceConfig, final DeviceClassOrBuilder deviceClass, ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> dalUnitRegistry) throws CouldNotPerformException {
        return setupUnitLabelByDeviceConfig(unitConfig, deviceConfig, deviceClass, checkDuplicatedUnitType(deviceConfig, dalUnitRegistry));
    }

    /**
     * Method setups a non defined unit label field or setups the unit label with the device label if the unit is bound to the device.
     *
     * @param unitConfig the unit config to setup the label.
     * @param deviceUnitConfig the device config to lookup device label.
     * @param deviceClass the device class to lookup the unit template.
     * @param deviceConfigHasDuplicatedUnitType can be precomputed out of performance reasons.
     * @return true if the unitConfig was modified otherwise false.
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public static boolean setupUnitLabelByDeviceConfig(final UnitConfig.Builder unitConfig, final UnitConfigOrBuilder deviceUnitConfig, final DeviceClassOrBuilder deviceClass, boolean deviceConfigHasDuplicatedUnitType) throws CouldNotPerformException {
        try {
            if (!unitConfig.hasLabel() || unitConfig.getLabel().isEmpty() || unitConfig.getBoundToUnitHost()) {
                if (deviceConfigHasDuplicatedUnitType) {
                    if (unitConfig.hasLabel() && !unitConfig.getLabel().isEmpty()) {
                        return false;
                    }

                    if (!unitConfig.hasUnitTemplateConfigId()) {
                        throw new NotAvailableException("unitconfig.unittemplateconfigid");
                    }

                    for (UnitTemplateConfig unitTemplateConfig : deviceClass.getUnitTemplateConfigList()) {
                        if (unitTemplateConfig.getId().equals(unitConfig.getUnitTemplateConfigId())) {
                            if (unitTemplateConfig.getLabel().isEmpty()) {
                                throw new NotAvailableException("unitTemplateConfig.label");
                            }
                            unitConfig.setLabel(deviceUnitConfig.getLabel() + "_" + unitTemplateConfig.getLabel());
                            return true;
                        }
                    }
                    throw new CouldNotPerformException("DeviceClass[" + deviceClass.getId() + "] does not contain UnitTemplateConfig[" + unitConfig.getUnitTemplateConfigId() + "]!");
                } else {

                    if (!deviceUnitConfig.hasLabel()) {
                        throw new NotAvailableException("deviceconfig.label");
                    }

                    if (!unitConfig.getLabel().equals(deviceUnitConfig.getLabel())) {
                        unitConfig.setLabel(deviceUnitConfig.getLabel());
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not setup UnitConfig[" + unitConfig.getId() + "] by DeviceConfig[" + deviceUnitConfig + "]!", ex);
        }
    }
}
