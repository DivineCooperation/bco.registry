package org.openbase.bco.registry.unit.core;

/*
 * #%L
 * REM UnitRegistry Core
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.openbase.bco.registry.lib.AbstractRegistryController;
import org.openbase.bco.registry.location.core.consistency.RootConsistencyHandler;
import org.openbase.bco.registry.location.core.consistency.RootLocationExistencConsistencyHandler;
import org.openbase.bco.registry.scene.lib.jp.JPUnitConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.core.consistency.ServiceConfigUnitIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.authorizationgroup.AuthorizationGroupConfigLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.authorizationgroup.AuthorizationGroupConfigScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionLocationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionTilesConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.ServiceConfigBindingTypeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.UnitConfigUnitTemplateConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.UnitTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceConfigDeviceClassIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceConfigDeviceClassUnitConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceConfigLocationIdForInstalledDevicesConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceLocationIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceOwnerConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.OpenhabServiceConfigItemIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.ChildWithSameLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationChildConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationLoopConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationParentConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationPlacementConfigConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationPositionConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.LocationUnitIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupMemberExistsConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupMemberListDuplicationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupMemberListTypesConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupUnitTypeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unittemplate.UnitTemplateValidationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.user.UserConfigScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.user.UserConfigUserNameConsistencyHandler;
import org.openbase.bco.registry.unit.core.dbconvert.DummyConverter;
import org.openbase.bco.registry.unit.core.plugin.UnitTemplateCreatorRegistryPlugin;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.lib.generator.UnitConfigIdGenerator;
import org.openbase.bco.registry.unit.lib.generator.UnitTemplateIdGenerator;
import org.openbase.bco.registry.unit.lib.jp.JPUnitRegistryScope;
import org.openbase.bco.registry.unit.lib.jp.JPUnitTemplateDatabaseDirectory;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.rsb.com.RPCHelper;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.iface.Manageable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.schedule.GlobalExecutionService;
import org.openbase.jul.storage.file.ProtoBufJSonFileProvider;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.authorization.AuthorizationGroupConfigType;
import rst.authorization.UserConfigType;
import rst.authorization.UserRegistryDataType;
import rst.homeautomation.control.agent.AgentClassType;
import rst.homeautomation.control.agent.AgentConfigType;
import rst.homeautomation.control.agent.AgentRegistryDataType;
import rst.homeautomation.control.app.AppClassType;
import rst.homeautomation.control.app.AppConfigType;
import rst.homeautomation.control.app.AppRegistryDataType;
import rst.homeautomation.control.scene.SceneConfigType;
import rst.homeautomation.control.scene.SceneRegistryDataType;
import rst.homeautomation.device.DeviceClassType;
import rst.homeautomation.device.DeviceConfigType;
import rst.homeautomation.device.DeviceRegistryDataType;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.homeautomation.unit.UnitGroupConfigType;
import rst.homeautomation.unit.UnitRegistryDataType.UnitRegistryData;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.rsb.ScopeType;
import rst.spatial.ConnectionConfigType;
import rst.spatial.LocationConfigType;
import rst.spatial.LocationRegistryDataType;

/**
 *
 * @author mpohling
 */
