package org.openbase.bco.registry.app.remote;

/*
 * #%L
 * REM AppRegistry Remote
 * %%
 * Copyright (C) 2014 - 2016 openbase.org
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.openbase.bco.registry.app.lib.AppRegistry;
import org.openbase.bco.registry.app.lib.jp.JPAppRegistryScope;
import org.openbase.bco.registry.lib.com.AbstractRegistryRemote;
import org.openbase.bco.registry.lib.com.SynchronizedRemoteRegistry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPReadOnly;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.rsb.com.RPCHelper;
import static org.openbase.jul.extension.rsb.com.RSBRemoteService.DATA_WAIT_TIMEOUT;
import org.openbase.jul.storage.registry.RegistryRemote;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.domotic.unit.app.AppClassType.AppClass;
import rst.domotic.registry.AppRegistryDataType.AppRegistryData;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>   
 */
public class AppRegistryRemote extends AbstractRegistryRemote<AppRegistryData> implements AppRegistry, RegistryRemote<AppRegistryData> {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitConfig.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(AppClass.getDefaultInstance()));
    }

    private final SynchronizedRemoteRegistry<String, UnitConfig, UnitConfig.Builder> appUnitConfigRemoteRegistry;
    private final SynchronizedRemoteRegistry<String, AppClass, AppClass.Builder> appClassRemoteRegistry;

    public AppRegistryRemote() throws InstantiationException, InterruptedException {
        super(JPAppRegistryScope.class, AppRegistryData.class);
        try {
            appUnitConfigRemoteRegistry = new SynchronizedRemoteRegistry<>(this, AppRegistryData.APP_UNIT_CONFIG_FIELD_NUMBER);
            appClassRemoteRegistry = new SynchronizedRemoteRegistry<>(this, AppRegistryData.APP_CLASS_FIELD_NUMBER);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    protected void registerRemoteRegistries() throws CouldNotPerformException {
        registerRemoteRegistry(appClassRemoteRegistry);
        registerRemoteRegistry(appUnitConfigRemoteRegistry);
    }

    @Override
    protected void notifyDataUpdate(final AppRegistryData data) throws CouldNotPerformException {
        appUnitConfigRemoteRegistry.notifyRegistryUpdate(data.getAppUnitConfigList());
        appClassRemoteRegistry.notifyRegistryUpdate(data.getAppClassList());
    }

    public SynchronizedRemoteRegistry<String, UnitConfig, UnitConfig.Builder> getAppConfigRemoteRegistry() {
        return appUnitConfigRemoteRegistry;
    }

    public SynchronizedRemoteRegistry<String, AppClass, AppClass.Builder> getAppClassRemoteRegistry() {
        return appClassRemoteRegistry;
    }

    @Override
    public Future<UnitConfig> registerAppConfig(final UnitConfig appUnitConfig) throws CouldNotPerformException {
        try {
            return RPCHelper.callRemoteMethod(appUnitConfig, this, UnitConfig.class);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register appUnitConfig!", ex);
        }
    }

    @Override
    public UnitConfig getAppConfigById(String appUnitConfigId) throws CouldNotPerformException, NotAvailableException, InterruptedException {
        validateData();
        return appUnitConfigRemoteRegistry.getMessage(appUnitConfigId);
    }

    @Override
    public Boolean containsAppConfig(final UnitConfig appUnitConfig) throws CouldNotPerformException, InterruptedException {
        validateData();
        return appUnitConfigRemoteRegistry.contains(appUnitConfig);
    }

    @Override
    public Boolean containsAppConfigById(final String appUnitConfigId) throws CouldNotPerformException, InterruptedException {
        validateData();
        return appUnitConfigRemoteRegistry.contains(appUnitConfigId);
    }

    @Override
    public Future<UnitConfig> updateAppConfig(final UnitConfig appUnitConfig) throws CouldNotPerformException {
        try {
            return RPCHelper.callRemoteMethod(appUnitConfig, this, UnitConfig.class);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update appUnitConfig!", ex);
        }
    }

    @Override
    public Future<UnitConfig> removeAppConfig(final UnitConfig appUnitConfig) throws CouldNotPerformException {
        try {
            return RPCHelper.callRemoteMethod(appUnitConfig, this, UnitConfig.class);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove appUnitConfig!", ex);
        }
    }

    @Override
    public List<UnitConfig> getAppConfigs() throws CouldNotPerformException, NotAvailableException, InterruptedException {
        validateData();
        List<UnitConfig> messages = appUnitConfigRemoteRegistry.getMessages();
        return messages;
    }

    @Override
    public Boolean isAppConfigRegistryReadOnly() throws CouldNotPerformException, InterruptedException {
        try {
            if (JPService.getProperty(JPReadOnly.class).getValue() || !isConnected()) {
                return true;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        validateData();
        return getData().getAppUnitConfigRegistryReadOnly();
    }

    @Override
    public List<UnitConfig> getAppConfigsByAppClass(AppClass appClass) throws CouldNotPerformException, InterruptedException {
        return getAppConfigsByAppClassId(appClass.getId());
    }

    @Override
    public List<UnitConfig> getAppConfigsByAppClassId(String appClassId) throws CouldNotPerformException, InterruptedException {
        if (!containsAppClassById(appClassId)) {
            throw new NotAvailableException("appClassId [" + appClassId + "]");
        }

        List<UnitConfig> appConfigs = new ArrayList<>();
        for (UnitConfig appConfig : getAppConfigs()) {
            if (appConfig.getAppConfig().getAppClassId().equals(appClassId)) {
                appConfigs.add(appConfig);
            }
        }
        return appConfigs;
    }

    @Override
    public Future<AppClass> registerAppClass(AppClass appClass) throws CouldNotPerformException {
        try {
            return RPCHelper.callRemoteMethod(appClass, this, AppClass.class);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register app class!", ex);
        }
    }

    @Override
    public Boolean containsAppClass(AppClass appClass) throws CouldNotPerformException, InterruptedException {
        validateData();
        return appClassRemoteRegistry.contains(appClass);
    }

    @Override
    public Boolean containsAppClassById(String appClassId) throws CouldNotPerformException, InterruptedException {
        validateData();
        return appClassRemoteRegistry.contains(appClassId);
    }

    @Override
    public Future<AppClass> updateAppClass(AppClass appClass) throws CouldNotPerformException {
        try {
            return RPCHelper.callRemoteMethod(appClass, this, AppClass.class);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update app class!", ex);
        }
    }

    @Override
    public Future<AppClass> removeAppClass(AppClass appClass) throws CouldNotPerformException {
        try {
            return RPCHelper.callRemoteMethod(appClass, this, AppClass.class);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove app class!", ex);
        }
    }

    @Override
    public AppClass getAppClassById(String appClassId) throws CouldNotPerformException, InterruptedException {
        validateData();
        return appClassRemoteRegistry.getMessage(appClassId);
    }

    @Override
    public List<AppClass> getAppClasses() throws CouldNotPerformException, InterruptedException {
        validateData();
        return appClassRemoteRegistry.getMessages();
    }

    @Override
    public Boolean isAppClassRegistryReadOnly() throws CouldNotPerformException, InterruptedException {
        try {
            if (JPService.getProperty(JPReadOnly.class).getValue() || !isConnected()) {
                return true;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        waitForData(DATA_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        return getData().getAppClassRegistryReadOnly();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean isAppClassRegistryConsistent() throws CouldNotPerformException {
        try {
            validateData();
            return getData().getAppClassRegistryConsistent();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not check consistency!", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public Boolean isAppConfigRegistryConsistent() throws CouldNotPerformException {
        try {
            validateData();
            return getData().getAppUnitConfigRegistryConsistent();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not check consistency!", ex);
        }
    }
}
