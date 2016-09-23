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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.openbase.bco.registry.app.core.consistency.LabelConsistencyHandler;
import org.openbase.bco.registry.app.core.consistency.ScopeConsistencyHandler;
import org.openbase.bco.registry.app.core.dbconvert.DummyConverter;
import org.openbase.bco.registry.app.lib.AppRegistry;
import org.openbase.bco.registry.app.lib.generator.AppClassIdGenerator;
import org.openbase.bco.registry.app.lib.generator.AppConfigIdGenerator;
import org.openbase.bco.registry.app.lib.jp.JPAppClassDatabaseDirectory;
import org.openbase.bco.registry.app.lib.jp.JPAppConfigDatabaseDirectory;
import org.openbase.bco.registry.app.lib.jp.JPAppRegistryScope;
import org.openbase.bco.registry.location.remote.LocationRegistryRemote;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.rsb.com.RPCHelper;
import org.openbase.jul.extension.rsb.com.RSBCommunicationService;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.iface.Manageable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalExecutionService;
import org.openbase.jul.storage.file.ProtoBufJSonFileProvider;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.homeautomation.control.app.AppClassType.AppClass;
import rst.homeautomation.control.app.AppConfigType.AppConfig;
import rst.homeautomation.control.app.AppRegistryDataType.AppRegistryData;
import rst.rsb.ScopeType;

/**
 *
 * @author mpohling
 */
