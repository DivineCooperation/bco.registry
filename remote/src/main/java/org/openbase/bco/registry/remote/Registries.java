package org.openbase.bco.registry.remote;

/*
 * #%L
 * BCO Registry Remote
 * %%
 * Copyright (C) 2014 - 2019 openbase.org
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

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import org.openbase.bco.registry.activity.remote.ActivityRegistryRemote;
import org.openbase.bco.registry.activity.remote.CachedActivityRegistryRemote;
import org.openbase.bco.registry.clazz.remote.CachedClassRegistryRemote;
import org.openbase.bco.registry.clazz.remote.ClassRegistryRemote;
import org.openbase.bco.registry.template.remote.CachedTemplateRegistryRemote;
import org.openbase.bco.registry.template.remote.TemplateRegistryRemote;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jul.annotation.Experimental;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.rsb.com.AbstractRemoteClient;
import org.openbase.jul.storage.registry.RegistryRemote;
import org.openbase.type.domotic.activity.ActivityConfigType.ActivityConfig;
import org.openbase.type.domotic.activity.ActivityTemplateType.ActivityTemplate;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate;
import org.openbase.type.domotic.unit.agent.AgentClassType.AgentClass;
import org.openbase.type.domotic.unit.app.AppClassType.AppClass;
import org.openbase.type.domotic.unit.device.DeviceClassType.DeviceClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class Registries {

    /**
     * An array of all types of messages in registries.
     * Can be used to iterate over all registries.
     */
    private static final Message[] MESSAGE_TYPES = {
            UnitConfig.getDefaultInstance(),
            DeviceClass.getDefaultInstance(),
            AppClass.getDefaultInstance(),
            AgentClass.getDefaultInstance(),
            UnitTemplate.getDefaultInstance(),
            ServiceTemplate.getDefaultInstance(),
            ActivityTemplate.getDefaultInstance(),
            ActivityConfig.getDefaultInstance()
    };

    /**
     * Returns a list of all available bco registries.
     *
     * @param waitForData
     *
     * @return a list of remote registry instances.
     *
     * @throws CouldNotPerformException is throw if at least one registry is not available.
     * @throws InterruptedException     is thrown if thread is externally interrupted.
     */
    public static List<RegistryRemote> getRegistries(final boolean waitForData) throws CouldNotPerformException, InterruptedException {
        final List<RegistryRemote> registryList = new ArrayList<>();
        registryList.add(getTemplateRegistry(waitForData));
        registryList.add(getClassRegistry(waitForData));
        registryList.add(getActivityRegistry(waitForData));
        registryList.add(getUnitRegistry(waitForData));
        return registryList;
    }

    /**
     * Returns a list of all available bco registries without waiting for data.
     *
     * @return a list of all available bco registries
     *
     * @throws CouldNotPerformException is throw if at least one registry is not available.
     */
    public static List<RegistryRemote> getRegistries() throws CouldNotPerformException {
        try {
            return getRegistries(false);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CouldNotPerformException("Interrupted while creating registry list", ex);
        }
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     */
    public static UnitRegistryRemote getUnitRegistry() throws NotAvailableException {
        return CachedUnitRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     */
    public static ActivityRegistryRemote getActivityRegistry() throws NotAvailableException {
        return CachedActivityRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     */
    public static ClassRegistryRemote getClassRegistry() throws NotAvailableException {
        return CachedClassRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     */
    public static TemplateRegistryRemote getTemplateRegistry() throws NotAvailableException {
        return CachedTemplateRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @param waitForData defines if this call should block until the registry data is available.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     * @throws InterruptedException  is thrown if thread is externally interrupted.
     */
    public static UnitRegistryRemote getUnitRegistry(final boolean waitForData) throws CouldNotPerformException, InterruptedException {
        if (waitForData) {
            CachedUnitRegistryRemote.getRegistry().waitForData();
        }
        return CachedUnitRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @param waitForData defines if this call should block until the registry data is available.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     * @throws InterruptedException  is thrown if thread is externally interrupted.
     */
    public static ActivityRegistryRemote getActivityRegistry(final boolean waitForData) throws CouldNotPerformException, InterruptedException {
        if (waitForData) {
            CachedActivityRegistryRemote.getRegistry().waitForData();
        }
        return CachedActivityRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @param waitForData defines if this call should block until the registry data is available.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     * @throws InterruptedException  is thrown if thread is externally interrupted.
     */
    public static ClassRegistryRemote getClassRegistry(final boolean waitForData) throws CouldNotPerformException, InterruptedException {
        if (waitForData) {
            CachedClassRegistryRemote.getRegistry().waitForData();
        }
        return CachedClassRegistryRemote.getRegistry();
    }

    /**
     * Returns an initialized and activated remote registry.
     *
     * @param waitForData defines if this call should block until the registry data is available.
     *
     * @return the remote registry instance.
     *
     * @throws NotAvailableException
     * @throws InterruptedException  is thrown if thread is externally interrupted.
     */
    public static TemplateRegistryRemote getTemplateRegistry(final boolean waitForData) throws CouldNotPerformException, InterruptedException {
        if (waitForData) {
            CachedTemplateRegistryRemote.getRegistry().waitForData();
        }
        return CachedTemplateRegistryRemote.getRegistry();
    }

    /**
     * Method shutdown all registry instances.
     * <p>
     * Please use method with care!
     * Make sure no other instances are using the cached remote instances before shutdown.
     * <p>
     * Note: This method takes only effect in unit tests, otherwise this call is ignored. During normal operation there is not need for a manual registry shutdown because each registry takes care of its shutdown.
     */
    public static void shutdown() {
        CachedUnitRegistryRemote.shutdown();
        CachedActivityRegistryRemote.shutdown();
        CachedClassRegistryRemote.shutdown();
        CachedTemplateRegistryRemote.shutdown();
    }

    /**
     * Method only returns if all available registries are synchronized.
     *
     * @throws CouldNotPerformException
     * @throws InterruptedException     is thrown if thread is externally interrupted.
     */
    public static void waitForData() throws CouldNotPerformException, InterruptedException {
        CachedTemplateRegistryRemote.waitForData();
        CachedClassRegistryRemote.waitForData();
        CachedActivityRegistryRemote.waitForData();
        CachedUnitRegistryRemote.waitForData();
    }

    /**
     * Method only returns if all available registries are synchronized or the timeout is reached.
     *
     * @throws InterruptedException     is thrown in case the thread was externally interrupted.
     * @throws CouldNotPerformException is thrown if the wait could not be performed.
     */
    public static void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        CachedTemplateRegistryRemote.waitForData(timeout, timeUnit);
        CachedClassRegistryRemote.waitForData(timeout, timeUnit);
        CachedActivityRegistryRemote.waitForData(timeout, timeUnit);
        CachedUnitRegistryRemote.waitForData(timeout, timeUnit);
    }

    public static boolean isDataAvailable() {
        try {
            return CachedUnitRegistryRemote.getRegistry().isDataAvailable()
                    && CachedTemplateRegistryRemote.getRegistry().isDataAvailable()
                    && CachedClassRegistryRemote.getRegistry().isDataAvailable()
                    && CachedActivityRegistryRemote.getRegistry().isDataAvailable();
        } catch (NotAvailableException ex) {
            // at least one remote is not available.
        }
        return false;
    }

    /**
     * Method forces a resynchronization on all remote registries.
     *
     * @throws CouldNotPerformException
     * @throws InterruptedException
     */
    public static void reinitialize() throws CouldNotPerformException, InterruptedException {
        CachedTemplateRegistryRemote.reinitialize();
        CachedClassRegistryRemote.reinitialize();
        CachedActivityRegistryRemote.reinitialize();
        CachedUnitRegistryRemote.reinitialize();
    }

    /**
     * Method blocks until all registries are not handling any tasks and are all consistent.
     * <p>
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller. So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @throws InterruptedException                                is thrown in case the thread was externally interrupted.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if the wait could not be performed.
     */
    public static void waitUntilReady() throws InterruptedException, CouldNotPerformException {
        CachedTemplateRegistryRemote.waitUntilReady();
        CachedClassRegistryRemote.waitUntilReady();
        CachedActivityRegistryRemote.waitUntilReady();
        CachedUnitRegistryRemote.waitUntilReady();
    }

    /**
     * Generic method to register a message in a registry.
     * The given message will be used to select the correct registry.
     *
     * @param message the message that will be registered
     * @param <M>     the type of message registered
     *
     * @return the registered message
     *
     * @throws CouldNotPerformException if registration fails or messages of this type cannot be registered
     */
    @Experimental
    public static <M extends Message> Future<M> register(final M message) throws CouldNotPerformException {
        return (Future<M>) invokeMethod("register", message);
    }

    /**
     * Generic method to update a message in a registry.
     * The given message will be used to select the correct registry.
     *
     * @param message the message that will be updated
     * @param <M>     the type of message updates
     *
     * @return the updated message
     *
     * @throws CouldNotPerformException if updating fails or messages of this type cannot be updated
     */
    @Experimental
    public static <M extends Message> Future<M> update(final M message) throws CouldNotPerformException {
        return (Future<M>) invokeMethod("update", message);
    }

    /**
     * Generic method to remove a message in a registry.
     * The given message will be used to select the correct registry.
     *
     * @param message the message that will be removed
     * @param <M>     the type of message removed
     *
     * @return the removed message
     *
     * @throws CouldNotPerformException if removing fails or messages of this type cannot be removed
     */
    @Experimental
    public static <M extends Message> Future<M> remove(final M message) throws CouldNotPerformException {
        return (Future<M>) invokeMethod("remove", message);
    }

    /**
     * Test if a given message is contained in a registry.
     * The given message will be used to select the correct registry.
     *
     * @param message the message that will be tested
     *
     * @return true if the message is contained in a registry, else false
     *
     * @throws CouldNotPerformException if it could not be checked for the message type or the check itself fails
     */
    @Experimental
    public static Boolean contains(final Message message) throws CouldNotPerformException {
        return (Boolean) invokeMethod("contains", message);
    }

    /**
     * Test if a given message is contained in a registry.
     *
     * @param id the id which is checked
     *
     * @return true if the message is contained in a registry, false if it is not contained in any and no exception is thrown
     *
     * @throws CouldNotPerformException if the check fails for at least one registry and the id is not contained in any
     */
    @Experimental
    public static Boolean containsById(final String id) throws CouldNotPerformException {
        ExceptionStack exceptionStack = null;
        for (final Message message : MESSAGE_TYPES) {
            try {
                if (containsById(id, message)) {
                    return true;
                }
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(Registries.class, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow(() -> "Could not check if a message with id[" + id + "] is contained", exceptionStack);

        return false;
    }

    /**
     * Test if a given message is contained in  a specific registry.
     *
     * @param id               the id that is checked
     * @param messageOrBuilder message or builder defining which registry is checked
     *
     * @return true if the message is contained
     *
     * @throws CouldNotPerformException if the check fails
     */
    @Experimental
    public static Boolean containsById(final String id, final MessageOrBuilder messageOrBuilder) throws CouldNotPerformException {
        return (Boolean) invokeMethod(getMethodName("contains", "ById", messageOrBuilder), messageOrBuilder, id);
    }

    /**
     * Get a message from a registry by id.
     *
     * @param id the id that is checked
     *
     * @return a message with the id
     *
     * @throws CouldNotPerformException if no message with the id could be found
     */
    @Experimental
    public static Message getById(final String id) throws CouldNotPerformException {
        ExceptionStack exceptionStack = null;
        for (final Message message : MESSAGE_TYPES) {
            try {
                return getById(id, message);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(Registries.class, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow(() -> "Could not check for id[" + id + "]", exceptionStack);

        throw new NotAvailableException("Could not find a message with id[" + id + "]");
    }

    /**
     * Get a message from a registry by id from a specific registry.
     *
     * @param id               the id that is checked
     * @param messageOrBuilder message or builder defining which registry is checked
     *
     * @return a message with the id
     *
     * @throws CouldNotPerformException if no message with the id could be found
     */
    @Experimental
    public static Message getById(final String id, final MessageOrBuilder messageOrBuilder) throws CouldNotPerformException {
        return (Message) invokeMethod(getMethodName("get", "ById", messageOrBuilder), messageOrBuilder, id);
    }

    /**
     * Get the list of all messages of a registry.
     *
     * @param messageOrBuilder type used to identify the registry whose list is returned
     * @param <M>              the type of message in the list
     *
     * @return a list of message in a registry
     *
     * @throws CouldNotPerformException if the list could not be retrieved for the type
     */
    @Experimental
    public static <M extends Message> List<M> getMessageList(MessageOrBuilder messageOrBuilder) throws CouldNotPerformException {
        String methodName;
        if (messageOrBuilder.getDescriptorForType().getName().endsWith("Class")) {
            methodName = getMethodName("get", "es", messageOrBuilder);
        } else {
            methodName = getMethodName("get", "s", messageOrBuilder);
        }

        return (List<M>) invokeMethod(methodName, messageOrBuilder);
    }

    /**
     * Test if a registry is read only.
     *
     * @param messageOrBuilder type to identify the registry checked.
     *
     * @return true if the registry is read only
     *
     * @throws CouldNotPerformException if the check fails
     */
    @Experimental
    public static Boolean isReadOnly(MessageOrBuilder messageOrBuilder) throws CouldNotPerformException {
        return (Boolean) invokeMethod(getMethodName("is", "RegistryReadOnly", messageOrBuilder), messageOrBuilder);
    }

    /**
     * Test if a registry is consistent.
     *
     * @param messageOrBuilder type to identify the registry checked.
     *
     * @return true if the registry is consistent
     *
     * @throws CouldNotPerformException if the check fails
     */
    @Experimental
    public static Boolean isConsistent(MessageOrBuilder messageOrBuilder) throws CouldNotPerformException {
        return (Boolean) invokeMethod(getMethodName("is", "RegistryConsistent", messageOrBuilder), messageOrBuilder);
    }

    private static Object invokeMethod(final String methodPrefix, final Message message) throws CouldNotPerformException {
        return invokeMethod(getMethodName(methodPrefix, "", message), message, message);
    }

    private static Object invokeMethod(final String methodName, final MessageOrBuilder messageOrBuilder, final Object... parameters) throws CouldNotPerformException {
        final Class[] classes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            classes[i] = parameters[i].getClass();
        }

        try {
            final AbstractRemoteClient remote = getRegistryRemoteByType(messageOrBuilder);
            Method method = remote.getClass().getMethod(methodName, classes);
            return method.invoke(remote, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Could not invoke method[" + methodName + "] for type[" + messageOrBuilder.getDescriptorForType().getName() + "]", ex);
        }
    }

    private static String getMethodName(final String prefix, final String suffix, final MessageOrBuilder messageOrBuilder) {
        return prefix + getMethodNameForType(messageOrBuilder) + suffix;
    }

    private static String getMethodNameForType(final MessageOrBuilder messageOrBuilder) {
        return messageOrBuilder.getDescriptorForType().getName();
    }

    private static AbstractRemoteClient getRegistryRemoteByType(final MessageOrBuilder messageOrBuilder) throws CouldNotPerformException {
        switch (messageOrBuilder.getDescriptorForType().getName()) {
            case "UnitConfig":
                return getUnitRegistry();
            case "DeviceClass":
            case "AgentClass":
            case "AppClass":
                return getClassRegistry();
            case "UnitTemplate":
            case "ServiceTemplate":
            case "ActivityTemplate":
                return getTemplateRegistry();
            case "ActivityConfig":
                return getActivityRegistry();
            default:
                throw new NotAvailableException("Registry remote for type [" + messageOrBuilder.getDescriptorForType().getName() + "]");

        }
    }

    /**
     * Method returns an unit registry remote. In case its not available an InstantiationException is build referring the given class.
     * <p>
     * Note: Method can be used in constructors where the registry needs to be passed to the super class and no exception handling is possible.
     *
     * @param clazz the class used as cause for the InstantiationException
     *
     * @return an unit registry remote instance.
     *
     * @throws InstantiationException is thrown if the registry is not available.
     */
    public static UnitRegistryRemote getUnitRegistry(final Class clazz) throws InstantiationException {
        try {
            return Registries.getUnitRegistry();
        } catch (NotAvailableException ex) {
            throw new InstantiationException(clazz, ex);
        }
    }
}
