package org.openbase.bco.registry.unit.core.plugin;

/*
 * #%L
 * BCO Registry Unit Core
 * %%
 * Copyright (C) 2014 - 2018 openbase.org
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
import java.util.ConcurrentModificationException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.rct.transform.PoseTransformer;
import org.openbase.jul.storage.registry.ProtoBufFileSynchronizedRegistry;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import org.openbase.jul.storage.registry.plugin.ProtobufRegistryPluginAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rct.Transform;
import rct.TransformPublisher;
import rct.TransformType;
import rct.TransformerException;
import rct.TransformerFactory;
import org.openbase.type.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig.Builder;

@Deprecated
public class PublishDeviceTransformationRegistryPlugin extends ProtobufRegistryPluginAdapter<String, UnitConfig, Builder> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> locationRegistry;

    private TransformerFactory transformerFactory;
    private TransformPublisher transformPublisher;

    public PublishDeviceTransformationRegistryPlugin(final ProtoBufFileSynchronizedRegistry<String, UnitConfig, UnitConfig.Builder, UnitRegistryData.Builder> locationRegistry) throws org.openbase.jul.exception.InstantiationException {
        try {
            this.locationRegistry = locationRegistry;
            this.transformerFactory = TransformerFactory.getInstance();
        } catch (Exception ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    @Override
    public void init(final ProtoBufRegistry<String, UnitConfig, Builder> registry) throws InitializationException, InterruptedException {
        try {
            super.init(registry);
            this.transformPublisher = transformerFactory.createTransformPublisher(registry.getName());
            for (IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry : registry.getEntries()) {
                publishTransformation(entry);
            }
        } catch (CouldNotPerformException | TransformerFactory.TransformerFactoryException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void publishTransformation(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) {
        try {
            UnitConfig deviceConfig = entry.getMessage();

            if (!deviceConfig.hasId()) {
                throw new NotAvailableException("deviceconfig.id");
            }

            if (!deviceConfig.hasPlacementConfig()) {
                throw new NotAvailableException("deviceconfig.placement");
            }

            if (!deviceConfig.getPlacementConfig().hasPosition()) {
                throw new NotAvailableException("deviceconfig.placement.position");
            }

            if (!deviceConfig.getPlacementConfig().hasTransformationFrameId() || deviceConfig.getPlacementConfig().getTransformationFrameId().isEmpty()) {
                throw new NotAvailableException("deviceconfig.placement.transformationframeid");
            }

            if (!deviceConfig.getPlacementConfig().hasLocationId() || deviceConfig.getPlacementConfig().getLocationId().isEmpty()) {
                throw new NotAvailableException("deviceconfig.placement.locationid");
            }

            Transform transformation;

            // publish device transformation
            if (deviceConfig.getPlacementConfig().hasPosition()) {
                logger.info("Publish " + locationRegistry.getMessage(deviceConfig.getPlacementConfig().getLocationId()).getPlacementConfig().getTransformationFrameId() + " to " + deviceConfig.getPlacementConfig().getTransformationFrameId());
                transformation = PoseTransformer.transform(deviceConfig.getPlacementConfig().getPosition(), locationRegistry.getMessage(deviceConfig.getPlacementConfig().getLocationId()).getPlacementConfig().getTransformationFrameId(), deviceConfig.getPlacementConfig().getTransformationFrameId());

                transformation.setAuthority(getRegistry().getName());
                transformPublisher.sendTransform(transformation, TransformType.STATIC);
            }
        } catch (NotAvailableException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not publish transformation of " + entry + "!", ex), logger, LogLevel.DEBUG);
        } catch (CouldNotPerformException | TransformerException | ConcurrentModificationException | NullPointerException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not publish transformation of " + entry + "!", ex), logger, LogLevel.WARN);
        }
    }

    @Override
    public void afterRegister(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) {
        publishTransformation(entry);
    }

    @Override
    public void afterUpdate(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) throws CouldNotPerformException {
        publishTransformation(entry);
    }

    @Override
    public void shutdown() {
        if (transformPublisher != null) {
            try {
                transformPublisher.shutdown();
            } catch (Exception ex) {
                logger.warn("Could not shutdown transformation publisher");
            }
        }
    }
}