public class AppRegistryController extends RSBCommunicationService<AppRegistryData, AppRegistryData.Builder> implements AppRegistry, Manageable<ScopeType.Scope> {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppClass.getDefaultInstance()));
    }

    private ProtoBufFileSynchronizedRegistry<String, AppClass, AppClass.Builder, AppRegistryData.Builder> appClassRegistry;
    private ProtoBufFileSynchronizedRegistry<String, AppConfig, AppConfig.Builder, AppRegistryData.Builder> appConfigRegistry;

    private final LocationRegistryRemote locationRegistryRemote;

    public AppRegistryController() throws InstantiationException, InterruptedException {
        super(AppRegistryData.newBuilder());
        try {
            ProtoBufJSonFileProvider protoBufJSonFileProvider = new ProtoBufJSonFileProvider();
            appClassRegistry = new ProtoBufFileSynchronizedRegistry<>(AppClass.class, getBuilderSetup(), getDataFieldDescriptor(AppRegistryData.APP_CLASS_FIELD_NUMBER), new AppClassIdGenerator(), JPService.getProperty(JPAppClassDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);
            appConfigRegistry = new ProtoBufFileSynchronizedRegistry<>(AppConfig.class, getBuilderSetup(), getDataFieldDescriptor(AppRegistryData.APP_CONFIG_FIELD_NUMBER), new AppConfigIdGenerator(), JPService.getProperty(JPAppConfigDatabaseDirectory.class).getValue(), protoBufJSonFileProvider);

            appClassRegistry.activateVersionControl(DummyConverter.class.getPackage());
            appConfigRegistry.activateVersionControl(DummyConverter.class.getPackage());

            locationRegistryRemote = new LocationRegistryRemote();

            appClassRegistry.loadRegistry();
            appConfigRegistry.loadRegistry();

            //TODO: should be activated but fails in the current db version since appClasses have just been introduced
            //appConfigRegistry.registerConsistencyHandler(new AppConfigAppClassIdConsistencyHandler(appClassRegistry));
            appConfigRegistry.registerConsistencyHandler(new ScopeConsistencyHandler(locationRegistryRemote));
            appConfigRegistry.registerConsistencyHandler(new LabelConsistencyHandler());
            appConfigRegistry.addObserver(new Observer<Map<String, IdentifiableMessage<String, AppConfig, AppConfig.Builder>>>() {

                @Override
                public void update(Observable<Map<String, IdentifiableMessage<String, AppConfig, AppConfig.Builder>>> source, Map<String, IdentifiableMessage<String, AppConfig, AppConfig.Builder>> data) throws Exception {
                    notifyChange();
                }
            });

            appClassRegistry.addObserver(new Observer<Map<String, IdentifiableMessage<String, AppClass, AppClass.Builder>>>() {

                @Override
                public void update(Observable<Map<String, IdentifiableMessage<String, AppClass, AppClass.Builder>>> source, Map<String, IdentifiableMessage<String, AppClass, AppClass.Builder>> data) throws Exception {
                    notifyChange();
                }
            });

        } catch (JPServiceException | CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public void init() throws InitializationException, InterruptedException {
        try {
            super.init(JPService.getProperty(JPAppRegistryScope.class).getValue());
            locationRegistryRemote.init();
        } catch (JPServiceException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        try {
            super.activate();
            locationRegistryRemote.activate();
            locationRegistryRemote.waitForData();

            appConfigRegistry.registerDependency(appClassRegistry);
            appConfigRegistry.registerDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not activate location registry!", ex);
        }

        try {
            appClassRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            logger.warn("Initial consistency check failed!");
            notifyChange();
        }

        try {
            appConfigRegistry.checkConsistency();
        } catch (CouldNotPerformException ex) {
            logger.warn("Initial consistency check failed!");
            notifyChange();
        }
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        appConfigRegistry.removeDependency(appClassRegistry);
        appConfigRegistry.removeDependency(locationRegistryRemote.getLocationConfigRemoteRegistry());
        locationRegistryRemote.deactivate();
        super.deactivate();
    }

    @Override
    public void shutdown() {
        if (appConfigRegistry != null) {
            appConfigRegistry.shutdown();
        }

        if (appClassRegistry != null) {
            appClassRegistry.shutdown();
        }

        try {
            deactivate();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory(ex, logger);
        }
        locationRegistryRemote.shutdown();
    }

    @Override
    public final void notifyChange() throws CouldNotPerformException, InterruptedException {
        // sync read only flags
        setDataField(AppRegistryData.APP_CONFIG_REGISTRY_READ_ONLY_FIELD_NUMBER, appConfigRegistry.isReadOnly());
        setDataField(AppRegistryData.APP_CLASS_REGISTRY_READ_ONLY_FIELD_NUMBER, appClassRegistry.isReadOnly());
        setDataField(AppRegistryData.APP_CONFIG_REGISTRY_CONSISTENT_FIELD_NUMBER, appConfigRegistry.isConsistent());
        setDataField(AppRegistryData.APP_CLASS_REGISTRY_CONSISTENT_FIELD_NUMBER, appClassRegistry.isConsistent());
        super.notifyChange();
    }

    @Override
    public void registerMethods(final RSBLocalServer server) throws CouldNotPerformException {
        RPCHelper.registerInterface(AppRegistry.class, this, server);
    }

    @Override
    public Future<AppConfig> registerAppConfig(AppConfig appConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> appConfigRegistry.register(appConfig));
    }

    @Override
    public AppConfig getAppConfigById(String appConfigId) throws CouldNotPerformException {
        return appConfigRegistry.get(appConfigId).getMessage();
    }

    @Override
    public Boolean containsAppConfigById(String appConfigId) throws CouldNotPerformException {
        return appConfigRegistry.contains(appConfigId);
    }

    @Override
    public Boolean containsAppConfig(AppConfig appConfig) throws CouldNotPerformException {
        return appConfigRegistry.contains(appConfig);
    }

    @Override
    public Future<AppConfig> updateAppConfig(AppConfig appConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> appConfigRegistry.update(appConfig));
    }

    @Override
    public Future<AppConfig> removeAppConfig(AppConfig appConfig) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> appConfigRegistry.remove(appConfig));
    }

    @Override
    public List<AppConfig> getAppConfigs() throws CouldNotPerformException {
        return appConfigRegistry.getMessages();
    }

    @Override
    public Boolean isAppConfigRegistryReadOnly() throws CouldNotPerformException {
        return appConfigRegistry.isReadOnly();
    }

    public ProtoBufFileSynchronizedRegistry<String, AppConfig, AppConfig.Builder, AppRegistryData.Builder> getAppConfigRegistry() {
        return appConfigRegistry;
    }

    @Override
    public List<AppConfig> getAppConfigsByAppClass(AppClass appClass) throws CouldNotPerformException, InterruptedException {
        return getAppConfigsByAppClassId(appClass.getId());
    }

    @Override
    public List<AppConfig> getAppConfigsByAppClassId(String appClassId) throws CouldNotPerformException, InterruptedException {
        if (!containsAppClassById(appClassId)) {
            throw new NotAvailableException("appClassId [" + appClassId + "]");
        }

        List<AppConfig> appConfigs = new ArrayList<>();
        for (AppConfig appConfig : getAppConfigs()) {
            if (appConfig.getAppClassId().equals(appClassId)) {
                appConfigs.add(appConfig);
            }
        }
        return appConfigs;
    }

    @Override
    public Future<AppClass> registerAppClass(AppClass appClass) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> appClassRegistry.register(appClass));
    }

    @Override
    public Boolean containsAppClass(AppClass appClass) throws CouldNotPerformException, InterruptedException {
        return appClassRegistry.contains(appClass);
    }

    @Override
    public Boolean containsAppClassById(String appClassId) throws CouldNotPerformException, InterruptedException {
        return appClassRegistry.contains(appClassId);
    }

    @Override
    public Future<AppClass> updateAppClass(AppClass appClass) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> appClassRegistry.update(appClass));
    }

    @Override
    public Future<AppClass> removeAppClass(AppClass appClass) throws CouldNotPerformException {
        return GlobalExecutionService.submit(() -> appClassRegistry.remove(appClass));
    }

    @Override
    public AppClass getAppClassById(String appClassId) throws CouldNotPerformException, InterruptedException {
        return appClassRegistry.getMessage(appClassId);
    }

    @Override
    public List<AppClass> getAppClasses() throws CouldNotPerformException, InterruptedException {
        return appClassRegistry.getMessages();
    }

    @Override
    public Boolean isAppClassRegistryReadOnly() throws CouldNotPerformException, InterruptedException {
        return appClassRegistry.isReadOnly();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean isAppClassRegistryConsistent() throws CouldNotPerformException {
        return appClassRegistry.isConsistent();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean isAppConfigRegistryConsistent() throws CouldNotPerformException {
        return appConfigRegistry.isConsistent();
    }
}
