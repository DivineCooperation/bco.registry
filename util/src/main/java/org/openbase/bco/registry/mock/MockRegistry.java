package org.openbase.bco.registry.mock;

/*
 * #%L
 * REM Utility
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbase.bco.registry.agent.core.AgentRegistryLauncher;
import org.openbase.bco.registry.agent.lib.AgentRegistry;
import org.openbase.bco.registry.app.core.AppRegistryLauncher;
import org.openbase.bco.registry.app.lib.AppRegistry;
import org.openbase.bco.registry.device.core.DeviceRegistryLauncher;
import org.openbase.bco.registry.device.lib.DeviceRegistry;
import org.openbase.bco.registry.device.remote.CachedDeviceRegistryRemote;
import org.openbase.bco.registry.location.core.LocationRegistryLauncher;
import org.openbase.bco.registry.location.lib.LocationRegistry;
import org.openbase.bco.registry.location.remote.CachedLocationRegistryRemote;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.BATTERY_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.BLIND_SOS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.BLIND_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.BRIGHTNESS_SOS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.BRIGHTNESS_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.BUTTON_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.COLOR_SOS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.COLOR_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.CONTACT_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.HANDLE_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.INTENSITY_SOS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.INTENSITY_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.MOTION_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.POWER_CONSUMPTION_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.POWER_SOS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.POWER_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.SMOKE_ALARM_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.SMOKE_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.TAMPER_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.TARGET_TEMPERATURE_SOS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.TARGET_TEMPERATURE_SPS;
import static org.openbase.bco.registry.mock.MockRegistry.MockServiceTemplate.TEMPERATURE_SPS;
import org.openbase.bco.registry.scene.core.SceneRegistryLauncher;
import org.openbase.bco.registry.scene.lib.SceneRegistry;
import org.openbase.bco.registry.unit.core.UnitRegistryLauncher;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.user.core.UserRegistryLauncher;
import org.openbase.bco.registry.user.lib.UserRegistry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.schedule.GlobalExecutionService;
import org.slf4j.LoggerFactory;
import rst.authorization.UserConfigType.UserConfig;
import rst.geometry.PoseType.Pose;
import rst.geometry.RotationType.Rotation;
import rst.geometry.TranslationType.Translation;
import rst.homeautomation.binding.BindingConfigType.BindingConfig;
import rst.homeautomation.device.DeviceClassType.DeviceClass;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;
import rst.homeautomation.service.ServiceConfigType;
import rst.homeautomation.service.ServiceConfigType.ServiceConfig;
import rst.homeautomation.service.ServiceTemplateConfigType.ServiceTemplateConfig;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServicePattern;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.homeautomation.state.EnablingStateType.EnablingState;
import rst.homeautomation.state.InventoryStateType;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.homeautomation.unit.UnitTemplateConfigType.UnitTemplateConfig;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.spatial.LocationConfigType.LocationConfig;
import rst.spatial.PlacementConfigType.PlacementConfig;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class MockRegistry {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MockRegistry.class);
    
    public static final String USER_NAME = "uSeRnAmE";
    public static UnitConfig testUser;
    
    public static final String COLORABLE_LIGHT_LABEL = "Ambient_Light_Unit_Test";
    public static final String BATTERY_LABEL = "Battery_Unit_Test";
    public static final String BRIGHTNESS_SENSOR_LABEL = "Brightness_Sensor_Unit_Test";
    public static final String BUTTON_LABEL = "Button_Unit_Test";
    public static final String DIMMABLE_LIGHT_LABEL = "Dimmer_Unit_Test";
    public static final String HANDLE_LABEL = "Handle_Sensor_Unit_Test";
    public static final String LIGHT_LABEL = "Light_Unit_Test";
    public static final String MOTION_DETECTOR_LABEL = "Motion_Sensor_Unit_Test";
    public static final String POWER_CONSUMPTION_LABEL = "Power_Consumption_Sensor_Unit_Test";
    public static final String POWER_SWITCH_LABEL = "Power_Plug_Unit_Test";
    public static final String REED_CONTACT_LABEL = "Reed_Switch_Unit_Test";
    public static final String ROLLER_SHUTTER_LABEL = "Rollershutter_Unit_Test";
    public static final String TAMPER_DETECTOR_LABEL = "Tamper_Switch_Unit_Test";
    public static final String TEMPERATURE_SENSOR_LABEL = "Temperature_Sensor_Unit_Test";
    public static final String TEMPERATURE_CONTROLLER_LABEL = "Temperature_Controller_Unit_Test";
    public static final String SMOKE_DETECTOR_LABEL = "Smoke_Detector_Unit_Test";
    private final String serialNumber = "1234-5678-9100";
    
    private static DeviceRegistryLauncher deviceRegistryLauncher;
    private static LocationRegistryLauncher locationRegistryLauncher;
    private static AgentRegistryLauncher agentRegistryLauncher;
    private static AppRegistryLauncher appRegistryLauncher;
    private static SceneRegistryLauncher sceneRegistryLauncher;
    private static UserRegistryLauncher userRegistryLauncher;
    private static UnitRegistryLauncher unitRegistryLauncher;
    
    private static DeviceRegistry deviceRegistry;
    private static LocationRegistry locationRegistry;
    private static AgentRegistry agentRegistry;
    private static AppRegistry appRegistry;
    private static SceneRegistry sceneRegistry;
    private static UserRegistry userRegisty;
    private static UnitRegistry unitRegistry;
    
    private static UnitConfig paradiseLocation;
    
    public static final Map<UnitType, String> UNIT_TYPE_LABEL_MAP = new HashMap<>();
    
    public enum MockServiceTemplate {

        // endings:
        // SOS = STATE_OPERATION_SERVICE
        // SPS = STATE_PROVIDER_SERVICE
        // SCS = STATE_CONSUMER_SERVICE
        BATTERY_SPS(ServiceType.BATTERY_STATE_SERVICE, ServicePattern.PROVIDER),
        BLIND_SOS(ServiceType.BLIND_STATE_SERVICE, ServicePattern.OPERATION),
        BLIND_SPS(ServiceType.BLIND_STATE_SERVICE, ServicePattern.PROVIDER),
        BRIGHTNESS_SOS(ServiceType.BRIGHTNESS_STATE_SERVICE, ServicePattern.OPERATION),
        BRIGHTNESS_SPS(ServiceType.BRIGHTNESS_STATE_SERVICE, ServicePattern.PROVIDER),
        BUTTON_SPS(ServiceType.BUTTON_STATE_SERVICE, ServicePattern.PROVIDER),
        COLOR_SOS(ServiceType.COLOR_STATE_SERVICE, ServicePattern.OPERATION),
        COLOR_SPS(ServiceType.COLOR_STATE_SERVICE, ServicePattern.PROVIDER),
        CONTACT_SPS(ServiceType.CONTACT_STATE_SERVICE, ServicePattern.PROVIDER),
        HANDLE_SPS(ServiceType.HANDLE_STATE_SERVICE, ServicePattern.PROVIDER),
        INTENSITY_SOS(ServiceType.INTENSITY_STATE_SERVICE, ServicePattern.OPERATION),
        INTENSITY_SPS(ServiceType.INTENSITY_STATE_SERVICE, ServicePattern.PROVIDER),
        MOTION_SPS(ServiceType.MOTION_STATE_SERVICE, ServicePattern.PROVIDER),
        POWER_CONSUMPTION_SPS(ServiceType.POWER_CONSUMPTION_STATE_SERVICE, ServicePattern.PROVIDER),
        POWER_SOS(ServiceType.POWER_STATE_SERVICE, ServicePattern.OPERATION),
        POWER_SPS(ServiceType.POWER_STATE_SERVICE, ServicePattern.PROVIDER),
        SMOKE_ALARM_SPS(ServiceType.SMOKE_ALARM_STATE_SERVICE, ServicePattern.PROVIDER),
        SMOKE_SPS(ServiceType.SMOKE_STATE_SERVICE, ServicePattern.PROVIDER),
        TAMPER_SPS(ServiceType.TAMPER_STATE_SERVICE, ServicePattern.PROVIDER),
        TARGET_TEMPERATURE_SOS(ServiceType.TARGET_TEMPERATURE_STATE_SERVICE, ServicePattern.OPERATION),
        TARGET_TEMPERATURE_SPS(ServiceType.TARGET_TEMPERATURE_STATE_SERVICE, ServicePattern.PROVIDER),
        TEMPERATURE_SPS(ServiceType.TEMPERATURE_STATE_SERVICE, ServicePattern.PROVIDER);
        
        private final ServiceTemplate template;
        
        MockServiceTemplate(ServiceType type, ServicePattern servicePattern) {
            ServiceTemplate.Builder templateBuilder = ServiceTemplate.newBuilder();
            templateBuilder.setType(type);
            templateBuilder.setPattern(servicePattern);
            this.template = templateBuilder.build();
        }
        
        public ServiceTemplate getTemplate() {
            return template;
        }
    }
    
    public enum MockUnitTemplate {
        
        COLORABLE_LIGHT(UnitType.COLORABLE_LIGHT, COLOR_SOS, COLOR_SPS, POWER_SOS, POWER_SPS, BRIGHTNESS_SOS, BRIGHTNESS_SPS),
        LIGHT(UnitType.LIGHT, POWER_SOS, POWER_SPS),
        MOTION_DETECTOR(UnitType.MOTION_DETECTOR, MOTION_SPS),
        BRIGHTNESS_SENSOR(UnitType.BRIGHTNESS_SENSOR, BRIGHTNESS_SPS),
        BUTTON(UnitType.BUTTON, BUTTON_SPS),
        DIMMER(UnitType.DIMMER, INTENSITY_SOS, INTENSITY_SPS, POWER_SOS, POWER_SPS),
        HANDLE(UnitType.HANDLE, HANDLE_SPS),
        POWER_CONSUMPTION_SENSOR(UnitType.POWER_CONSUMPTION_SENSOR, POWER_CONSUMPTION_SPS),
        POWER_SOURCE(UnitType.POWER_SWITCH, POWER_SOS, POWER_SPS),
        REED_CONTACT(UnitType.REED_CONTACT, CONTACT_SPS),
        ROLLER_SHUTTER(UnitType.ROLLER_SHUTTER, BLIND_SOS, BLIND_SPS),
        TAMPER_DETECTOR(UnitType.TAMPER_DETECTOR, TAMPER_SPS),
        TEMPERATURE_CONTROLLER(UnitType.TEMPERATURE_CONTROLLER, TARGET_TEMPERATURE_SOS, TARGET_TEMPERATURE_SPS, TEMPERATURE_SPS),
        SMOKE_DETECTOR_CONTROLLER(UnitType.SMOKE_DETECTOR, SMOKE_SPS, SMOKE_ALARM_SPS),
        TEMPERATURE_SENSOR(UnitType.TEMPERATURE_SENSOR, TEMPERATURE_SPS),
        BATTERY(UnitType.BATTERY, BATTERY_SPS);
        
        private final UnitTemplate template;
        
        MockUnitTemplate(UnitTemplate.UnitType type, MockServiceTemplate... serviceTemplates) {
            UnitTemplate.Builder templateBuilder = UnitTemplate.newBuilder();
            templateBuilder.setType(type);
            for (MockServiceTemplate serviceTemplate : serviceTemplates) {
                templateBuilder.addServiceTemplate(serviceTemplate.getTemplate());
            }
            this.template = templateBuilder.build();
        }
        
        public UnitTemplate getTemplate() {
            return template;
        }
        
        public static UnitTemplate getTemplate(UnitType type) throws CouldNotPerformException {
            for (MockUnitTemplate templateType : values()) {
                if (templateType.getTemplate().getType() == type) {
                    return templateType.getTemplate();
                }
            }
            throw new CouldNotPerformException("Could not find template for " + type + "!");
        }
    }
    
    protected MockRegistry() throws InstantiationException {
        if (UNIT_TYPE_LABEL_MAP.isEmpty()) {
            UNIT_TYPE_LABEL_MAP.put(UnitType.COLORABLE_LIGHT, COLORABLE_LIGHT_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.LIGHT, LIGHT_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.MOTION_DETECTOR, MOTION_DETECTOR_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.BRIGHTNESS_SENSOR, BRIGHTNESS_SENSOR_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.BUTTON, BUTTON_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.DIMMER, DIMMABLE_LIGHT_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.HANDLE, HANDLE_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.POWER_CONSUMPTION_SENSOR, POWER_CONSUMPTION_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.POWER_SWITCH, POWER_SWITCH_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.REED_CONTACT, REED_CONTACT_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.ROLLER_SHUTTER, ROLLER_SHUTTER_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.TAMPER_DETECTOR, TAMPER_DETECTOR_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.TEMPERATURE_CONTROLLER, TEMPERATURE_CONTROLLER_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.SMOKE_DETECTOR, SMOKE_DETECTOR_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.TEMPERATURE_SENSOR, TEMPERATURE_SENSOR_LABEL);
            UNIT_TYPE_LABEL_MAP.put(UnitType.BATTERY, BATTERY_LABEL);
        }
        
        try {
            JPService.setupJUnitTestMode();
            List<Future<Void>> registryStartupTasks = new ArrayList<>();
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        unitRegistryLauncher = new UnitRegistryLauncher();
                        unitRegistry = unitRegistryLauncher.getUnitRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        deviceRegistryLauncher = new DeviceRegistryLauncher();
                        deviceRegistry = deviceRegistryLauncher.getDeviceRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        agentRegistryLauncher = new AgentRegistryLauncher();
                        agentRegistry = agentRegistryLauncher.getAgentRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        appRegistryLauncher = new AppRegistryLauncher();
                        appRegistry = appRegistryLauncher.getAppRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            logger.info("Starting all real registries: unit, device, agent, app ...");
            for (Future<Void> task : registryStartupTasks) {
                task.get();
            }
            logger.info("Real registries started!");
            
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        locationRegistryLauncher = new LocationRegistryLauncher();
                        locationRegistry = locationRegistryLauncher.getLocationRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        userRegistryLauncher = new UserRegistryLauncher();
                        userRegisty = userRegistryLauncher.getUserRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        sceneRegistryLauncher = new SceneRegistryLauncher();
                        sceneRegistry = sceneRegistryLauncher.getSceneRegistry();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            logger.info("Waiting for purely virtual registries: location, user, scene ...");
            for (Future<Void> task : registryStartupTasks) {
                task.get();
            }
            logger.info("Virtual registries started!");
            
            registryStartupTasks.add(GlobalExecutionService.submit(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    try {
                        logger.info("Update unitTemplates...");
                        // load templates
                        for (MockUnitTemplate template : MockUnitTemplate.values()) {
                            String unitTemplateId = unitRegistry.getUnitTemplateByType(template.getTemplate().getType()).getId();
                            unitRegistry.updateUnitTemplate(template.getTemplate().toBuilder().setId(unitTemplateId).build()).get();
                        }
                        
                        logger.info("Register user...");
                        registerUser();
                        
                        logger.info("Register locations...");
                        registerLocations();
                        // TODO need to be implemented.
                        // locationRegistry.waitForConsistency();
                        logger.info("Register devices...");
                        registerDevices();
                        logger.info("Wait for consistency");
                        deviceRegistry.waitForConsistency();
                    } catch (CouldNotPerformException | InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger, org.openbase.jul.exception.printer.LogLevel.ERROR);
                    }
                    return null;
                }
            }));
            
            logger.info("Wait for unitTemplate updates; device, location and user registration...");
            for (Future<Void> task : registryStartupTasks) {
                task.get();
            }
            logger.info("UnitTemplates updated and devices, locations and user registered!");
            
            logger.info("Started app/agent/scene registries!");
            CachedDeviceRegistryRemote.reinitialize();
            CachedLocationRegistryRemote.reinitialize();
            logger.info("Reinitialized remotes!");
        } catch (JPServiceException | InterruptedException | ExecutionException | CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }
    
    protected void shutdown() {
        deviceRegistryLauncher.shutdown();
        locationRegistryLauncher.shutdown();
        agentRegistryLauncher.shutdown();
        appRegistryLauncher.shutdown();
        sceneRegistryLauncher.shutdown();
        userRegistryLauncher.shutdown();
        CachedDeviceRegistryRemote.shutdown();
        CachedLocationRegistryRemote.shutdown();
    }
    
    private void registerLocations() throws CouldNotPerformException, InterruptedException {
        try {
            LocationConfig defaultLocation = LocationConfig.getDefaultInstance();
            paradiseLocation = locationRegistry.registerLocationConfig(UnitConfig.newBuilder().setType(UnitType.LOCATION).setLabel("Paradise").setLocationConfig(defaultLocation).build()).get();
        } catch (ExecutionException ex) {
            throw new CouldNotPerformException(ex);
        }
    }
    
    private void registerUser() throws CouldNotPerformException, InterruptedException {
        UserConfig.Builder config = UserConfig.newBuilder().setFirstName("Max").setLastName("Mustermann").setUserName(USER_NAME);
        UnitConfig userUnitConfig = UnitConfig.newBuilder().setType(UnitType.USER).setUserConfig(config).setEnablingState(EnablingState.newBuilder().setValue(EnablingState.State.ENABLED)).build();
        try {
            testUser = userRegisty.registerUserConfig(userUnitConfig).get();
        } catch (ExecutionException ex) {
            throw new CouldNotPerformException(ex);
        }
    }
    
    private void registerDevices() throws CouldNotPerformException, InterruptedException {
        try {
            // ambient light
            System.out.println("Device 1");
            DeviceClass ambientLightClass = deviceRegistry.registerDeviceClass(getDeviceClass("Philips_Hue_E27", "KV01_18U", "Philips", UnitType.COLORABLE_LIGHT)).get();
            registerDeviceUnitConfig(getDeviceConfig("PH_Hue_E27_Device", serialNumber, ambientLightClass));
            
            Thread.sleep(1000);
            System.out.println("Device 2");
            // battery, brightnessSensor, motionSensor, tamperSwitch, temperatureSensor
            DeviceClass motionSensorClass = deviceRegistry.registerDeviceClass(getDeviceClass("Fibaro_MotionSensor", "FGMS_001", "Fibaro", UnitType.MOTION_DETECTOR, UnitType.BATTERY, UnitType.BRIGHTNESS_SENSOR, UnitType.TEMPERATURE_SENSOR, UnitType.TAMPER_DETECTOR)).get();
            registerDeviceUnitConfig(getDeviceConfig("F_MotionSensor_Device", serialNumber, motionSensorClass));
            
            System.out.println("Device 3");
            // button
            DeviceClass buttonClass = deviceRegistry.registerDeviceClass(getDeviceClass("Gira_429496730210000", "429496730210000", "Gira", UnitType.BUTTON)).get();
            registerDeviceUnitConfig(getDeviceConfig("GI_429496730210000_Device", serialNumber, buttonClass));
            
            System.out.println("Device 4");
            // dimmer
            DeviceClass dimmerClass = deviceRegistry.registerDeviceClass(getDeviceClass("Hager_TYA663A", "TYA663A", "Hager", UnitType.DIMMER)).get();
            registerDeviceUnitConfig(getDeviceConfig("HA_TYA663A_Device", serialNumber, dimmerClass));
            
            System.out.println("Device 5");
            // handle
            DeviceClass handleClass = deviceRegistry.registerDeviceClass(getDeviceClass("Homematic_RotaryHandleSensor", "Sec_RHS", "Homematic", UnitType.HANDLE)).get();
            registerDeviceUnitConfig(getDeviceConfig("HM_RotaryHandleSensor_Device", serialNumber, handleClass));
            
            System.out.println("Device 6");
            // light
            DeviceClass lightClass = deviceRegistry.registerDeviceClass(getDeviceClass("Fibaro_FGS_221", "FGS_221", "Fibaro", UnitType.LIGHT)).get();
            registerDeviceUnitConfig(getDeviceConfig("F_FGS221_Device", serialNumber, lightClass));
            
            System.out.println("Device 7");
            // powerConsumptionSensor, powerPlug
            DeviceClass powerPlugClass = deviceRegistry.registerDeviceClass(getDeviceClass("Plugwise_PowerPlug", "070140", "Plugwise", UnitType.POWER_SWITCH, UnitType.POWER_CONSUMPTION_SENSOR)).get();
            registerDeviceUnitConfig(getDeviceConfig("PW_PowerPlug_Device", serialNumber, powerPlugClass));
            
            System.out.println("Device 8");
            // reedSwitch
            DeviceClass reedSwitchClass = deviceRegistry.registerDeviceClass(getDeviceClass("Homematic_ReedSwitch", "Sec_SC_2", "Homematic", UnitType.REED_CONTACT)).get();
            registerDeviceUnitConfig(getDeviceConfig("HM_ReedSwitch_Device", serialNumber, reedSwitchClass));
            
            System.out.println("Device 9");
            // rollershutter
            DeviceClass rollershutterClass = deviceRegistry.registerDeviceClass(getDeviceClass("Hager_TYA628C", "TYA628C", "Hager", UnitType.ROLLER_SHUTTER)).get();
            registerDeviceUnitConfig(getDeviceConfig("HA_TYA628C_Device", serialNumber, rollershutterClass));
            
            System.out.println("Device 10");
            // smoke detector
            DeviceClass smokeDetector = deviceRegistry.registerDeviceClass(getDeviceClass("Fibaro_FGSS_001", "FGSS_001", "Fibaro", UnitType.SMOKE_DETECTOR)).get();
            registerDeviceUnitConfig(getDeviceConfig("Fibaro_SmokeDetector_Device", serialNumber, smokeDetector));
            
            System.out.println("Device 11");
            // temperature controller
            DeviceClass temperatureControllerClass = deviceRegistry.registerDeviceClass(getDeviceClass("Gira_429496730250000", "429496730250000", "Gira", UnitType.TEMPERATURE_CONTROLLER)).get();
            registerDeviceUnitConfig(getDeviceConfig("Gire_TemperatureController_Device", serialNumber, temperatureControllerClass));
        } catch (ExecutionException ex) {
            throw new CouldNotPerformException(ex);
        }
    }
    
    private static void updateUnitLabel(List<String> unitIds) throws CouldNotPerformException, InterruptedException, ExecutionException {
        for (String unitId : unitIds) {
            UnitConfig tmp = unitRegistry.getUnitConfigById(unitId);
            unitRegistry.updateUnitConfig(tmp.toBuilder().setLabel(UNIT_TYPE_LABEL_MAP.get(tmp.getType())).build()).get();
        }
    }
    
    private static void registerDeviceUnitConfig(UnitConfig deviceUnitConfig) throws CouldNotPerformException, InterruptedException, ExecutionException {
        UnitConfig tmp = deviceRegistry.registerDeviceConfig(deviceUnitConfig).get();
        updateUnitLabel(tmp.getDeviceConfig().getUnitIdList());
    }
    
    public static PlacementConfig getDefaultPlacement() {
        Rotation rotation = Rotation.newBuilder().setQw(1).setQx(0).setQy(0).setQz(0).build();
        Translation translation = Translation.newBuilder().setX(0).setY(0).setZ(0).build();
        Pose pose = Pose.newBuilder().setRotation(rotation).setTranslation(translation).build();
        return PlacementConfig.newBuilder().setPosition(pose).setLocationId(getLocation().getId()).build();
    }
    
    public static Iterable<ServiceConfigType.ServiceConfig> getServiceConfig(final UnitTemplate template) {
        List<ServiceConfigType.ServiceConfig> serviceConfigList = new ArrayList<>();
        template.getServiceTemplateList().stream().forEach((serviceTemplate) -> {
            BindingConfig bindingServiceConfig = BindingConfig.newBuilder().setBindingId("OPENHAB").build();
            serviceConfigList.add(ServiceConfig.newBuilder().setServiceTemplate(serviceTemplate).setBindingConfig(bindingServiceConfig).build());
        });
        return serviceConfigList;
    }
    
    public static UnitConfig getUnitConfig(UnitTemplate.UnitType type, String label) throws CouldNotPerformException {
        UnitTemplate template = MockUnitTemplate.getTemplate(type);
        return UnitConfig.newBuilder().setPlacementConfig(getDefaultPlacement()).setType(type).addAllServiceConfig(getServiceConfig(template)).setLabel(label).setBoundToUnitHost(false).build();
    }
    
    public static UnitConfig getDeviceConfig(String label, String serialNumber, DeviceClass clazz) {
        DeviceConfig tmp = DeviceConfig.newBuilder()
                .setSerialNumber(serialNumber)
                .setDeviceClassId(clazz.getId())
                .setInventoryState(InventoryStateType.InventoryState.newBuilder().setValue(InventoryStateType.InventoryState.State.INSTALLED))
                .build();
        return UnitConfig.newBuilder()
                .setPlacementConfig(getDefaultPlacement())
                .setLabel(label)
                .setDeviceConfig(tmp)
                .setType(UnitType.DEVICE)
                .build();
    }
    
    private static List<UnitTemplateConfig> getUnitTemplateConfigs(List<UnitTemplate.UnitType> unitTypes) throws CouldNotPerformException {
        List<UnitTemplateConfig> unitTemplateConfigs = new ArrayList<>();
        for (UnitTemplate.UnitType type : unitTypes) {
            List<ServiceTemplateConfig> serviceTemplateConfigs = new ArrayList<>();
            for (ServiceTemplate serviceTemplate : MockUnitTemplate.getTemplate(type).getServiceTemplateList()) {
                serviceTemplateConfigs.add(ServiceTemplateConfig.newBuilder().setServiceType(serviceTemplate.getType()).build());
            }
            UnitTemplateConfig config = UnitTemplateConfig.newBuilder().setType(type).addAllServiceTemplateConfig(serviceTemplateConfigs).build();
            unitTemplateConfigs.add(config);
        }
        return unitTemplateConfigs;
    }
    
    public static DeviceClass getDeviceClass(String label, String productNumber, String company, UnitTemplate.UnitType... types) throws CouldNotPerformException {
        List<UnitTemplate.UnitType> unitTypeList = new ArrayList<>();
        for (UnitTemplate.UnitType type : types) {
            unitTypeList.add(type);
        }
        return DeviceClass.newBuilder().setLabel(label).setProductNumber(productNumber).setCompany(company)
                .setBindingConfig(getBindingConfig()).addAllUnitTemplateConfig(getUnitTemplateConfigs(unitTypeList)).build();
    }
    
    public static BindingConfig getBindingConfig() {
        BindingConfig.Builder bindingConfigBuilder = BindingConfig.newBuilder();
        bindingConfigBuilder.setBindingId("OPENHAB");
        return bindingConfigBuilder.build();
    }
    
    public static UnitConfig getLocation() {
        return paradiseLocation;
    }
}
