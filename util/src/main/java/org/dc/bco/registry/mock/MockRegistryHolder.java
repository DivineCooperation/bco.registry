/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.registry.mock;

import org.dc.jul.exception.InstantiationException;
import org.dc.jul.schedule.SyncObject;

/**
 *
 * @author mpohling
 */
public class MockRegistryHolder {

    private static int mockRegistryCounter = 0;
    private final static SyncObject mockRegistrySync = new SyncObject("MockRegistrySync");
    private static MockRegistry mockRegistry;

    public static MockRegistry newMockRegistry() throws InstantiationException {
        synchronized (mockRegistrySync) {
            if (mockRegistry == null) {
                assert mockRegistryCounter == 0;
                mockRegistry = new MockRegistry();
            }
            mockRegistryCounter++;
            assert mockRegistry != null;
            return mockRegistry;
        }
    }

    public static void shutdownMockRegistry() {
        synchronized (mockRegistrySync) {
            if(mockRegistry == null) {
                assert mockRegistryCounter == 0;
                return;
            }
            mockRegistryCounter--;
            
            if(mockRegistryCounter == 0) {
                mockRegistry.shutdown();
                mockRegistry = null;
            }
        }
    }
}
