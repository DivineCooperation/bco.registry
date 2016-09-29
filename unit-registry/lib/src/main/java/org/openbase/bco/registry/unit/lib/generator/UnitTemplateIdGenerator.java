/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.registry.unit.lib.generator;

/*
 * #%L
 * BCO Registry Unit Library
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

import org.openbase.bco.registry.lib.generator.UUIDGenerator;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class UnitTemplateIdGenerator extends UUIDGenerator<UnitTemplate> {

    // TODO Consistency Handler needed to make sure only one unit template per enum value is registered.
    
    // Legency generation
    //    @Override
    //    public String generateId(final UnitTemplate message) throws CouldNotPerformException {
    //        String id;
    //        try {
    //            if (!message.hasType()) {
    //                throw new InvalidStateException("Field [UnitType] is missing!");
    //            }
    //            return generateId(message.getType());
    //        } catch (CouldNotPerformException ex) {
    //            throw new CouldNotPerformException("Could not generate id!", ex);
    //        }
    //    }
    //
    //    public String generateId(final UnitTemplate.UnitType type) {
    //        return StringProcessor.transformToIdString(type.name());
//    }
}