public class UnitRegistryController extends AbstractRegistryController<UnitRegistryData, UnitRegistryData.Builder> implements UnitRegistry, Manageable<ScopeType.Scope> {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitTemplate.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UserRegistryDataType.UserRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UserConfigType.UserConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AuthorizationGroupConfigType.AuthorizationGroupConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DeviceRegistryDataType.DeviceRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DeviceClassType.DeviceClass.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DeviceConfigType.DeviceConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitTemplate.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitGroupConfigType.UnitGroupConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(LocationRegistryDataType.LocationRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(LocationConfigType.LocationConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(ConnectionConfigType.ConnectionConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AgentRegistryDataType.AgentRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AgentConfigType.AgentConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AgentClassType.AgentClass.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneRegistryDataType.SceneRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneConfigType.SceneConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppRegistryDataType.AppRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppConfigType.AppConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppClassType.AppClass.getDefaultInstance()));
    }

    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> unitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitTemplate, UnitTemplate.Builder, UnitRegistryData.Builder> unitTemplateRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UserConfigType.UserConfig, UserConfigType.UserConfig.Builder, UserRegistryDataType.UserRegistryData.Builder> userRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, AuthorizationGroupConfigType.AuthorizationGroupConfig, AuthorizationGroupConfigType.AuthorizationGroupConfig.Builder, UserRegistryDataType.UserRegistryData.Builder> authorizationGroupRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, DeviceConfigType.DeviceConfig, DeviceConfigType.DeviceConfig.Builder, DeviceRegistryDataType.DeviceRegistryData.Builder> deviceConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitGroupConfigType.UnitGroupConfig, UnitGroupConfigType.UnitGroupConfig.Builder, DeviceRegistryDataType.DeviceRegistryData.Builder> unitGroupConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder, LocationRegistryDataType.LocationRegistryData.Builder> locationConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, ConnectionConfigType.ConnectionConfig, ConnectionConfigType.ConnectionConfig.Builder, LocationRegistryDataType.LocationRegistryData.Builder> connectionConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, AgentConfigType.AgentConfig, AgentConfigType.AgentConfig.Builder, AgentRegistryDataType.AgentRegistryData.Builder> agentConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, SceneConfigType.SceneConfig, SceneConfigType.SceneConfig.Builder, SceneRegistryDataType.SceneRegistryData.Builder> sceneConfigRegistry;
    private ProtoBufFileSynchronizedRegistry<String, AppConfigType.AppConfig, AppConfigType.AppConfig.Builder, AppRegistryDataType.AppRegistryData.Builder> appConfigRegistry;

