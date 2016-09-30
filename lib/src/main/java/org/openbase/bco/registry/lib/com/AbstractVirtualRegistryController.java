package org.openbase.bco.registry.lib.com;

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
import com.google.protobuf.GeneratedMessage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.ClosableDataBuilder;
import org.openbase.jul.extension.rsb.scope.jp.JPScope;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M> The virtual registry message type.
 * @param <MB> The virtual registry message builder.
 * @param <RM> The message type of the real registry which is mirrored by this virtual registry.
 */
public abstract class AbstractVirtualRegistryController<M extends GeneratedMessage, MB extends M.Builder<MB>, RM> extends AbstractRegistryController<M, MB> {

    private final VirtualRegistrySynchronizer virtualRegistrySynchronizer;

    public AbstractVirtualRegistryController(Class<? extends JPScope> jpScopePropery, MB builder) throws InstantiationException {
        super(jpScopePropery, builder);
        this.virtualRegistrySynchronizer = new VirtualRegistrySynchronizer();
    }

    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        getRegistryRemotes().forEach((registryRemote) -> {
            registryRemote.addDataObserver(virtualRegistrySynchronizer);
        });
        super.activate();
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        getRegistryRemotes().forEach((registryRemote) -> {
            registryRemote.removeDataObserver(virtualRegistrySynchronizer);
        });
        super.deactivate();
    }

    @Override
    protected void registerConsistencyHandler() throws CouldNotPerformException {
        // not needed for virtual registries.
    }

    @Override
    protected void registerDependencies() throws CouldNotPerformException {
        // not needed for virtual registries.
    }

    @Override
    protected void syncRegistryFlags() throws CouldNotPerformException, InterruptedException {
        // not needed for virtual registries.
    }

    @Override
    protected void registerRegistries() throws CouldNotPerformException {
        // not needed for virtual registries.
    }

    @Override
    protected void registerPlugins() throws CouldNotPerformException, InterruptedException {
        // not needed for virtual registries.
    }

    @Override
    protected Package getVersionConverterPackage() throws CouldNotPerformException {
        // not needed for virtual registries.
        return null;
    }

    protected abstract void syncVirtualRegistryFields(final MB virtualDataBuilder, final RM realData) throws CouldNotPerformException;

    class VirtualRegistrySynchronizer implements Observer<RM> {

        @Override
        public void update(Observable<RM> source, RM realData) throws Exception {
            try {
                try (ClosableDataBuilder<MB> dataBuilder = getDataBuilder(this)) {
                    syncVirtualRegistryFields(dataBuilder.getInternalBuilder(), realData);
                } catch (Exception ex) {
                    throw new CouldNotPerformException("Could not apply data change!", ex);
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not sync virtual registry!", ex, logger);
            }
        }
    }
}
