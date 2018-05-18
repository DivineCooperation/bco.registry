package org.openbase.bco.registry.lib.provider.template;

/*-
 * #%L
 * BCO Registry Lib
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.annotations.RPCMethod;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.util.List;

public interface ServiceTemplateCollectionProvider {

    /**
     * Method returns true if the service template with the given id is
     * registered, otherwise false. The service template id field is used for the
     * comparison.
     *
     * @param serviceTemplate the service template which is tested
     * @return if the service template with the given id is registered, otherwise false
     * @throws CouldNotPerformException is thrown if the check fails.
     */
    @RPCMethod
    Boolean containsServiceTemplate(final ServiceTemplate serviceTemplate) throws CouldNotPerformException;

    /**
     * Method returns true if the service template with the given id is
     * registered, otherwise false.
     *
     * @param serviceTemplateId the id of the service template
     * @return if the service template with the given id is registered, otherwise false
     * @throws CouldNotPerformException is thrown if the check fails.
     */
    @RPCMethod
    Boolean containsServiceTemplateById(final String serviceTemplateId) throws CouldNotPerformException;

    /**
     * Method returns the service template which is registered with the given
     * id.
     *
     * @param serviceTemplateId the id of the service template
     * @return the requested service template.
     * @throws CouldNotPerformException is thrown if the request fails.
     */
    @RPCMethod
    ServiceTemplate getServiceTemplateById(final String serviceTemplateId) throws CouldNotPerformException;

    /**
     * Method returns all registered service template.
     *
     * @return the service templates stored in this registry.
     * @throws CouldNotPerformException is thrown if the request fails.
     */
    List<ServiceTemplate> getServiceTemplates() throws CouldNotPerformException;

    /**
     * Method returns the service template with the given type.
     *
     * @param serviceType the service type
     * @return the requested service template.
     * @throws CouldNotPerformException is thrown if the request fails.
     */
    @RPCMethod
    ServiceTemplate getServiceTemplateByType(final ServiceType serviceType) throws CouldNotPerformException;
}