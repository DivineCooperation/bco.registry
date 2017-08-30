package org.openbase.bco.registry.lib.authorization;

/*-
 * #%L
 * BCO Registry Lib
 * %%
 * Copyright (C) 2014 - 2017 openbase.org
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

import com.google.protobuf.ProtocolStringList;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.slf4j.LoggerFactory;
import rst.domotic.authentication.PermissionConfigType.PermissionConfig;
import rst.domotic.authentication.PermissionConfigType.PermissionConfig.MapFieldEntry;
import rst.domotic.authentication.PermissionType.Permission;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 * Helper class to determine the permissions for a given user on a given permission configuration.
 * All methods return the highest permission, i.e. if the permission is true for any level
 * applying to the user, it can't be revoked at any other level and true will be returned.
 *
 * @author <a href="mailto:cromankiewicz@techfak.uni-bielefeld.de">Constantin Romankiewicz</a>
 */
public class AuthorizationHelper {
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AuthorizationHelper.class);
    
    private enum Type {
        READ,
        WRITE,
        ACCESS
    }

    /**
     * Checks whether a user has the permission to read from a permissionConfig,
     * for example to query information about the unit's state who has this permissionConfig.
     *
     * @param unitConfig The unitConfig of the unit the user wants to read.
     * @param userId ID of the user whose permissions should be checked.
     * @param groups All available groups in the system, indexed by their group ID.
     * @return True if the user can read from the unit, false if not.
     * @throws InterruptedException
     * @throws CouldNotPerformException If the permissions could not be checked, probably because of invalid location information.
     */
    public static boolean canRead(UnitConfig unitConfig, String userId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> groups, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations) throws InterruptedException, CouldNotPerformException {
        return canDo(unitConfig, userId, groups, locations, Type.READ);
    }

    /**
     * Checks whether a user has the permission to write to a something with the given permissionConfig,
     * for example to run any action on a unit.
     *
     * @param unitConfig The unitConfig of the unit the user wants to write to.
     * @param userId ID of the user whose permissions should be checked.
     * @param groups All available groups in the system, indexed by their group ID.
     * @return True if the user can write to the unit, false if not.
     * @throws InterruptedException
     * @throws CouldNotPerformException If the permissions could not be checked, probably because of invalid location information.
     */
    public static boolean canWrite(UnitConfig unitConfig, String userId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> groups, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations) throws InterruptedException, CouldNotPerformException {
        return canDo(unitConfig, userId, groups, locations, Type.WRITE);
    }

    /**
     * Checks whether a user has the permission to access a unit with the given permissionConfig.
     *
     * @param unitConfig The unitConfig of the unit the user wants to access.
     * @param userId ID of the user whose permissions should be checked.
     * @param groups All available groups in the system, indexed by their group ID.
     * @return True if the user can access the unit, false if not.
     * @throws InterruptedException
     * @throws CouldNotPerformException If the permissions could not be checked, probably because of invalid location information.
     */
    public static boolean canAccess(UnitConfig unitConfig, String userId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> groups, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations) throws InterruptedException, CouldNotPerformException {
        return canDo(unitConfig, userId, groups, locations, Type.ACCESS);
    }

    /**
     * Checks all permissions for a user.
     *
     * @param unitConfig The unitConfig of the unit for which the permissions apply.
     * @param userId ID of the user whose permissions should be checked.
     * @param groups All available groups in the system, indexed by their group ID.
     * @return Permission object representing the maximum permissions for the given user on the given unit.
     * @throws InterruptedException
     * @throws CouldNotPerformException If the permissions could not be checked, probably because of invalid location information.
     */
    public static Permission getPermission(UnitConfig unitConfig, String userId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> groups, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations) throws InterruptedException, CouldNotPerformException {
        return Permission.newBuilder()
          .setAccess(canAccess(unitConfig, userId, groups, locations))
          .setRead(canRead(unitConfig, userId, groups, locations))
          .setWrite(canWrite(unitConfig, userId, groups, locations))
          .build();
    }

    private static boolean canDo(UnitConfig unitConfig, String userId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> groups, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations, Type type) throws InterruptedException, CouldNotPerformException {
        System.out.println(unitConfig);
        boolean isRoot = unitConfig.getType() == UnitType.LOCATION && unitConfig.getLocationConfig().hasRoot() && unitConfig.getLocationConfig().getRoot();

        if (!isRoot) {
            UnitConfig locationUnitConfig = getLocationUnitConfig(unitConfig.getPlacementConfig().getLocationId(), locations);

            if (!canRead(locationUnitConfig, userId, groups, locations)) {
                return false;
            }
        }

        return canDo(getPermissionConfig(unitConfig, locations), userId, groups, locations, type);
    }

    /**
     * Internal helper method to check one of the permissions on a unit.
     *
     * @param permissionConfig The unit for which the permissions apply.
     * @param userId ID of the user whose permissions should be checked.
     * @param groups All available groups in the system, indexed by their group ID.
     * @param type The permission type to check.
     * @return True if the user has the given permission, false if not.
     */
    private static boolean canDo(PermissionConfig permissionConfig, String userId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> groups, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations, Type type) {
        // Other
        if (permitted(permissionConfig.getOtherPermission(), type)) {
            return true;
        }

        // If no user was given, only "other" rights apply.
        if (userId == null) {
            return false;
        }

        // If the given ID has the form user@client, we check both.
        String[] split = userId.split("@", 2);

        if (split.length > 1) {
            return canDo(permissionConfig, split[0], groups, locations, type) || canDo(permissionConfig, split[1], groups, locations, type);
        }

        // Owner
        if (permissionConfig.getOwnerId().equals(userId) && permitted(permissionConfig.getOwnerPermission(), type)) {
            return true;
        }

        // Groups
        if (groups == null) {
            return false;
        }

        ProtocolStringList groupMembers;
        for (MapFieldEntry entry : permissionConfig.getGroupPermissionList()) {
            if(groups.get(entry.getGroupId()) == null) {
                LOGGER.warn("No Group for id["+entry.getGroupId()+"] available");
                continue;
            }
            groupMembers = groups.get(entry.getGroupId()).getMessage().getAuthorizationGroupConfig().getMemberIdList();

            // Check if the user belongs to the group.
            if (groupMembers.contains(userId) && permitted(entry.getPermission(), type)) {
                return true;
            }
        }

        return false;
    }

    private static boolean permitted(Permission permission, Type type) {
        switch(type) {
            case READ:
                return permission.getRead();
            case WRITE:
                return permission.getWrite();
            case ACCESS:
                return permission.getAccess();
            default:
                return false;
        }
    }

    private static PermissionConfig getPermissionConfig(UnitConfig unitConfig, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations) throws InterruptedException, CouldNotPerformException {
        // If the unit itself has a PermissionConfig, we use this one.
        if (unitConfig.hasPermissionConfig()) {
            return unitConfig.getPermissionConfig();
        }
        // If the unit has a parent location (i.e. is not the root location), we use the PermissionConfig of the parent(s).
        else if (unitConfig.getType() != UnitType.LOCATION || !unitConfig.getLocationConfig().hasRoot() || !unitConfig.getLocationConfig().getRoot()) {
            return getPermissionConfig(getLocationUnitConfig(unitConfig.getPlacementConfig().getLocationId(), locations), locations);
        }

        return PermissionConfig.getDefaultInstance();
    }

    private static UnitConfig getLocationUnitConfig(String locationId, Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> locations) throws NotAvailableException {
        if (locations.containsKey(locationId)) {
            return locations.get(locationId).getMessage();
        }

        new NotAvailableException("").printStackTrace();

        throw new NotAvailableException("Location [" + locationId + "]");
    }
}