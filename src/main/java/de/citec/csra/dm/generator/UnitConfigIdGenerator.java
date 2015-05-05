/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.dm.generator;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.rsb.scope.ScopeGenerator;
import de.citec.jul.rsb.util.IdGenerator;
import rst.homeautomation.unit.UnitConfigType;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public class UnitConfigIdGenerator implements IdGenerator<String, UnitConfigType.UnitConfig> {

    private static UnitConfigIdGenerator instance;

    private UnitConfigIdGenerator() {
    }

    public static synchronized UnitConfigIdGenerator getInstance() {
        if (instance == null) {
            instance = new UnitConfigIdGenerator();
        }
        return instance;
    }

    @Override
    public String generateId(UnitConfigType.UnitConfig unitConfig) throws CouldNotPerformException {
        try {
            if (!unitConfig.hasScope()) {
                throw new NotAvailableException("unitconfig.scope");
            }
            return ScopeGenerator.generateStringRep(unitConfig.getScope());
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate unti id!", ex);
        }
    }
}