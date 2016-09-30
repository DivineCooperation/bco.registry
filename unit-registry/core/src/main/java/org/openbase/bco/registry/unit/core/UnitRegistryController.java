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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.openbase.bco.registry.agent.remote.AgentRegistryRemote;
import org.openbase.bco.registry.app.remote.AppRegistryRemote;
import org.openbase.bco.registry.device.remote.DeviceRegistryRemote;
import org.openbase.bco.registry.lib.com.AbstractRegistryController;
import org.openbase.bco.registry.lib.util.UnitConfigUtils;
import org.openbase.bco.registry.unit.core.consistency.ServiceConfigUnitIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.UnitConfigUnitTemplateConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.UnitEnablingStateConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.agent.AgentLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.agent.AgentLocationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.agent.AgentScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.app.AppLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.app.AppLocationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.app.AppScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.authorizationgroup.AuthorizationGroupConfigLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.authorizationgroup.AuthorizationGroupConfigScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionLocationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionTilesConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.connection.ConnectionTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.DalUnitEnablingStateConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.DalUnitHostIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.DalUnitLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.DalUnitLocationIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.DalUnitScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.UnitBoundToHostConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.dal.UnitTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceConfigDeviceClassIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceConfigDeviceClassUnitConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceConfigLocationIdForInstalledDevicesConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceEnablingStateConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceLocationIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceOwnerConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.DeviceTransformationFrameConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.OpenhabServiceConfigItemIdConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.device.SyncBindingConfigDeviceClassUnitConsistencyHandler;
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
import org.openbase.bco.registry.unit.core.consistency.location.RootConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.location.RootLocationExistencConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.scene.SceneLabelConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.scene.SceneScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupMemberExistsConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupMemberListDuplicationConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupMemberListTypesConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.unitgroup.UnitGroupUnitTypeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.user.UserConfigScopeConsistencyHandler;
import org.openbase.bco.registry.unit.core.consistency.user.UserConfigUserNameConsistencyHandler;
import org.openbase.bco.registry.unit.core.dbconvert.DummyConverter;
import org.openbase.bco.registry.unit.core.plugin.PublishConnectionTransformationRegistryPlugin;
import org.openbase.bco.registry.unit.core.plugin.PublishDalUnitTransformationRegistryPlugin;
import org.openbase.bco.registry.unit.core.plugin.PublishDeviceTransformationRegistryPlugin;
import org.openbase.bco.registry.unit.core.plugin.PublishLocationTransformationRegistryPlugin;
import org.openbase.bco.registry.unit.core.plugin.UnitTemplateCreatorRegistryPlugin;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.lib.generator.UnitConfigIdGenerator;
import org.openbase.bco.registry.unit.lib.generator.UnitTemplateIdGenerator;
import org.openbase.bco.registry.unit.lib.jp.JPAgentConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPAppConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPAuthorizationGroupConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPConnectionConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPDalUnitConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPDeviceConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPLocationConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPSceneConfigDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPUnitGroupDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPUnitRegistryScope;
import org.openbase.bco.registry.unit.lib.jp.JPUnitTemplateDatabaseDirectory;
import org.openbase.bco.registry.unit.lib.jp.JPUserConfigDatabaseDirectory;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.extension.rsb.com.RPCHelper;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.iface.Manageable;
import org.openbase.jul.schedule.GlobalExecutionService;
import org.openbase.jul.storage.file.ProtoBufJSonFileProvider;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.authorization.AuthorizationGroupConfigType.AuthorizationGroupConfig;
import rst.authorization.UserConfigType.UserConfig;
import rst.homeautomation.control.agent.AgentConfigType.AgentConfig;
import rst.homeautomation.control.app.AppConfigType.AppConfig;
import rst.homeautomation.control.scene.SceneConfigType.SceneConfig;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;
import rst.homeautomation.service.ServiceConfigType.ServiceConfig;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.homeautomation.unit.UnitGroupConfigType.UnitGroupConfig;
import rst.homeautomation.unit.UnitRegistryDataType.UnitRegistryData;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.rsb.ScopeType;
import rst.spatial.ConnectionConfigType.ConnectionConfig;
import rst.spatial.LocationConfigType.LocationConfig;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class UnitRegistryController extends AbstractRegistryController<UnitRegistryData, UnitRegistryData.Builder> implements UnitRegistry, Manageable<ScopeType.Scope> {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitTemplate.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UserConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AuthorizationGroupConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DeviceConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitGroupConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(LocationConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(ConnectionConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AgentConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppConfig.getDefaultInstance()));
    }

    public final static UnitConfigIdGenerator UNIT_ID_GENERATOR = new UnitConfigIdGenerator();

    private final ProtoBufFileSynchronizedRegistry<String, UnitTemplate, UnitTemplate.Builder, UnitRegistryData.Builder> unitTemplateRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> dalUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> userUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> authorizationGroupUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> deviceUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> unitGroupUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> locationUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> connectionUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> agentUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> sceneUnitConfigRegistry;
    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> appUnitConfigRegistry;

    private final DeviceRegistryRemote deviceRegistryRemote;
    private final AppRegistryRemote appRegistryRemote;
    private final AgentRegistryRemote agentRegistryRemote;

    private final ArrayList<ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder>> unitConfigRegistryList;

    public UnitRegistryController() throws InstantiationException, InterruptedException {
        super(JPUnitRegistryScope.class, UnitRegistryData.newBuilder());
        try {
            this.unitConfigRegistryList = new ArrayList();
            this.unitTemplateRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitTemplate.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.UNIT_TEMPLATE_FIELD_NUMBER), new UnitTemplateIdGenerator(), JPService.getProperty(JPUnitTemplateDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.dalUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.DAL_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPDalUnitConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.userUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.USER_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPUserConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.authorizationGroupUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.AUTHORIZATION_GROUP_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPAuthorizationGroupConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.deviceUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.DEVICE_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPDeviceConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.unitGroupUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.UNIT_GROUP_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPUnitGroupDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.locationUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.LOCATION_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPLocationConfigDatabaseDirectory.class).getValue(), new ProtoBufJSonFileProvider());
            this.connectionUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.CONNECTION_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPConnectionConfigDatabaseDirectory.class).getValue(), new ProtoBufJSonFileProvider());
            this.agentUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.AGENT_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPAgentConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.sceneUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.SCENE_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPSceneConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            this.appUnitConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(UnitConfig.class, getBuilderSetup(), getDataFieldDescriptor(UnitRegistryData.APP_UNIT_CONFIG_FIELD_NUMBER), UNIT_ID_GENERATOR, JPService.getProperty(JPAppConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);

            this.unitConfigRegistryList.add(dalUnitConfigRegistry);
            this.unitConfigRegistryList.add(userUnitConfigRegistry);
            this.unitConfigRegistryList.add(authorizationGroupUnitConfigRegistry);
            this.unitConfigRegistryList.add(deviceUnitConfigRegistry);
            this.unitConfigRegistryList.add(unitGroupUnitConfigRegistry);
            this.unitConfigRegistryList.add(locationUnitConfigRegistry);
            this.unitConfigRegistryList.add(connectionUnitConfigRegistry);
            this.unitConfigRegistryList.add(sceneUnitConfigRegistry);
            this.unitConfigRegistryList.add(agentUnitConfigRegistry);
            this.unitConfigRegistryList.add(appUnitConfigRegistry);

            this.deviceRegistryRemote = new DeviceRegistryRemote();
            this.appRegistryRemote = new AppRegistryRemote();
            this.agentRegistryRemote = new AgentRegistryRemote();

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
    protected Package getVersionConverterPackage() throws CouldNotPerformException {
        return DummyConverter.class.getPackage();
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerRegistryRemotes() throws CouldNotPerformException {
        registerRegistryRemote(deviceRegistryRemote);
        registerRegistryRemote(appRegistryRemote);
        registerRegistryRemote(agentRegistryRemote);
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerRegistries() throws CouldNotPerformException {
        registerRegistry(unitTemplateRegistry);
        for (ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> registry : unitConfigRegistryList) {
            registerRegistry(registry);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerConsistencyHandler() throws CouldNotPerformException {
        //TODO: should be activated but fails in the current db version since agentClasses have just been introduced
        //agentUnitConfigRegistry.registerConsistencyHandler(new AgentConfigAgentClassIdConsistencyHandler(agentClassRegistry));
        agentUnitConfigRegistry.registerConsistencyHandler(new AgentLabelConsistencyHandler());
        agentUnitConfigRegistry.registerConsistencyHandler(new AgentLocationConsistencyHandler(locationUnitConfigRegistry));
        agentUnitConfigRegistry.registerConsistencyHandler(new AgentScopeConsistencyHandler(locationUnitConfigRegistry));

        //TODO: should be activated but fails in the current db version since appClasses have just been introduced
        //appConfigRegistry.registerConsistencyHandler(new AppConfigAppClassIdConsistencyHandler(appClassRegistry));
        appUnitConfigRegistry.registerConsistencyHandler(new AppLabelConsistencyHandler());
        appUnitConfigRegistry.registerConsistencyHandler(new AppLocationConsistencyHandler(locationUnitConfigRegistry));
        appUnitConfigRegistry.registerConsistencyHandler(new AppScopeConsistencyHandler(locationUnitConfigRegistry));

        authorizationGroupUnitConfigRegistry.registerConsistencyHandler(new AuthorizationGroupConfigLabelConsistencyHandler());
        authorizationGroupUnitConfigRegistry.registerConsistencyHandler(new AuthorizationGroupConfigScopeConsistencyHandler());

        connectionUnitConfigRegistry.registerConsistencyHandler(new ConnectionLabelConsistencyHandler());
        connectionUnitConfigRegistry.registerConsistencyHandler(new ConnectionTilesConsistencyHandler(locationUnitConfigRegistry));
        connectionUnitConfigRegistry.registerConsistencyHandler(new ConnectionLocationConsistencyHandler(locationUnitConfigRegistry));
        connectionUnitConfigRegistry.registerConsistencyHandler(new ConnectionScopeConsistencyHandler(locationUnitConfigRegistry));
        connectionUnitConfigRegistry.registerConsistencyHandler(new ConnectionTransformationFrameConsistencyHandler(locationUnitConfigRegistry));

        //TODO replace null with device class remote registry
        dalUnitConfigRegistry.registerConsistencyHandler(new DalUnitEnablingStateConsistencyHandler(deviceUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new DalUnitHostIdConsistencyHandler(deviceUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new DalUnitLabelConsistencyHandler(deviceRegistryRemote.getDeviceClassRemoteRegistry(), deviceUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new DalUnitLocationIdConsistencyHandler(locationUnitConfigRegistry, deviceUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new DalUnitScopeConsistencyHandler(locationUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new UnitBoundToHostConsistencyHandler(deviceUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new DalUnitEnablingStateConsistencyHandler(deviceUnitConfigRegistry));
        dalUnitConfigRegistry.registerConsistencyHandler(new UnitTransformationFrameConsistencyHandler(locationUnitConfigRegistry));

        //TODO replace null with device class remote registry
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceConfigDeviceClassIdConsistencyHandler(deviceRegistryRemote.getDeviceClassRemoteRegistry()));
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceConfigDeviceClassUnitConsistencyHandler(deviceRegistryRemote.getDeviceClassRemoteRegistry(), dalUnitConfigRegistry));
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceConfigLocationIdForInstalledDevicesConsistencyHandler());
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceEnablingStateConsistencyHandler());
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceLabelConsistencyHandler());
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceLocationIdConsistencyHandler(locationUnitConfigRegistry));
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceOwnerConsistencyHandler(userUnitConfigRegistry));
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceScopeConsistencyHandler(locationUnitConfigRegistry));
        deviceUnitConfigRegistry.registerConsistencyHandler(new DeviceTransformationFrameConsistencyHandler(locationUnitConfigRegistry));
        deviceUnitConfigRegistry.registerConsistencyHandler(new OpenhabServiceConfigItemIdConsistencyHandler(deviceRegistryRemote.getDeviceClassRemoteRegistry(), locationUnitConfigRegistry, dalUnitConfigRegistry));
        deviceUnitConfigRegistry.registerConsistencyHandler(new SyncBindingConfigDeviceClassUnitConsistencyHandler(deviceRegistryRemote.getDeviceClassRemoteRegistry(), dalUnitConfigRegistry));

        userUnitConfigRegistry.registerConsistencyHandler(new UserConfigScopeConsistencyHandler());
        userUnitConfigRegistry.registerConsistencyHandler(new UserConfigUserNameConsistencyHandler());

        unitGroupUnitConfigRegistry.registerConsistencyHandler(new UnitGroupMemberListDuplicationConsistencyHandler());
        unitGroupUnitConfigRegistry.registerConsistencyHandler(new UnitGroupMemberExistsConsistencyHandler(agentUnitConfigRegistry, appUnitConfigRegistry, authorizationGroupUnitConfigRegistry, connectionUnitConfigRegistry, dalUnitConfigRegistry, deviceUnitConfigRegistry, locationUnitConfigRegistry, sceneUnitConfigRegistry, unitGroupUnitConfigRegistry, userUnitConfigRegistry));
        unitGroupUnitConfigRegistry.registerConsistencyHandler(new UnitGroupUnitTypeConsistencyHandler(unitTemplateRegistry));
        unitGroupUnitConfigRegistry.registerConsistencyHandler(new UnitGroupMemberListTypesConsistencyHandler(agentUnitConfigRegistry, appUnitConfigRegistry, authorizationGroupUnitConfigRegistry, connectionUnitConfigRegistry, dalUnitConfigRegistry, deviceUnitConfigRegistry, locationUnitConfigRegistry, sceneUnitConfigRegistry, unitGroupUnitConfigRegistry, userUnitConfigRegistry, unitTemplateRegistry));
        unitGroupUnitConfigRegistry.registerConsistencyHandler(new UnitGroupScopeConsistencyHandler(locationUnitConfigRegistry));

        locationUnitConfigRegistry.registerConsistencyHandler(new LocationPlacementConfigConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationPositionConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new RootConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationChildConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationIdConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationParentConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new RootLocationExistencConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationLoopConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new ChildWithSameLabelConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationScopeConsistencyHandler());
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationUnitIdConsistencyHandler(agentUnitConfigRegistry, appUnitConfigRegistry, authorizationGroupUnitConfigRegistry, connectionUnitConfigRegistry, dalUnitConfigRegistry, deviceUnitConfigRegistry, sceneUnitConfigRegistry, unitGroupUnitConfigRegistry, userUnitConfigRegistry));
        locationUnitConfigRegistry.registerConsistencyHandler(new LocationTransformationFrameConsistencyHandler(locationUnitConfigRegistry));

        sceneUnitConfigRegistry.registerConsistencyHandler(new SceneLabelConsistencyHandler());
        sceneUnitConfigRegistry.registerConsistencyHandler(new SceneScopeConsistencyHandler(locationUnitConfigRegistry));

        // add consistency handler for all unitConfig registries
        registerConsistencyHandler(new ServiceConfigUnitIdConsistencyHandler(), UnitConfig.class);
        registerConsistencyHandler(new UnitConfigUnitTemplateConsistencyHandler(unitTemplateRegistry), UnitConfig.class);
        registerConsistencyHandler(new UnitEnablingStateConsistencyHandler(), UnitConfig.class);
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    protected void registerPlugins() throws CouldNotPerformException, InterruptedException {
        connectionUnitConfigRegistry.registerPlugin(new PublishConnectionTransformationRegistryPlugin(locationUnitConfigRegistry));
        dalUnitConfigRegistry.registerPlugin(new PublishDalUnitTransformationRegistryPlugin(locationUnitConfigRegistry));
        deviceUnitConfigRegistry.registerPlugin(new PublishDeviceTransformationRegistryPlugin(locationUnitConfigRegistry));
        unitTemplateRegistry.registerPlugin(new UnitTemplateCreatorRegistryPlugin(unitTemplateRegistry));
        locationUnitConfigRegistry.registerPlugin(new PublishLocationTransformationRegistryPlugin());
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected void registerDependencies() throws CouldNotPerformException {
        registerDependency(unitTemplateRegistry, UnitConfig.class);

        dalUnitConfigRegistry.registerDependency(deviceUnitConfigRegistry);
        dalUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);

        authorizationGroupUnitConfigRegistry.registerDependency(userUnitConfigRegistry);

        deviceUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);
        deviceUnitConfigRegistry.registerDependency(userUnitConfigRegistry);
        deviceUnitConfigRegistry.registerDependency(deviceRegistryRemote.getDeviceClassRemoteRegistry());

        unitGroupUnitConfigRegistry.registerDependency(agentUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(appUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(authorizationGroupUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(connectionUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(dalUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(deviceUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(sceneUnitConfigRegistry);
        unitGroupUnitConfigRegistry.registerDependency(unitTemplateRegistry);
        unitGroupUnitConfigRegistry.registerDependency(userUnitConfigRegistry);

        locationUnitConfigRegistry.registerDependency(agentUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(appUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(authorizationGroupUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(connectionUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(dalUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(deviceUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(sceneUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(unitGroupUnitConfigRegistry);
        locationUnitConfigRegistry.registerDependency(userUnitConfigRegistry);

        connectionUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);

        agentUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);
        agentUnitConfigRegistry.registerDependency(agentRegistryRemote.getAgentConfigRemoteRegistry());

        sceneUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);

        appUnitConfigRegistry.registerDependency(appRegistryRemote.getAppClassRemoteRegistry());
        appUnitConfigRegistry.registerDependency(locationUnitConfigRegistry);
    }

    @Override
    public final void syncRegistryFlags() throws CouldNotPerformException, InterruptedException {
        setDataField(UnitRegistryData.DAL_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, dalUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.DAL_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, dalUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.UNIT_TEMPLATE_REGISTRY_READ_ONLY_FIELD_NUMBER, unitTemplateRegistry.isReadOnly());
        setDataField(UnitRegistryData.UNIT_TEMPLATE_REGISTRY_CONSISTENT_FIELD_NUMBER, unitTemplateRegistry.isConsistent());

        setDataField(UnitRegistryData.USER_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, userUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.USER_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, userUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.AUTHORIZATION_GROUP_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, authorizationGroupUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.AUTHORIZATION_GROUP_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, authorizationGroupUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.DEVICE_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, deviceUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.DEVICE_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, deviceUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.UNIT_GROUP_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, unitGroupUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.UNIT_GROUP_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, unitGroupUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.LOCATION_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, locationUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.LOCATION_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, locationUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.CONNECTION_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, connectionUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.CONNECTION_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, connectionUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.SCENE_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, sceneUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.SCENE_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, sceneUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.AGENT_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, agentUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.AGENT_UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, agentUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.APP_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, appUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.APP_UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, appUnitConfigRegistry.isConsistent());

        setDataField(UnitRegistryData.UNIT_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, dalUnitConfigRegistry.isReadOnly()
                || unitTemplateRegistry.isReadOnly() || userUnitConfigRegistry.isReadOnly() || authorizationGroupUnitConfigRegistry.isReadOnly()
                || deviceUnitConfigRegistry.isReadOnly() || unitGroupUnitConfigRegistry.isReadOnly() || locationUnitConfigRegistry.isReadOnly()
                || connectionUnitConfigRegistry.isReadOnly() || sceneUnitConfigRegistry.isReadOnly() || agentUnitConfigRegistry.isReadOnly()
                || appUnitConfigRegistry.isReadOnly());
        setDataField(UnitRegistryData.UNIT_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, dalUnitConfigRegistry.isConsistent()
                || unitTemplateRegistry.isConsistent() || userUnitConfigRegistry.isConsistent() || authorizationGroupUnitConfigRegistry.isConsistent()
                || deviceUnitConfigRegistry.isConsistent() || unitGroupUnitConfigRegistry.isConsistent() || locationUnitConfigRegistry.isConsistent()
                || connectionUnitConfigRegistry.isConsistent() || sceneUnitConfigRegistry.isConsistent() || agentUnitConfigRegistry.isConsistent()
                || appUnitConfigRegistry.isConsistent());
        super.notifyChange();
    }

    @Override
    public void registerMethods(final RSBLocalServer server) throws CouldNotPerformException {
        RPCHelper.registerInterface(UnitRegistry.class, this, server);
    }

    private ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getUnitConfigRegistry(final UnitType unitType) {
        switch (unitType) {
            case AUTHORIZATION_GROUP:
                return authorizationGroupUnitConfigRegistry;
            case AGENT:
                return agentUnitConfigRegistry;
            case APP:
                return appUnitConfigRegistry;
            case CONNECTION:
                return connectionUnitConfigRegistry;
            case DEVICE:
                return deviceUnitConfigRegistry;
            case LOCATION:
                return locationUnitConfigRegistry;
            case SCENE:
                return sceneUnitConfigRegistry;
            case UNIT_GROUP:
                return unitGroupUnitConfigRegistry;
            case USER:
                return userUnitConfigRegistry;
            default:
                return dalUnitConfigRegistry;
        }
    }

    private void verifyUnitGroupUnitConfig(UnitConfig unitConfig) throws VerificationFailedException {
        UnitConfigUtils.verifyUnitType(unitConfig, UnitType.UNIT_GROUP);
    }

    @Override
    public Future<UnitConfig> registerUnitConfig(final UnitConfig unitConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> getUnitConfigRegistry(unitConfig.getType()).register(unitConfig));
    }

    @Override
    public UnitConfig getUnitConfigById(final String unitConfigId) throws CouldNotPerformException {
        return dalUnitConfigRegistry.get(unitConfigId).getMessage();
    }

    @Override
    public Boolean containsUnitConfigById(final String sceneConfigId) throws CouldNotPerformException {
        return dalUnitConfigRegistry.contains(sceneConfigId);
    }

    @Override
    public Boolean containsUnitConfig(final UnitConfig unitConfig) throws CouldNotPerformException {
        return getUnitConfigRegistry(unitConfig.getType()).contains(unitConfig);
    }

    @Override
    public Future<UnitConfig> updateUnitConfig(final UnitConfig unitConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> getUnitConfigRegistry(unitConfig.getType()).update(unitConfig));
    }

    @Override
    public Future<UnitConfig> removeUnitConfig(final UnitConfig unitConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> getUnitConfigRegistry(unitConfig.getType()).remove(unitConfig));
    }

    @Override
    public List<UnitConfig> getUnitConfigList() throws CouldNotPerformException {
        ArrayList<UnitConfig> unitConfigList = new ArrayList<>();
        for (ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> unitConfigRegistry : unitConfigRegistryList) {
            unitConfigList.addAll(unitConfigRegistry.getMessages());
        }
        return unitConfigList;
    }

    @Override
    public Boolean isUnitConfigRegistryReadOnly() throws CouldNotPerformException {
        for (ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> unitConfigRegistry : unitConfigRegistryList) {
            if (unitConfigRegistry.isReadOnly()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean isUnitConfigRegistryConsistent() throws CouldNotPerformException {
        for (ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> unitConfigRegistry : unitConfigRegistryList) {
            if (unitConfigRegistry.isConsistent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param unitTemplateId {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public UnitTemplate getUnitTemplateById(final String unitTemplateId) throws CouldNotPerformException {
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
    public Boolean containsUnitTemplateById(final String unitTemplateId) throws CouldNotPerformException {
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
    public Boolean containsUnitTemplate(final UnitTemplate unitTemplate) throws CouldNotPerformException {
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
    public Future<UnitTemplate> updateUnitTemplate(final UnitTemplate unitTemplate) throws CouldNotPerformException {
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
        for (UnitTemplate unitTemplate : unitTemplateRegistry.getMessages()) {
            if (unitTemplate.getType() == type) {
                return unitTemplate;
            }
        }
        throw new NotAvailableException("UnitTemplate with type [" + type + "]");
    }

    /**
     * {@inheritDoc}
     *
     * @param unitConfigLabel {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public List<UnitConfig> getUnitConfigsByLabel(final String unitConfigLabel) throws CouldNotPerformException, NotAvailableException {
        List<UnitConfig> unitConfigs = Collections.synchronizedList(new ArrayList<>());
        getUnitConfigList().stream().filter((unitConfig) -> (unitConfig.getLabel().equalsIgnoreCase(unitConfigLabel))).forEach((unitConfig) -> {
            unitConfigs.add(unitConfig);
        });
        return unitConfigs;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<ServiceConfig> getServiceConfigs() throws CouldNotPerformException {
        List<ServiceConfig> serviceConfigs = new ArrayList<>();
        for (UnitConfig unitConfig : getUnitConfigList()) {
            serviceConfigs.addAll(unitConfig.getServiceConfigList());
        }
        return serviceConfigs;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Boolean isUnitGroupConfigRegistryReadOnly() throws CouldNotPerformException {
        return unitGroupUnitConfigRegistry.isReadOnly();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Boolean isUnitGroupConfigRegistryConsistent() throws CouldNotPerformException {
        return unitGroupUnitConfigRegistry.isConsistent();
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitConfigs(final UnitType type) throws CouldNotPerformException {
        List<UnitConfig> unitConfigs = new ArrayList<>();
        for (UnitConfig unitConfig : getUnitConfigList()) {
            if (type == UnitType.UNKNOWN || unitConfig.getType() == type || getSubUnitTypesOfUnitType(type).contains(unitConfig.getType())) {
                unitConfigs.add(unitConfig);
            }
        }
        return unitConfigs;
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceType
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<ServiceConfig> getServiceConfigs(final ServiceType serviceType) throws CouldNotPerformException {
        List<ServiceConfig> serviceConfigs = new ArrayList<>();
        for (UnitConfig unitConfig : getUnitConfigList()) {
            for (ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                if (serviceConfig.getServiceTemplate().getType() == serviceType) {
                    serviceConfigs.add(serviceConfig);
                }
            }
        }
        return serviceConfigs;
    }

    /**
     * {@inheritDoc}
     *
     * @param groupConfig
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Future<UnitConfig> registerUnitGroupConfig(final UnitConfig groupConfig) throws CouldNotPerformException {
        verifyUnitGroupUnitConfig(groupConfig);
        return GlobalExecutionService.submit(() -> unitGroupUnitConfigRegistry.register(groupConfig));
    }

    /**
     * {@inheritDoc}
     *
     * @param groupConfig
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Boolean containsUnitGroupConfig(final UnitConfig groupConfig) throws CouldNotPerformException {
        return unitGroupUnitConfigRegistry.contains(groupConfig);
    }

    /**
     * {@inheritDoc}
     *
     * @param groupConfigId
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Boolean containsUnitGroupConfigById(final String groupConfigId) throws CouldNotPerformException {
        return unitGroupUnitConfigRegistry.contains(groupConfigId);
    }

    /**
     * {@inheritDoc}
     *
     * @param groupConfig
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Future<UnitConfig> updateUnitGroupConfig(final UnitConfig groupConfig) throws CouldNotPerformException {
        verifyUnitGroupUnitConfig(groupConfig);
        return GlobalExecutionService.submit(() -> unitGroupUnitConfigRegistry.update(groupConfig));
    }

    /**
     * {@inheritDoc}
     *
     * @param groupConfig
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public Future<UnitConfig> removeUnitGroupConfig(final UnitConfig groupConfig) throws CouldNotPerformException {
        verifyUnitGroupUnitConfig(groupConfig);
        return GlobalExecutionService.submit(() -> unitGroupUnitConfigRegistry.remove(groupConfig));
    }

    /**
     * {@inheritDoc}
     *
     * @param groupConfigId
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public UnitConfig getUnitGroupConfigById(final String groupConfigId) throws CouldNotPerformException {
        return unitGroupUnitConfigRegistry.get(groupConfigId).getMessage();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitGroupConfigs() throws CouldNotPerformException {
        return new ArrayList<>(unitGroupUnitConfigRegistry.getMessages());
    }

    /**
     * {@inheritDoc}
     *
     * @param unitConfig
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitGroupConfigsByUnitConfig(final UnitConfig unitConfig) throws CouldNotPerformException {
        List<UnitConfig> unitConfigList = new ArrayList<>();
        for (UnitConfig unitGroupUnitConfig : unitGroupUnitConfigRegistry.getMessages()) {
            if (unitGroupUnitConfig.getUnitGroupConfig().getMemberIdList().contains(unitConfig.getId())) {
                unitConfigList.add(unitGroupUnitConfig);
            }
        }
        return unitConfigList;
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitGroupConfigsByUnitType(final UnitType type) throws CouldNotPerformException {
        List<UnitConfig> unitConfigList = new ArrayList<>();
        for (UnitConfig unitGroupUnitConfig : unitGroupUnitConfigRegistry.getMessages()) {
            if (unitGroupUnitConfig.getType() == type || getSubUnitTypesOfUnitType(type).contains(unitGroupUnitConfig.getType())) {
                unitConfigList.add(unitGroupUnitConfig);
            }
        }
        return unitConfigList;
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceTypes
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitGroupConfigsByServiceTypes(final List<ServiceType> serviceTypes) throws CouldNotPerformException {
        List<UnitConfig> unitGroups = new ArrayList<>();
        for (UnitConfig unitGroupUnitConfig : unitGroupUnitConfigRegistry.getMessages()) {
            boolean skipGroup = false;
            for (ServiceTemplate serviceTemplate : unitGroupUnitConfig.getUnitGroupConfig().getServiceTemplateList()) {
                if (!serviceTypes.contains(serviceTemplate.getType())) {
                    skipGroup = true;
                }
            }
            if (skipGroup) {
                continue;
            }
            unitGroups.add(unitGroupUnitConfig);
        }
        return unitGroups;
    }

    /**
     * {@inheritDoc}
     *
     * @param unitGroupUnitConfig
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitConfigsByUnitGroupConfig(final UnitConfig unitGroupUnitConfig) throws CouldNotPerformException {
        verifyUnitGroupUnitConfig(unitGroupUnitConfig);
        List<UnitConfig> unitConfigs = new ArrayList<>();
        for (String unitId : unitGroupUnitConfig.getUnitGroupConfig().getMemberIdList()) {
            unitConfigs.add(getUnitConfigById(unitId));
        }
        return unitConfigs;
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     * @param serviceTypes
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitConfig> getUnitConfigsByUnitTypeAndServiceTypes(final UnitType type, final List<ServiceType> serviceTypes) throws CouldNotPerformException {

        List<UnitConfig> unitConfigs = getUnitConfigs(type);

        boolean foundServiceType;

        for (UnitConfig unitConfig : new ArrayList<>(unitConfigs)) {
            foundServiceType = false;
            for (ServiceType serviceType : serviceTypes) {
                for (ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                    if (serviceConfig.getServiceTemplate().getType() == serviceType) {
                        foundServiceType = true;
                    }
                }
                if (!foundServiceType) {
                    unitConfigs.remove(unitConfig);
                }
            }
        }
        return unitConfigs;
    }

    /**
     * {@inheritDoc}
     *
     * @param scope
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public UnitConfig getUnitConfigByScope(final ScopeType.Scope scope) throws CouldNotPerformException {
        for (UnitConfig unitConfig : getUnitConfigList()) {
            if (unitConfig.getScope().equals(scope)) {
                return unitConfig;
            }
        }
        throw new NotAvailableException("No unit config available for given scope!");
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     * @return
     * @throws CouldNotPerformException
     */
    @Override
    public List<UnitType> getSubUnitTypesOfUnitType(final UnitType type) throws CouldNotPerformException {
        List<UnitType> unitTypes = new ArrayList<>();
        for (UnitTemplate template : unitTemplateRegistry.getMessages()) {
            if (template.getIncludedTypeList().contains(type)) {
                unitTypes.add(template.getType());
            }
        }
        return unitTypes;
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

    // TODO: implement and interface the is consistent and is readonly flags for all internal registries.
    public ProtoBufFileSynchronizedRegistry<String, UnitTemplate, UnitTemplate.Builder, UnitRegistryData.Builder> getUnitTemplateRegistry() {
        return unitTemplateRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getDalUnitConfigRegistry() {
        return dalUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getUserUnitConfigRegistry() {
        return userUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getAuthorizationGroupUnitConfigRegistry() {
        return authorizationGroupUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getDeviceUnitConfigRegistry() {
        return deviceUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getUnitGroupUnitConfigRegistry() {
        return unitGroupUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getLocationUnitConfigRegistry() {
        return locationUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getConnectionUnitConfigRegistry() {
        return connectionUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getAgentUnitConfigRegistry() {
        return agentUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getSceneUnitConfigRegistry() {
        return sceneUnitConfigRegistry;
    }

    public ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> getAppUnitConfigRegistry() {
        return appUnitConfigRegistry;
    }

    public ArrayList<ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder>> getUnitConfigRegistryList() {
        return unitConfigRegistryList;
    }
}
