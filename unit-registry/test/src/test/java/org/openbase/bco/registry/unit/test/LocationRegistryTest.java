package org.openbase.bco.registry.unit.test;

/*
 * #%L
 * BCO Registry Unit Test
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

import org.junit.*;
import org.openbase.bco.authentication.core.AuthenticatorController;
import org.openbase.bco.registry.activity.core.ActivityRegistryController;
import org.openbase.bco.registry.clazz.core.ClassRegistryController;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.template.core.TemplateRegistryController;
import org.openbase.bco.registry.unit.core.UnitRegistryController;
import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate;
import rst.domotic.unit.connection.ConnectionConfigType.ConnectionConfig;
import rst.domotic.unit.location.LocationConfigType.LocationConfig;
import rst.domotic.unit.location.LocationConfigType.LocationConfig.LocationType;
import rst.spatial.PlacementConfigType.PlacementConfig;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class LocationRegistryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationRegistryTest.class);

    private static UnitRegistryController unitRegistry;
    private static ClassRegistryController classRegistry;
    private static TemplateRegistryController templateRegistry;
    private static ActivityRegistryController activityRegistry;
    private static AuthenticatorController authenticatorController;

    public LocationRegistryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Throwable {
        try {
            JPService.setupJUnitTestMode();
            JPService.registerProperty(JPDebugMode.class, true);

            authenticatorController = new AuthenticatorController();
            authenticatorController.init();
            authenticatorController.activate();
            authenticatorController.waitForActivation();

            unitRegistry = new UnitRegistryController();
            classRegistry = new ClassRegistryController();
            templateRegistry = new TemplateRegistryController();
            activityRegistry = new ActivityRegistryController();

            Thread unitRegistryThread = new Thread(() -> {
                try {
                    LOGGER.info("Start unit registry");
                    unitRegistry.init();
                    unitRegistry.activate();
                } catch (CouldNotPerformException | InterruptedException ex) {
                    ExceptionPrinter.printHistory(ex, LOGGER);
                }
            });
            Thread classRegistryThread = new Thread(() -> {
                try {
                    LOGGER.info("Start class registry");
                    classRegistry.init();
                    classRegistry.activate();
                } catch (CouldNotPerformException | InterruptedException ex) {
                    ExceptionPrinter.printHistory(ex, LOGGER);
                }
            });
            Thread templateRegistryThread = new Thread(() -> {
                try {
                    LOGGER.info("Start template registry");
                    templateRegistry.init();
                    templateRegistry.activate();
                } catch (CouldNotPerformException | InterruptedException ex) {
                    ExceptionPrinter.printHistory(ex, LOGGER);
                }
            });
            Thread activityRegistryThread = new Thread(() -> {
                try {
                    LOGGER.info("Start activity registry");
                    activityRegistry.init();
                    activityRegistry.activate();
                } catch (CouldNotPerformException | InterruptedException ex) {
                    ExceptionPrinter.printHistory(ex, LOGGER);
                }
            });


            templateRegistryThread.start();
            classRegistryThread.start();
            activityRegistryThread.start();
            unitRegistryThread.start();

            templateRegistryThread.join();
            classRegistryThread.join();
            activityRegistryThread.join();
            unitRegistryThread.join();
        } catch (Throwable ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, LOGGER);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Throwable {
        try {
            if (unitRegistry != null) {
                unitRegistry.shutdown();
            }
            if (classRegistry != null) {
                classRegistry.shutdown();
            }
            if (templateRegistry != null) {
                templateRegistry.shutdown();
            }
            if (activityRegistry != null) {
                activityRegistry.shutdown();
            }
            if (authenticatorController != null) {
                authenticatorController.shutdown();
            }

            Registries.shutdown();
        } catch (Throwable ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, LOGGER);
        }
    }

    @Before
    public void setUp() throws CouldNotPerformException {
        unitRegistry.getConnectionUnitConfigRegistry().clear();
        unitRegistry.getLocationUnitConfigRegistry().clear();
    }

    @After
    public void tearDown() {
    }

    private static UnitConfig.Builder getLocationUnitBuilder() {
        return UnitConfig.newBuilder().setType(UnitTemplate.UnitType.LOCATION).setLocationConfig(LocationConfig.getDefaultInstance());
    }

    private static UnitConfig.Builder getLocationUnitBuilder(LocationConfig.LocationType locationType) {
        LocationConfig locationConfig = LocationConfig.getDefaultInstance().toBuilder().setType(locationType).build();
        return UnitConfig.newBuilder().setType(UnitTemplate.UnitType.LOCATION).setLocationConfig(locationConfig);
    }

    private static UnitConfig.Builder getConnectionUnitBuilder() {
        return UnitConfig.newBuilder().setType(UnitTemplate.UnitType.CONNECTION).setConnectionConfig(ConnectionConfig.getDefaultInstance());
    }

    private static UnitConfig.Builder getConnectionUnitBuilder(ConnectionConfig.ConnectionType connectionType) {
        ConnectionConfig connectionConfig = ConnectionConfig.getDefaultInstance().toBuilder().setType(connectionType).build();
        return UnitConfig.newBuilder().setType(UnitTemplate.UnitType.LOCATION).setConnectionConfig(connectionConfig);
    }

    /**
     * Test if a root location becomes a child after it is set as a child of
     * root locations.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testChildConsistency() throws Exception {
        System.out.println("TestChildConsistency");
        String label = "Test2Living";
        UnitConfig living = getLocationUnitBuilder(LocationType.ZONE).setLabel(label).build();
        UnitConfig registeredLiving = unitRegistry.registerUnitConfig(living).get();
        assertTrue("The new location isn't registered as a root location.", registeredLiving.getLocationConfig().getRoot());
        assertEquals("Label has not been set", label, registeredLiving.getLabel());

        String rootLocationConfigLabel = "Test3RootLocation";
        UnitConfig rootLocationConfig = getLocationUnitBuilder(LocationType.ZONE).setLabel(rootLocationConfigLabel).build();
        UnitConfig registeredRootLocationConfig = unitRegistry.registerUnitConfig(rootLocationConfig).get();
        UnitConfig.Builder registeredLivingBuilder = unitRegistry.getUnitConfigById(registeredLiving.getId()).toBuilder();
        registeredLivingBuilder.getPlacementConfigBuilder().setLocationId(registeredRootLocationConfig.getId());
        unitRegistry.updateUnitConfig(registeredLivingBuilder.build()).get();
        assertEquals("Parent was not updated!", registeredRootLocationConfig.getId(), registeredLivingBuilder.getPlacementConfig().getLocationId());

        UnitConfig home = getLocationUnitBuilder(LocationType.TILE).setLabel("Test2Home").build();
        UnitConfig registeredHome = unitRegistry.registerUnitConfig(home).get();
        registeredLivingBuilder = unitRegistry.getUnitConfigById(registeredRootLocationConfig.getId()).toBuilder();
        registeredLivingBuilder.getPlacementConfigBuilder().setLocationId(registeredHome.getId());
        assertEquals("Parent was not updated!", registeredHome.getId(), unitRegistry.updateUnitConfig(registeredLivingBuilder.build()).get().getPlacementConfig().getLocationId());
    }

    /**
     * Test if a root location becomes a child after it is set as a child of
     * root locations.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testParentIdUpdateConsistency() throws Exception {
        System.out.println("testParentIdUpdateConsistency");

        String rootLocationConfigLabel = "Test3RootLocation";
        UnitConfig rootLocationConfig = getLocationUnitBuilder(LocationType.ZONE).setLabel(rootLocationConfigLabel).build();
        UnitConfig registeredRootLocationConfig = unitRegistry.registerUnitConfig(rootLocationConfig).get();

        String childLocationConfigLabel = "Test3ChildLocation";
        UnitConfig.Builder childLocationConfigBuilder = getLocationUnitBuilder(LocationType.TILE);
        childLocationConfigBuilder.setLabel(childLocationConfigLabel);
        childLocationConfigBuilder.getPlacementConfigBuilder().setLocationId(registeredRootLocationConfig.getId());
        UnitConfig registeredChildLocationConfig = unitRegistry.registerUnitConfig(childLocationConfigBuilder.build()).get();

        String parentLabel = "Test3ParentLocation";
        UnitConfig.Builder parentLocationConfigBuilder = getLocationUnitBuilder(LocationType.ZONE).setLabel(parentLabel);
        UnitConfig registeredParentLocationConfig = unitRegistry.registerUnitConfig(parentLocationConfigBuilder.build()).get();

        assertEquals("The new location isn't registered as child of Location[" + rootLocationConfigLabel + "]!", registeredRootLocationConfig.getId(), registeredChildLocationConfig.getPlacementConfig().getLocationId());

        UnitConfig.Builder registeredChildLocationConfigBuilder = registeredChildLocationConfig.toBuilder();
        registeredChildLocationConfigBuilder.getPlacementConfigBuilder().setLocationId(registeredParentLocationConfig.getId());
        registeredChildLocationConfig = unitRegistry.updateUnitConfig(registeredChildLocationConfigBuilder.build()).get();

        assertEquals("The parent location of child was not updated as new placement location id after update.", registeredParentLocationConfig.getId(), registeredChildLocationConfig.getPlacementConfig().getLocationId());
        assertEquals("The parent location of child was not updated as new placement location id in global registry.", registeredParentLocationConfig.getId(), unitRegistry.getUnitConfigsByLabel(childLocationConfigLabel).get(0).getPlacementConfig().getLocationId());
    }

    /**
     * Test if a a loop in the location configuration is detected by the
     * consistency handler.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testLoopConsistency() throws Exception {
        System.out.println("TestLoopConsistency");

        String rootLabel = "Root";
        String firstChildLabel = "FirstChild";
        String SecondChildLabel = "SecondChild";
        UnitConfig root = getLocationUnitBuilder(LocationType.ZONE).setLabel(rootLabel).build();
        root = unitRegistry.registerUnitConfig(root).get();

        UnitConfig firstChild = getLocationUnitBuilder(LocationType.ZONE).setLabel(firstChildLabel).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build();
        unitRegistry.registerUnitConfig(firstChild);

        UnitConfig secondChild = getLocationUnitBuilder(LocationType.ZONE).setLabel(SecondChildLabel).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build();
        secondChild = unitRegistry.registerUnitConfig(secondChild).get();

        try {
            // register loop
            root = unitRegistry.getUnitConfigById(root.getId());
            UnitConfig.Builder rootBuilder = root.toBuilder();
            rootBuilder.getPlacementConfigBuilder().setLocationId(secondChild.getId());
            ExceptionPrinter.setBeQuit(Boolean.TRUE);
            unitRegistry.registerUnitConfig(rootBuilder.build()).get();
            Assert.fail("No exception when registering location with a loop [" + secondChild + "]");
        } catch (CouldNotPerformException | InterruptedException | ExecutionException ex) {
        } finally {
            ExceptionPrinter.setBeQuit(Boolean.FALSE);
        }
    }

    /**
     * Test if a location with two children with the same label can be
     * registered.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testChildWithSameLabelConsistency() throws Exception {
        System.out.println("TestChildWithSameLabelConsistency");

        String rootLabel = "RootWithChildrenWithSameLabel";
        String childLabel = "childWithSameLabel";
        UnitConfig root = getLocationUnitBuilder(LocationType.ZONE).setLabel(rootLabel).build();
        root = unitRegistry.registerUnitConfig(root).get();

        UnitConfig firstChild = getLocationUnitBuilder(LocationType.ZONE).setLabel(childLabel).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build();
        unitRegistry.registerUnitConfig(firstChild).get();

        try {
            UnitConfig secondChild = getLocationUnitBuilder(LocationType.ZONE).setLabel(childLabel).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build();
            ExceptionPrinter.setBeQuit(Boolean.TRUE);
            unitRegistry.registerUnitConfig(secondChild).get();
            Assert.fail("No exception thrown when registering a second child with the same label");
        } catch (CouldNotPerformException | InterruptedException | ExecutionException ex) {
        } finally {
            ExceptionPrinter.setBeQuit(Boolean.FALSE);
        }
    }

    /**
     * Test connection scope, location and label consistency handler.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testConnectionLocationAndScopeAndLabelConsistency() throws Exception {
        System.out.println("TestConnectionLocationAndScopeAndLabelConsistency");

        String rootLabel = "RootZoneForConnectionTest";
        String zoneLabel = "SubZone";
        String tile1Label = "Tile1";
        String tile2Label = "Tile3";
        String tile3Label = "Tile2";
        UnitConfig root = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.ZONE).setLabel(rootLabel).build()).get();
        UnitConfig zone = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.ZONE).setLabel(zoneLabel).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build()).get();
        UnitConfig tile1 = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.TILE).setLabel(tile1Label).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build()).get();
        UnitConfig tile2 = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.TILE).setLabel(tile2Label).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(zone.getId())).build()).get();
        UnitConfig tile3 = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.TILE).setLabel(tile3Label).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(zone.getId())).build()).get();

        String connection1Label = "Connection1";
        String connection2Label = "Connection2";
        ConnectionConfig connectionConfig1 = ConnectionConfig.newBuilder().setType(ConnectionConfig.ConnectionType.DOOR).addTileId(tile1.getId()).addTileId(tile2.getId()).build();
        ConnectionConfig connectionConfig2 = ConnectionConfig.newBuilder().setType(ConnectionConfig.ConnectionType.WINDOW).addTileId(tile2.getId()).addTileId(tile3.getId()).build();
        UnitConfig connection1 = unitRegistry.registerUnitConfig(getConnectionUnitBuilder().setLabel(connection1Label).setConnectionConfig(connectionConfig1).build()).get();
        UnitConfig connection2 = unitRegistry.registerUnitConfig(getConnectionUnitBuilder().setLabel(connection2Label).setConnectionConfig(connectionConfig2).build()).get();

        assertEquals(root.getId(), connection1.getPlacementConfig().getLocationId());
        assertEquals(zone.getId(), connection2.getPlacementConfig().getLocationId());

        assertEquals("/rootzoneforconnectiontest/connection/connection1/", ScopeGenerator.generateStringRep(connection1.getScope()));
        assertEquals(ScopeGenerator.generateConnectionScope(connection2, zone), connection2.getScope());

        ConnectionConfig connectionConfig3 = ConnectionConfig.newBuilder().setType(ConnectionConfig.ConnectionType.PASSAGE).addTileId(tile2.getId()).addTileId(tile3.getId()).build();
        UnitConfig connection3 = getConnectionUnitBuilder().setConnectionConfig(connectionConfig3).build();
        try {
            ExceptionPrinter.setBeQuit(Boolean.TRUE);
            unitRegistry.registerUnitConfig(connection3).get();
            Assert.fail("No exception thrown when registering a second connection at the same location with the same label");
        } catch (Throwable ex) {
        } finally {
            ExceptionPrinter.setBeQuit(Boolean.FALSE);
        }
    }

    /**
     * Test connection tiles consistency handler.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testConnectionTilesConsistency() throws Exception {
        System.out.println("TestConnectionTilesConsistency");

        String rootLabel = "ConnectionTestRootZone";
        String noTileLabel = "NoTile";
        String tile1Label = "RealTile1";
        String tile2Label = "RealTile2";
        UnitConfig root = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.ZONE).setLabel(rootLabel).build()).get();
        UnitConfig tile1 = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.TILE).setLabel(tile1Label).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build()).get();
        UnitConfig tile2 = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.TILE).setLabel(tile2Label).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(root.getId())).build()).get();
        UnitConfig noTile = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationConfig.LocationType.REGION).setLabel(noTileLabel).setPlacementConfig(PlacementConfig.newBuilder().setLocationId(tile1.getId())).build()).get();

        System.out.println("Locations: ");
        for (UnitConfig location : unitRegistry.getLocationUnitConfigRegistry().getMessages()) {
            System.out.println(location.getLocationConfig().getType() + ", " + location.getLabel());
        }

        String connectionFailLabel = "ConnectionFail";
        String connectionLabel = "TilesTestConnection";
        ConnectionConfig connectionFail = ConnectionConfig.newBuilder().setType(ConnectionConfig.ConnectionType.DOOR).addTileId(tile2.getId()).build();
        UnitConfig connectionUnitFail = getConnectionUnitBuilder().setLabel(connectionFailLabel).setConnectionConfig(connectionFail).build();
        try {
            ExceptionPrinter.setBeQuit(Boolean.TRUE);
            unitRegistry.registerUnitConfig(connectionUnitFail).get();
            Assert.fail("Registered connection with less than one tile");
        } catch (ExecutionException ex) {
        } finally {
            ExceptionPrinter.setBeQuit(Boolean.FALSE);
        }

        ConnectionConfig.Builder connectionBuilder = ConnectionConfig.newBuilder().setType(ConnectionConfig.ConnectionType.WINDOW);
        connectionBuilder.addTileId(noTile.getId());
        connectionBuilder.addTileId(tile1.getId());
        connectionBuilder.addTileId(tile1.getId());
        connectionBuilder.addTileId(tile2.getId());
        connectionBuilder.addTileId(root.getId());
        connectionBuilder.addTileId("fakeLocationId");
        UnitConfig connectionUnit = unitRegistry.registerUnitConfig(getConnectionUnitBuilder().setLabel(connectionLabel).setConnectionConfig(connectionBuilder.build()).build()).get();
        ConnectionConfig connection = connectionUnit.getConnectionConfig();

        assertEquals("Doubled tiles or locations that aren't tiles or that do not exists do not have been removed", 2, connection.getTileIdCount());
        assertTrue("The tile list does not contain the expected tile", connection.getTileIdList().contains(tile1.getId()));
        assertTrue("The tile list does not contain the expected tile", connection.getTileIdList().contains(tile2.getId()));
    }

    /**
     * Test the locationTypeConsistencyHandler.
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testLocationTypeConsistency() throws Exception {
        System.out.println("testLocationTypeConsistency");

        String rootLabel = "RootZone";
        String tilelabel = "tile";
        String regionlabel = "region";
        try {
            UnitConfig rootLocation = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationType.ZONE).setLabel(rootLabel).build()).get();
            PlacementConfig tilePlacement = PlacementConfig.newBuilder().setLocationId(rootLocation.getId()).build();
            UnitConfig tile = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationType.TILE).setLabel(tilelabel).setPlacementConfig(tilePlacement).build()).get();

            UnitConfig.Builder rootWithoutType = unitRegistry.getUnitConfigById(rootLocation.getId()).toBuilder();
            LocationConfig.Builder rootWithoutTypeConfig = rootWithoutType.getLocationConfigBuilder();
            rootWithoutTypeConfig.clearType();
            rootLocation = unitRegistry.updateUnitConfig(rootWithoutType.build()).get();
            assertEquals("Type has not been reset even though the root location has a child which is a tile!", LocationType.ZONE, rootLocation.getLocationConfig().getType());

            PlacementConfig regionPlacement = PlacementConfig.newBuilder().setLocationId(tile.getId()).build();
            UnitConfig region = unitRegistry.registerUnitConfig(getLocationUnitBuilder().setLabel(regionlabel).setPlacementConfig(regionPlacement).build()).get();
            assertEquals("Type has not been detected for region!", LocationType.REGION, region.getLocationConfig().getType());

            LocationConfig wrongType = tile.getLocationConfig().toBuilder().setType(LocationType.ZONE).build();
            tile = tile.toBuilder().setLocationConfig(wrongType).build();
            assertEquals("LocationType is not correct!", LocationType.ZONE, tile.getLocationConfig().getType());
            tile = unitRegistry.updateUnitConfig(tile).get();
            assertEquals("Type has not been corrected for tile!", LocationType.TILE, tile.getLocationConfig().getType());
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, LOGGER);
        }
    }

    @Test(timeout = 5000)
    public void testGetLocationUnitConfigByScope() throws Exception {
        System.out.println("testGetLocationUnitConfigByScope");

        String label = "RootLocation";
        try {
            UnitConfig rootLocation = unitRegistry.registerUnitConfig(getLocationUnitBuilder(LocationType.ZONE).setLabel(label).build()).get();

            assertEquals("Could not resolve locationUnitConfig by its scope!", rootLocation, unitRegistry.getUnitConfigByScope(rootLocation.getScope()));
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, LOGGER);
        }
    }
}
