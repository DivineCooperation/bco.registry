package org.openbase.bco.registry.print;

/*
 * #%L
 * BCO Registry Utility
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.processing.StringProcessor.Alignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.device.DeviceClassType.DeviceClass;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class BCORegistryPrinter {

    private static final Logger logger = LoggerFactory.getLogger(BCORegistryPrinter.class);

    private static final int AMOUNT_COLUM_SPACE = 5;
    private final int LINE_LENGHT;
    private static final String COLUM_DELIMITER = "|";
    private final String LINE_DELIMITER_SMALL;
    private final String LINE_DELIMITER_FAT;

    private final Map<String, Integer> deviceNumberByClassMap;
    private final Map<UnitType, Integer> unitNumberByTypeMap;
    private final Map<ServiceType, Integer> serviceNumberByTypeMap;

    public BCORegistryPrinter() throws InterruptedException, NotAvailableException, InstantiationException, CouldNotPerformException {

        // pre init
        deviceNumberByClassMap = new HashMap<>();
        unitNumberByTypeMap = new HashMap<>();
        serviceNumberByTypeMap = new HashMap<>();
        Registries.getDeviceRegistry().waitForData();

        // calculate max unit label length
        int maxUnitLabelLength = 0;

        // prepare devices
        for (DeviceClass deviceClass : Registries.getDeviceRegistry().getDeviceClasses()) {
            maxUnitLabelLength = Math.max(maxUnitLabelLength, deviceClass.getLabel().length());
            deviceNumberByClassMap.put(deviceClass.getId(), 0);
        }
        for (UnitConfig deviceUnitConfig : Registries.getDeviceRegistry().getDeviceConfigs()) {
            deviceNumberByClassMap.put(deviceUnitConfig.getDeviceConfig().getDeviceClassId(), deviceNumberByClassMap.get(deviceUnitConfig.getDeviceConfig().getDeviceClassId()) + 1);
        }

        // prepare units
        for (UnitType unitType : UnitType.values()) {
            maxUnitLabelLength = Math.max(maxUnitLabelLength, unitType.name().length());
            int unitsPerType = Registries.getDeviceRegistry().getUnitConfigs(unitType).size();
            unitNumberByTypeMap.put(unitType, unitsPerType);
        }

        // prepare services
        for (ServiceType serviceType : ServiceType.values()) {
            maxUnitLabelLength = Math.max(maxUnitLabelLength, serviceType.name().length());
            int servicesPerType = Registries.getDeviceRegistry().getServiceConfigs(serviceType).size();
            serviceNumberByTypeMap.put(serviceType, servicesPerType);
        }

        // post init
        LINE_LENGHT = (COLUM_DELIMITER.length() * 2) + AMOUNT_COLUM_SPACE + 3 + maxUnitLabelLength;
        LINE_DELIMITER_FAT = StringProcessor.fillWithSpaces("", LINE_LENGHT).replaceAll(" ", "=");
        LINE_DELIMITER_SMALL = StringProcessor.fillWithSpaces("", LINE_LENGHT).replaceAll(" ", "-");

        // print device category
        System.out.println("");
        System.out.println(LINE_DELIMITER_FAT);
        printEntry("Devices", Registries.getDeviceRegistry().getDeviceConfigs().size());
        System.out.println(LINE_DELIMITER_FAT);

        // sort devices
        List<DeviceClass> devicesList = Registries.getDeviceRegistry().getDeviceClasses();
        Collections.sort(devicesList, new Comparator<DeviceClass>() {
            @Override
            public int compare(DeviceClass o1, DeviceClass o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        // print devices
        for (DeviceClass deviceClass : devicesList) {
            if (deviceNumberByClassMap.get(deviceClass.getId()) == 0) {
                continue;
            }
            printEntry(deviceClass.getLabel(), deviceNumberByClassMap.get(deviceClass.getId()));
        }
        System.out.println(LINE_DELIMITER_SMALL);
        System.out.println("");

        // print unit category
        System.out.println(LINE_DELIMITER_FAT);
        printEntry("Units", Registries.getUnitRegistry().getUnitConfigs().size());
        System.out.println(LINE_DELIMITER_FAT);

        // sort units
        List<UnitType> unitServiceList = Arrays.asList(UnitType.values());
        Collections.sort(unitServiceList, new Comparator<UnitType>() {
            @Override
            public int compare(UnitType o1, UnitType o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        // print units
        for (UnitType unitType : unitServiceList) {
            if (unitType == UnitType.UNKNOWN) {
                continue;
            }
            if (unitNumberByTypeMap.get(unitType) == 0) {
                continue;
            }
            printEntry(StringProcessor.transformUpperCaseToCamelCase(unitType.name()), unitNumberByTypeMap.get(unitType));
        }
        System.out.println(LINE_DELIMITER_SMALL);
        System.out.println("");

        // print service category
        System.out.println(LINE_DELIMITER_FAT);
        printEntry("Services", Registries.getUnitRegistry().getServiceConfigs().size());
        System.out.println(LINE_DELIMITER_FAT);

        // sort services
        List<ServiceType> servicesServiceList = Arrays.asList(ServiceType.values());
        Collections.sort(servicesServiceList, new Comparator<ServiceType>() {
            @Override
            public int compare(ServiceType o1, ServiceType o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        // print services
        for (ServiceType serviceType : servicesServiceList) {
            if (serviceType == ServiceType.UNKNOWN) {
                continue;
            }
            if (serviceNumberByTypeMap.get(serviceType) == 0) {
                continue;
            }
            printEntry(StringProcessor.transformUpperCaseToCamelCase(serviceType.name()), serviceNumberByTypeMap.get(serviceType));
        }
        System.out.println(LINE_DELIMITER_SMALL);
        System.out.println("");
    }

    private void printEntry(final String context, final int amount) {
        System.out.println(COLUM_DELIMITER
                + StringProcessor.fillWithSpaces(
                        StringProcessor.fillWithSpaces(
                                Integer.toString(amount),
                                AMOUNT_COLUM_SPACE,
                                Alignment.RIGHT)
                        + " x " + context,
                        LINE_LENGHT - (COLUM_DELIMITER.length() * 2),
                        Alignment.LEFT)
                + COLUM_DELIMITER);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new BCORegistryPrinter();
        } catch (InterruptedException | CouldNotPerformException ex) {
            ExceptionPrinter.printHistoryAndExit(ex, logger);
        }
        System.exit(0);
    }
}