    public UnitRegistryController() throws InstantiationException, InterruptedException {
        super(JPUnitRegistryScope.class, UnitRegistryData.newBuilder());
        try {
            this.unitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.DAL_UNIT_CONFIG_FIELD_NUMBER), new UnitConfigIdGenerator(), JPService.getProperty(JPUnitConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.unitTemplateRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitTemplate.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.UNIT_TEMPLATE_FIELD_NUMBER), new UnitTemplateIdGenerator(), JPService.getProperty(JPUnitTemplateDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.userRegistry = new ProtoBufFileSynchronizedRegistry<>(UserConfigType.UserConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.USER_UNIT_CONFIG_FIELD_NUMBER), new UserConfigIdGenerator(), JPService.getProperty(JPUserConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.authorizationGroupRegistry = new ProtoBufFileSynchronizedRegistry<>(AuthorizationGroupConfigType.AuthorizationGroupConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.AUTHORIZATION_GROUP_UNIT_CONFIG_FIELD_NUMBER), new AuthorizationGroupConfigIdGenerator(), JPService.getProperty(JPAuthorizationGroupConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.deviceConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(DeviceConfigType.DeviceConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.DEVICE_UNIT_CONFIG_FIELD_NUMBER), new DeviceConfigIdGenerator(), JPService.getProperty(JPDeviceConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.unitGroupConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitGroupConfigType.UnitGroupConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.UNIT_GROUP_UNIT_CONFIG_FIELD_NUMBER), new UnitGroupIdGenerator(), JPService.getProperty(JPUnitGroupDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.locationConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(LocationConfigType.LocationConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.LOCATION_UNIT_CONFIG_FIELD_NUMBER), new LocationIDGenerator(), JPService.getProperty(JPLocationConfigDatabaseDirectory.class).getValue(), new ProtoBufJSonFileProvider());
            this.connectionConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(ConnectionConfigType.ConnectionConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.CONNECTION_UNIT_CONFIG_FIELD_NUMBER), new ConnectionIDGenerator(), JPService.getProperty(JPConnectionConfigDatabaseDirectory.class).getValue(), new ProtoBufJSonFileProvider());
            this.agentConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(AgentConfigType.AgentConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.AGENT_UNIT_CONFIG_FIELD_NUMBER), new AgentConfigIdGenerator(), JPService.getProperty(JPAgentConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.sceneConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(SceneConfigType.SceneConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.SCENE_UNIT_CONFIG_FIELD_NUMBER), new SceneConfigIdGenerator(), JPService.getProperty(JPSceneConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            appConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(AppConfigType.AppConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.APP_UNIT_CONFIG_FIELD_NUMBER), new AppConfigIdGenerator(), JPService.getProperty(JPAppConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
        } catch (JPServiceException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void activateVersionControl() throws CouldNotPerformException {
        unitConfigRegistry.activateVersionControl(DummyConverter.class.getPackage());
        userRegistry.activateVersionControl(DummyConverter.class.getPackage());
        authorizationGroupRegistry.activateVersionControl(DummyConverter.class.getPackage());
        deviceConfigRegistry.activateVersionControl(DeviceConfig_0_To_1_DBConverter.class.getPackage());
        unitTemplateRegistry.activateVersionControl(UnitTemplate_0_To_1_DBConverter.class.getPackage());
        unitGroupConfigRegistry.activateVersionControl(UnitGroupConfig_0_To_1_DBConverter.class.getPackage());
        locationConfigRegistry.activateVersionControl(LocationConfig_0_To_1_DBConverter.class.getPackage());
        connectionConfigRegistry.activateVersionControl(LocationConfig_0_To_1_DBConverter.class.getPackage());
        agentConfigRegistry.activateVersionControl(AgentConfig_0_To_1_DBConverter.class.getPackage());
        sceneConfigRegistry.activateVersionControl(SceneConfig_0_To_1_DBConverter.class.getPackage());
        appConfigRegistry.activateVersionControl(DummyConverter.class.getPackage());
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void loadRegistries() throws CouldNotPerformException {
        unitTemplateRegistry.loadRegistry();
        unitConfigRegistry.loadRegistry();
        userRegistry.loadRegistry();
        authorizationGroupRegistry.loadRegistry();
        unitTemplateRegistry.loadRegistry();
        deviceConfigRegistry.loadRegistry();
        unitGroupConfigRegistry.loadRegistry();
        locationConfigRegistry.loadRegistry();
        connectionConfigRegistry.loadRegistry();
        agentConfigRegistry.loadRegistry();
        sceneConfigRegistry.loadRegistry();
        appConfigRegistry.loadRegistry();
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerRegistryRemotes() throws CouldNotPerformException {

    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerConsistencyHandler() throws CouldNotPerformException {
        unitTemplateRegistry.registerConsistencyHandler(new UnitTemplateValidationConsistencyHandler(unitTemplateRegistry));
        unitTemplateRegistry.registerPlugin(new UnitTemplateCreatorRegistryPlugin(unitTemplateRegistry));
        userRegistry.registerConsistencyHandler(new UserConfigUserNameConsistencyHandler());
        userRegistry.registerConsistencyHandler(new UserConfigScopeConsistencyHandler());
        authorizationGroupRegistry.registerConsistencyHandler(new AuthorizationGroupConfigLabelConsistencyHandler());
        authorizationGroupRegistry.registerConsistencyHandler(new AuthorizationGroupConfigScopeConsistencyHandler());
        deviceConfigRegistry.registerConsistencyHandler(new DeviceIdConsistencyHandler());
        deviceConfigRegistry.registerConsistencyHandler(new DeviceConfigDeviceClassIdConsistencyHandler(deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new DeviceLabelConsistencyHandler());
        deviceConfigRegistry.registerConsistencyHandler(new DeviceLocationIdConsistencyHandler(locationRegistryRemote));
        deviceConfigRegistry.registerConsistencyHandler(new DeviceOwnerConsistencyHandler(userRegistryRemote));
        deviceConfigRegistry.registerConsistencyHandler(new DeviceScopeConsistencyHandler(locationRegistryRemote));
        deviceConfigRegistry.registerConsistencyHandler(new DeviceTransformationFrameConsistencyHandler(locationRegistryRemote.getLocationConfigRemoteRegistry()));

        deviceConfigRegistry.registerConsistencyHandler(new UnitScopeConsistencyHandler(locationRegistryRemote));
        deviceConfigRegistry.registerConsistencyHandler(new UnitIdConsistencyHandler());
        deviceConfigRegistry.registerConsistencyHandler(new UnitBoundsToDeviceConsistencyHandler(deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new UnitLabelConsistencyHandler(deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new UnitLocationIdConsistencyHandler(locationRegistryRemote));
        deviceConfigRegistry.registerConsistencyHandler(new UnitTransformationFrameConsistencyHandler(locationRegistryRemote.getLocationConfigRemoteRegistry()));
        deviceConfigRegistry.registerConsistencyHandler(new ServiceConfigUnitIdConsistencyHandler());
        deviceConfigRegistry.registerConsistencyHandler(new ServiceConfigBindingTypeConsistencyHandler(deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new OpenhabServiceConfigItemIdConsistencyHandler(locationRegistryRemote, deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new UnitConfigUnitTemplateConsistencyHandler(unitTemplateRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new UnitConfigUnitTemplateConfigIdConsistencyHandler(deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new DeviceConfigDeviceClassUnitConsistencyHandler(deviceClassRegistry));
        deviceConfigRegistry.registerConsistencyHandler(new DeviceConfigLocationIdForInstalledDevicesConsistencyHandler());

        unitTemplateRegistry.registerConsistencyHandler(new UnitTemplateValidationConsistencyHandler(unitTemplateRegistry));
        unitTemplateRegistry.registerPlugin(new UnitTemplateCreatorRegistryPlugin(unitTemplateRegistry));

        unitGroupConfigRegistry.registerConsistencyHandler(new UnitGroupMemberListDuplicationConsistencyHandler());
        unitGroupConfigRegistry.registerConsistencyHandler(new UnitGroupMemberExistsConsistencyHandler(deviceConfigRegistry));
        unitGroupConfigRegistry.registerConsistencyHandler(new UnitGroupUnitTypeConsistencyHandler(unitTemplateRegistry));
        unitGroupConfigRegistry.registerConsistencyHandler(new UnitGroupMemberListTypesConsistencyHandler(deviceConfigRegistry, unitTemplateRegistry));
        unitGroupConfigRegistry.registerConsistencyHandler(new UnitGroupScopeConsistencyHandler(locationRegistryRemote));

        locationConfigRegistry.registerConsistencyHandler(new LocationPlacementConfigConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationPositionConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new RootConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationChildConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationIdConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationParentConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new RootLocationExistencConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationLoopConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new ChildWithSameLabelConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationScopeConsistencyHandler());
        locationConfigRegistry.registerConsistencyHandler(new LocationUnitIdConsistencyHandler(deviceRegistryRemote));
        locationConfigRegistry.registerConsistencyHandler(new LocationTransformationFrameConsistencyHandler(locationConfigRegistry));
        locationConfigRegistry.registerPlugin(new PublishLocationTransformationRegistryPlugin());

        connectionConfigRegistry.registerConsistencyHandler(new ConnectionLabelConsistencyHandler());
        connectionConfigRegistry.registerConsistencyHandler(new ConnectionTilesConsistencyHandler(locationConfigRegistry));
        connectionConfigRegistry.registerConsistencyHandler(new ConnectionLocationConsistencyHandler(locationConfigRegistry));
        connectionConfigRegistry.registerConsistencyHandler(new ConnectionScopeConsistencyHandler(locationConfigRegistry));
        connectionConfigRegistry.registerConsistencyHandler(new ConnectionTransformationFrameConsistencyHandler(locationConfigRegistry));
        connectionConfigRegistry.registerPlugin(new PublishConnectionTransformationRegistryPlugin(locationConfigRegistry));

        //TODO: should be activated but fails in the current db version since agentClasses have just been introduced
        //agentConfigRegistry.registerConsistencyHandler(new AgentConfigAgentClassIdConsistencyHandler(agentClassRegistry));
        agentConfigRegistry.registerConsistencyHandler(new LocationIdConsistencyHandler(locationRegistryRemote));
        agentConfigRegistry.registerConsistencyHandler(new LabelConsistencyHandler());
        agentConfigRegistry.registerConsistencyHandler(new ScopeConsistencyHandler(locationRegistryRemote));

        sceneConfigRegistry.registerConsistencyHandler(new ScopeConsistencyHandler(locationRegistryRemote));
        sceneConfigRegistry.registerConsistencyHandler(new LabelConsistencyHandler());

        //TODO: should be activated but fails in the current db version since appClasses have just been introduced
        //appConfigRegistry.registerConsistencyHandler(new AppConfigAppClassIdConsistencyHandler(appClassRegistry));
        appConfigRegistry.registerConsistencyHandler(new ScopeConsistencyHandler(locationRegistryRemote));
        appConfigRegistry.registerConsistencyHandler(new LabelConsistencyHandler());
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerObserver() throws CouldNotPerformException {
        unitConfigRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>>> source, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> data) -> {
            notifyChange();
        });
        unitTemplateRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, UnitTemplate, UnitTemplate.Builder>>> source, Map<String, IdentifiableMessage<String, UnitTemplate, UnitTemplate.Builder>> data) -> {
            notifyChange();
        });

        userRegistry.addObserver((final Observable<Map<String, IdentifiableMessage<String, UserConfigType.UserConfig, UserConfigType.UserConfig.Builder>>> source, Map<String, IdentifiableMessage<String, UserConfigType.UserConfig, UserConfigType.UserConfig.Builder>> data) -> {
            notifyChange();
        });

        authorizationGroupRegistry.addObserver((final Observable<Map<String, IdentifiableMessage<String, AuthorizationGroupConfigType.AuthorizationGroupConfig, AuthorizationGroupConfigType.AuthorizationGroupConfig.Builder>>> source, Map<String, IdentifiableMessage<String, AuthorizationGroupConfigType.AuthorizationGroupConfig, AuthorizationGroupConfigType.AuthorizationGroupConfig.Builder>> data) -> {
            notifyChange();
        });

        unitTemplateRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, UnitTemplate, UnitTemplate.Builder>>> source, Map<String, IdentifiableMessage<String, UnitTemplate, UnitTemplate.Builder>> data) -> {
            notifyChange();
        });

        deviceConfigRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, DeviceConfigType.DeviceConfig, DeviceConfigType.DeviceConfig.Builder>>> source, Map<String, IdentifiableMessage<String, DeviceConfigType.DeviceConfig, DeviceConfigType.DeviceConfig.Builder>> data) -> {
            notifyChange();
        });

        unitGroupConfigRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, UnitGroupConfigType.UnitGroupConfig, UnitGroupConfigType.UnitGroupConfig.Builder>>> source, Map<String, IdentifiableMessage<String, UnitGroupConfigType.UnitGroupConfig, UnitGroupConfigType.UnitGroupConfig.Builder>> data) -> {
            notifyChange();
        });

        locationConfigRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder>>> source, Map<String, IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder>> data) -> {
            notifyChange();
        });

        connectionConfigRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, ConnectionConfigType.ConnectionConfig, ConnectionConfigType.ConnectionConfig.Builder>>> source, Map<String, IdentifiableMessage<String, ConnectionConfigType.ConnectionConfig, ConnectionConfigType.ConnectionConfig.Builder>> data) -> {
            notifyChange();
        });

        agentConfigRegistry.addObserver((final Observable<Map<String, IdentifiableMessage<String, AgentConfigType.AgentConfig, AgentConfigType.AgentConfig.Builder>>> source, Map<String, IdentifiableMessage<String, AgentConfigType.AgentConfig, AgentConfigType.AgentConfig.Builder>> data) -> {
            notifyChange();
        });

        sceneConfigRegistry.addObserver((final Observable<Map<String, IdentifiableMessage<String, SceneConfigType.SceneConfig, SceneConfigType.SceneConfig.Builder>>> source, Map<String, IdentifiableMessage<String, SceneConfigType.SceneConfig, SceneConfigType.SceneConfig.Builder>> data) -> {
            notifyChange();
        });
        
        appConfigRegistry.addObserver((Observable<Map<String, IdentifiableMessage<String, AppConfigType.AppConfig, AppConfigType.AppConfig.Builder>>> source, Map<String, IdentifiableMessage<String, AppConfigType.AppConfig, AppConfigType.AppConfig.Builder>> data) -> {
            notifyChange();
        });

    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerDependencies() throws CouldNotPerformException {
        unitConfigRegistry.registerDependency(unitTemplateRegistry);
        authorizationGroupRegistry.registerDependency(userRegistry);
        deviceConfigRegistry.registerDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        deviceConfigRegistry.registerDependency(userRegistryRemote.getUserConfigRemoteRegistry());
        deviceConfigRegistry.registerDependency(deviceClassRegistry);
        unitGroupConfigRegistry.registerDependency(deviceConfigRegistry);
        locationConfigRegistry.registerDependency(deviceRegistryRemote.getDeviceConfigRemoteRegistry());
        connectionConfigRegistry.registerDependency(deviceRegistryRemote.getDeviceConfigRemoteRegistry());
        agentConfigRegistry.registerDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        agentConfigRegistry.registerDependency(agentClassRegistry);
        sceneConfigRegistry.registerDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        appConfigRegistry.registerDependency(appClassRegistry);
            appConfigRegistry.registerDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void removeDependencies() throws CouldNotPerformException {
        unitConfigRegistry.removeDependency(unitTemplateRegistry);
        authorizationGroupRegistry.removeDependency(userRegistry);
        deviceConfigRegistry.removeDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        deviceConfigRegistry.removeDependency(userRegistryRemote.getUserConfigRemoteRegistry());
        deviceConfigRegistry.removeDependency(deviceClassRegistry);
        unitGroupConfigRegistry.removeDependency(deviceConfigRegistry);
        locationConfigRegistry.removeDependency(deviceRegistryRemote.getDeviceConfigRemoteRegistry());
        connectionConfigRegistry.removeDependency(deviceRegistryRemote.getDeviceConfigRemoteRegistry());
        agentConfigRegistry.removeDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        agentConfigRegistry.removeDependency(agentClassRegistry);
        sceneConfigRegistry.removeDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        appConfigRegistry.removeDependency(appClassRegistry);
        appConfigRegistry.removeDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void performInitialConsistencyCheck() throws CouldNotPerformException, InterruptedException {
        try {
            unitTemplateRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            unitConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            userRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            authorizationGroupRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }
        try {
            unitTemplateRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            deviceConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            unitGroupConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            deviceConfigRegistry.registerPlugin(new PublishDeviceTransformationRegistryPlugin(locationRegistryRemote));
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not load all plugins!", ex), logger, LogLevel.ERROR);
            notifyChange();
        }

        try {
            locationConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            connectionConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            agentConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }

        try {
            sceneConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }
        
        try {
            appConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial consistency check failed!", ex), logger, LogLevel.WARN);
            notifyChange();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException
     * @throws CouldNotPerformException
     */
    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {

        if (userRegistry != null) {
            userRegistry.deactivate();
        }

        if (authorizationGroupRegistry != null) {
            authorizationGroupRegistry.deactivate();
        }
        super.deactivate();
    }

    @Override
    public void shutdown() {
        if (unitConfigRegistry != null) {
            unitConfigRegistry.shutdown();
        }

        if (unitTemplateRegistry != null) {
            unitTemplateRegistry.shutdown();
        }

        if (deviceConfigRegistry != null) {
            deviceConfigRegistry.shutdown();
        }

        if (unitTemplateRegistry != null) {
            unitTemplateRegistry.shutdown();
        }

        if (unitGroupConfigRegistry != null) {
            unitGroupConfigRegistry.shutdown();
        }

        if (locationConfigRegistry != null) {
            locationConfigRegistry.shutdown();
        }

        if (connectionConfigRegistry != null) {
            connectionConfigRegistry.shutdown();
        }

        if (sceneConfigRegistry != null) {
            sceneConfigRegistry.shutdown();
        }
        
        if (appConfigRegistry != null) {
            appConfigRegistry.shutdown();
        }
        
        if (agentConfigRegistry != null) {
            agentConfigRegistry.shutdown();
        }

        super.shutdown();
    }

    @Override
    public final void syncDataTypeFlags() throws CouldNotPerformException, InterruptedException {
        setDataField(UnitRegistryData.UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, unitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, unitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.UNIT_TEMPLATE_REGISTRY_READ_ONLY_FIELD_NUMBER, unitTemplateRegistry.isReadOnly());
        setDataField(UnitRegistryData.UNIT_TEMPLATE_REGISTRY_CONSISTENT_FIELD_NUMBER, unitTemplateRegistry.isConsistent());

        setDataField(UserRegistryDataType.UserRegistryData.USER_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, userRegistry.isReadOnly());
        setDataField(UserRegistryDataType.UserRegistryData.AUTHORIZATION_GROUP_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, authorizationGroupRegistry.isReadOnly());
        setDataField(UserRegistryDataType.UserRegistryData.USER_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, userRegistry.isConsistent());
        setDataField(UserRegistryDataType.UserRegistryData.AUTHORIZATION_GROUP_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, authorizationGroupRegistry.isConsistent());

        setDataField(DeviceRegistryDataType.DeviceRegistryData.DEVICE_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, deviceConfigRegistry.isReadOnly());
        setDataField(DeviceRegistryDataType.DeviceRegistryData.UNIT_TEMPLATE_REGISTRY_READ_ONLY_FIELD_NUMBER, unitTemplateRegistry.isReadOnly());
        setDataField(DeviceRegistryDataType.DeviceRegistryData.UNIT_GROUP_REGISTRY_READ_ONLY_FIELD_NUMBER, unitGroupConfigRegistry.isReadOnly());
        setDataField(DeviceRegistryDataType.DeviceRegistryData.DEVICE_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, deviceConfigRegistry.isConsistent());
        setDataField(DeviceRegistryDataType.DeviceRegistryData.UNIT_TEMPLATE_REGISTRY_CONSISTENT_FIELD_NUMBER, unitTemplateRegistry.isConsistent());
        setDataField(DeviceRegistryDataType.DeviceRegistryData.UNIT_GROUP_REGISTRY_CONSISTENT_FIELD_NUMBER, unitGroupConfigRegistry.isConsistent());

        setDataField(LocationRegistryDataType.LocationRegistryData.LOCATION_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, locationConfigRegistry.isReadOnly());
        setDataField(LocationRegistryDataType.LocationRegistryData.CONNECTION_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, connectionConfigRegistry.isReadOnly());
        setDataField(LocationRegistryDataType.LocationRegistryData.LOCATION_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, locationConfigRegistry.isConsistent());
        setDataField(LocationRegistryDataType.LocationRegistryData.CONNECTION_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, connectionConfigRegistry.isConsistent());

        setDataField(SceneRegistryDataType.SceneRegistryData.SCENE_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, sceneConfigRegistry.isReadOnly());
        setDataField(SceneRegistryDataType.SceneRegistryData.SCENE_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, sceneConfigRegistry.isConsistent());
        
        setDataField(AgentRegistryDataType.AgentRegistryData.AGENT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, agentConfigRegistry.isReadOnly());
        setDataField(AgentRegistryDataType.AgentRegistryData.AGENT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, agentConfigRegistry.isConsistent());
        
        setDataField(AppRegistryDataType.AppRegistryData.APP_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, appConfigRegistry.isReadOnly());
        setDataField(AppRegistryDataType.AppRegistryData.APP_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, appConfigRegistry.isConsistent());

        super.notifyChange();
    }

    @Override
    public void registerMethods(final RSBLocalServer server) throws CouldNotPerformException {
        RPCHelper.registerInterface(UnitRegistry.class, this, server);
    }

    @Override
    public Future<UnitConfig> registerUnitConfig(UnitConfig unitConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> unitConfigRegistry.register(unitConfig));
    }

    @Override
    public UnitConfig getUnitConfigById(String unitConfigId) throws CouldNotPerformException {
        return unitConfigRegistry.get(unitConfigId).getMessage();
    }

    @Override
    public Boolean containsUnitConfigById(String sceneConfigId) throws CouldNotPerformException {
        return unitConfigRegistry.contains(sceneConfigId);
    }

    @Override
    public Boolean containsUnitConfig(UnitConfig unitConfig) throws CouldNotPerformException {
        return unitConfigRegistry.contains(unitConfig);
    }

    @Override
    public Future<UnitConfig> updateUnitConfig(UnitConfig unitConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> unitConfigRegistry.update(unitConfig));
    }

    @Override
    public Future<UnitConfig> removeUnitConfig(UnitConfig unitConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> unitConfigRegistry.remove(unitConfig));
    }

    @Override
    public List<UnitConfig> getUnitConfigs() throws CouldNotPerformException {
        return unitConfigRegistry.getMessages();
    }

    @Override
    public Boolean isUnitConfigRegistryReadOnly() throws CouldNotPerformException {
        return unitConfigRegistry.isReadOnly();
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getUnitConfigRegistry() {
        return unitConfigRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean isUnitConfigRegistryConsistent() throws CouldNotPerformException {
        return unitConfigRegistry.isConsistent();
    }

    /**
     * {@inheritDoc}
     *
     * @param unitTemplateId {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public UnitTemplate getUnitTemplateById(String unitTemplateId) throws CouldNotPerformException {
        return unitTemplateRegistry.get(unitTemplateId).getMessage();
    }

    /**
     * {@inheritDoc}
     *
     * @param unitTemplateId {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean containsUnitTemplateById(String unitTemplateId) throws CouldNotPerformException {
        return unitTemplateRegistry.contains(unitTemplateId);
    }

    /**
     * {@inheritDoc}
     *
     * @param unitTemplate {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean containsUnitTemplate(UnitTemplate unitTemplate) throws CouldNotPerformException {
        return unitTemplateRegistry.contains(unitTemplate);
    }

    /**
     * {@inheritDoc}
     *
     * @param unitTemplate {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Future<UnitTemplate> updateUnitTemplate(UnitTemplate unitTemplate) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> unitTemplateRegistry.update(unitTemplate));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitTemplate> getUnitTemplates() throws CouldNotPerformException {
        return unitTemplateRegistry.getMessages();
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public UnitTemplate getUnitTemplateByType(final UnitType type) throws CouldNotPerformException {
        return unitTemplateRegistry.getMessage(((UnitTemplateIdGenerator) unitTemplateRegistry.getIdGenerator()).generateId(type));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Boolean isUnitTemplateRegistryReadOnly() throws CouldNotPerformException {
        return unitTemplateRegistry.isReadOnly();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Boolean isUnitTemplateRegistryConsistent() throws CouldNotPerformException {
        return unitTemplateRegistry.isConsistent();
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitTemplate, UnitTemplate.Builder, UnitRegistryData.Builder> getUnitTemplateRegistry() {
        return unitTemplateRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UserConfigType.UserConfig, UserConfigType.UserConfig.Builder, UserRegistryDataType.UserRegistryData.Builder> getUserRegistry() {
        return userRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, AuthorizationGroupConfigType.AuthorizationGroupConfig, AuthorizationGroupConfigType.AuthorizationGroupConfig.Builder, UserRegistryDataType.UserRegistryData.Builder> getAuthorizationGroupRegistry() {
        return authorizationGroupRegistry;
    }
}
