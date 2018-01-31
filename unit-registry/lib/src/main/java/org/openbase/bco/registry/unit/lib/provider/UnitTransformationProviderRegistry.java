package org.openbase.bco.registry.unit.lib.provider;

import java.util.concurrent.Future;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.openbase.bco.registry.lib.provider.RootLocationConfigProvider;
import org.openbase.bco.registry.lib.provider.UnitConfigCollectionProvider;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.rct.GlobalTransformReceiver;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import rct.Transform;
import rct.TransformerException;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.geometry.AxisAlignedBoundingBox3DFloatType;
import rst.geometry.RotationType;
import rst.geometry.TranslationType;
import rst.spatial.ShapeType.Shape;


public interface UnitTransformationProviderRegistry<D> extends RootLocationConfigProvider, DataProvider<D>, UnitConfigCollectionProvider {

    public default UnitConfig getRootLocationConfig() throws CouldNotPerformException, NotAvailableException {
        for (UnitConfig locationConfig : getUnitConfigs(UnitType.LOCATION)) {
            if (locationConfig.getLocationConfig().hasRoot() && locationConfig.getLocationConfig().getRoot()) {
                return locationConfig;
            }
        }
        throw new NotAvailableException("rootlocation");
    }

    /**
     * Method returns the transformation which leads from the root location to the given unit.
     *
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     */
    public default Future<Transform> getRootToUnitTransformationFuture(final UnitConfig unitConfigTarget) {
        try {
            return getUnitTransformationFuture(getRootLocationConfig(), unitConfigTarget);
        } catch (CouldNotPerformException ex) {
            return FutureProcessor.canceledFuture(new NotAvailableException("UnitTransformation", ex));
        }
    }

    /**
     * Method returns the transformation which leads from the given unit to the root location.
     *
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     */
    public default Future<Transform> getUnitToRootTransformationFuture(final UnitConfig unitConfigTarget) {
        try {
            return getUnitTransformationFuture(unitConfigTarget, getRootLocationConfig());
        } catch (CouldNotPerformException ex) {
            return FutureProcessor.canceledFuture(new NotAvailableException("UnitTransformation", ex));
        }
    }

    /**
     * Method returns the transformation between the given unit A and the given unit B.
     *
     * @param unitConfigSource the unit used as transformation base.
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     * @deprecated please use getUnitTransformationFuture instead because this method will be return the Transform type directly in near future.
     */
    @Deprecated
    public default Future<Transform> getUnitTransformation(final UnitConfig unitConfigSource, final UnitConfig unitConfigTarget) {
        return getUnitTransformationFuture(unitConfigSource, unitConfigTarget);
    }

    /**
     * Method returns the transformation between the given unit A and the given unit B.
     *
     * @param unitConfigSource the unit used as transformation base.
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     */
    public default Future<Transform> getUnitTransformationFuture(final UnitConfig unitConfigSource, final UnitConfig unitConfigTarget) {

        if (unitConfigSource.getEnablingState().getValue() != State.ENABLED) {
            return FutureProcessor.canceledFuture(new InvalidStateException("Source Unit[" + unitConfigSource.getLabel() + ":" + unitConfigSource.getId() + "] is disbled and does not provide any transformation!"));
        }

        if (unitConfigTarget.getEnablingState().getValue() != State.ENABLED) {
            return FutureProcessor.canceledFuture(new InvalidStateException("Target Unit[" + unitConfigTarget.getLabel() + ":" + unitConfigTarget.getId() + "] is disbled and does not provide any transformation!"));
        }

        if (!unitConfigSource.hasPlacementConfig() || !unitConfigSource.getPlacementConfig().hasPosition()) {
            return FutureProcessor.canceledFuture(new InvalidStateException("Source Unit[" + unitConfigSource.getLabel() + ":" + unitConfigSource.getId() + "] does not provide any position!"));
        }

        if (!unitConfigTarget.hasPlacementConfig() || !unitConfigTarget.getPlacementConfig().hasPosition()) {
            return FutureProcessor.canceledFuture(new InvalidStateException("Target Unit[" + unitConfigTarget.getLabel() + ":" + unitConfigTarget.getId() + "] does not provide any position!"));
        }

        if (!unitConfigSource.getPlacementConfig().hasTransformationFrameId() || unitConfigSource.getPlacementConfig().getTransformationFrameId().isEmpty()) {
            return FutureProcessor.canceledFuture(new InvalidStateException("Source Unit[" + unitConfigSource.getLabel() + ":" + unitConfigSource.getId() + "] does not provide yet a transformation frame id!"));
        }

        if (!unitConfigTarget.getPlacementConfig().hasTransformationFrameId() || unitConfigTarget.getPlacementConfig().getTransformationFrameId().isEmpty()) {
            return FutureProcessor.canceledFuture(new InvalidStateException("Target Unit[" + unitConfigTarget.getLabel() + ":" + unitConfigTarget.getId() + "] does not provide yet a transformation frame id!"));
        }

        Future<Transform> transformationFuture = GlobalTransformReceiver.getInstance().requestTransform(
                unitConfigTarget.getPlacementConfig().getTransformationFrameId(),
                unitConfigSource.getPlacementConfig().getTransformationFrameId(),
                System.currentTimeMillis());
        return GlobalCachedExecutorService.allOfInclusiveResultFuture(transformationFuture, getDataFuture());
    }

    /**
     * Method returns the transformation from the root location to the given unit.
     *
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     * @throws org.openbase.jul.exception.NotAvailableException
     */
    public default Transform getRootToUnitTransformation(final UnitConfig unitConfigTarget) throws NotAvailableException {
        try {
            return lookupUnitTransformation(getRootLocationConfig(), unitConfigTarget);
        } catch (final CouldNotPerformException ex) {
            throw new NotAvailableException("UnitTransformation", ex);
        }
    }

    /**
     * Method returns the transformation from the root location to the given unit.
     *
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     * @throws org.openbase.jul.exception.NotAvailableException
     */
    public default Transform getUnitToRootTransformation(final UnitConfig unitConfigTarget) throws NotAvailableException {
        try {
            return lookupUnitTransformation(unitConfigTarget, getRootLocationConfig());
        } catch (final CouldNotPerformException ex) {
            throw new NotAvailableException("UnitTransformation", ex);
        }
    }

    // release todo: refactor into getUnitTransformation(...) which is currently blocked because it returns an future type in this api version.

    /**
     * Method returns the transformation between the given unit A and the given unit B.
     *
     * @param unitConfigSource the unit used as transformation base.
     * @param unitConfigTarget the unit where the transformation leads to.
     * @return a transformation future
     * @throws org.openbase.jul.exception.NotAvailableException
     * @deprecated do not use method because API changes will be applied in near future and this method will be renamed into {@code getUnitTransformation} within the next release.
     */
    @Deprecated
    public default Transform lookupUnitTransformation(final UnitConfig unitConfigSource, final UnitConfig unitConfigTarget) throws NotAvailableException {

        try {
            if (unitConfigSource.getEnablingState().getValue() != State.ENABLED) {
                throw new InvalidStateException("Source Unit[" + unitConfigSource.getLabel() + ":" + unitConfigSource.getId() + "] is disbled and does not provide any transformation!");
            }

            if (unitConfigTarget.getEnablingState().getValue() != State.ENABLED) {
                throw new InvalidStateException("Target Unit[" + unitConfigTarget.getLabel() + ":" + unitConfigTarget.getId() + "] is disbled and does not provide any transformation!");
            }

            if (!unitConfigSource.hasPlacementConfig() || !unitConfigSource.getPlacementConfig().hasPosition()) {
                throw new InvalidStateException("Source Unit[" + unitConfigSource.getLabel() + ":" + unitConfigSource.getId() + "] does not provide any position!");
            }

            if (!unitConfigTarget.hasPlacementConfig() || !unitConfigTarget.getPlacementConfig().hasPosition()) {
                throw new InvalidStateException("Target Unit[" + unitConfigTarget.getLabel() + ":" + unitConfigTarget.getId() + "] does not provide any position!");
            }

            if (!unitConfigSource.getPlacementConfig().hasTransformationFrameId() || unitConfigSource.getPlacementConfig().getTransformationFrameId().isEmpty()) {
                throw new InvalidStateException("Source Unit[" + unitConfigSource.getLabel() + ":" + unitConfigSource.getId() + "] does not provide yet a transformation frame id!");
            }

            if (!unitConfigTarget.getPlacementConfig().hasTransformationFrameId() || unitConfigTarget.getPlacementConfig().getTransformationFrameId().isEmpty()) {
                throw new InvalidStateException("Target Unit[" + unitConfigTarget.getLabel() + ":" + unitConfigTarget.getId() + "] does not provide yet a transformation frame id!");
            }

            return GlobalTransformReceiver.getInstance().lookupTransform(
                    unitConfigTarget.getPlacementConfig().getTransformationFrameId(),
                    unitConfigSource.getPlacementConfig().getTransformationFrameId(),
                    System.currentTimeMillis());
        } catch (final CouldNotPerformException | TransformerException ex) {
            throw new NotAvailableException("UnitTransformation", ex);
        }
    }

    /**
     * Gets the Transform3D of the transformation from root to unit coordinate system.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return transform relative to root location
     * @throws NotAvailableException is thrown if the transformation is not available.
     */
    default Transform3D getRootToUnitTransform3D(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            return getRootToUnitTransformation(unitConfig).getTransform();
        } catch (final CouldNotPerformException ex) {
            throw new NotAvailableException("Transform3D", ex);
        }
    }

    /**
     * Gets the inverse Transform3D to getTransform3D().
     * This is basically rotation and translation of the object in the root coordinate system
     * and thereby the inverse transformation to the one returned by getTransform3D().
     *
     * @param unitConfig the unit config to refer the unit.
     * @return transform relative to root location
     * @throws NotAvailableException is thrown if the transformation is not available.
     */
    default public Transform3D getUnitToRootTransform3D(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            return getUnitToRootTransformation(unitConfig).getTransform();
        } catch (NotAvailableException ex) {
            throw new NotAvailableException("Transform3Dinverse", ex);
        }
    }

    /**
     * Gets the position of the unit relative to the root location as a Point3d object.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return position relative to the root location
     * @throws NotAvailableException is thrown if the transformation is not available.
     */
    default public Point3d getUnitPositionGlobalPoint3d(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            final Transform3D transformation = getUnitToRootTransform3D(unitConfig);
            final Vector3d pos = new Vector3d();
            transformation.get(pos);
            return new Point3d(pos);
        } catch (NotAvailableException ex) {
            throw new NotAvailableException("GlobalPositionVector", ex);
        }
    }

    /**
     * Gets the position of the unit relative to the root location as a Translation object.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return position relative to the root location
     * @throws NotAvailableException is thrown if the transformation is not available.
     */
    default public TranslationType.Translation getUnitPositionGlobal(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            final Point3d pos = getUnitPositionGlobalPoint3d(unitConfig);
            return TranslationType.Translation.newBuilder().setX(pos.x).setY(pos.y).setZ(pos.z).build();
        } catch (NotAvailableException ex) {
            throw new NotAvailableException("GlobalPosition", ex);
        }
    }

    /**
     * Gets the rotation of the unit relative to the root location as a Quat4d object.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return rotation relative to the root location
     * @throws NotAvailableException is thrown if the transformation is not available.
     */
    default public Quat4d getUnitRotationGlobalQuat4d(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            final Transform3D transformation = getUnitToRootTransform3D(unitConfig);
            final Quat4d quat = new Quat4d();
            transformation.get(quat);
            return quat;
        } catch (final NotAvailableException ex) {
            throw new NotAvailableException("GlobalRotationQuat", ex);
        }
    }

    /**
     * Gets the rotation of the unit relative to the root location as a Rotation object.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return rotation relative to the root location
     * @throws NotAvailableException is thrown if the transformation is not available.
     */
    default public RotationType.Rotation getUnitRotationGlobal(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            final Quat4d quat = getUnitRotationGlobalQuat4d(unitConfig);
            return RotationType.Rotation.newBuilder().setQw(quat.w).setQx(quat.x).setQy(quat.y).setQz(quat.z).build();
        } catch (final NotAvailableException ex) {
            throw new NotAvailableException("GlobalRotation", ex);
        }
    }

    /**
     * Gets the center coordinates of the unit's BoundingBox in the unit coordinate system as a Point3d object.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return center coordinates of the unit's BoundingBox relative to unit
     * @throws NotAvailableException is thrown if the center can not be calculate.
     */
    default public Point3d getUnitBoundingBoxCenterPoint3d(final UnitConfig unitConfig) throws NotAvailableException {
        final AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat bb = getUnitShape(unitConfig).getBoundingBox();
        final TranslationType.Translation lfc = bb.getLeftFrontBottom();

        final Point3d center = new Point3d(bb.getWidth(), bb.getDepth(), bb.getHeight());
        center.scale(0.5);
        center.add(new Point3d(lfc.getX(), lfc.getY(), lfc.getZ()));
        return center;
    }

    /**
     * Gets the center coordinates of the unit's BoundingBox in the coordinate system of the root location as a Point3d object.
     *
     * @param unitConfig the unit config to refer the unit.
     * @return center coordinates of the unit's BoundingBox relative to root location
     * @throws NotAvailableException is thrown if the center can not be calculate.
     */
    default public Point3d getUnitBoundingBoxCenterGlobalPoint3d(final UnitConfig unitConfig) throws NotAvailableException {
        try {
            final Transform3D transformation = getUnitToRootTransform3D(unitConfig);
            final Point3d center = getUnitBoundingBoxCenterPoint3d(unitConfig);
            transformation.transform(center);
            return center;
        } catch (NotAvailableException ex) {
            throw new NotAvailableException("GlobalBoundingBoxCenter", ex);
        }
    }

    /**
     * Method returns the unit shape of the given unit referred by the id.
     * <p>
     * If this unit configuration does not provide any shape information the shape of the unit host will be returned.
     * In case the unit host even does not provide any shape information and the unit is a device than the shape of the device class will be used.
     *
     * @param unitId the id to resolve the unit shape.
     * @return the shape representing the unit.
     * @throws NotAvailableException is thrown if the unit shape is not available or the resolution has been failed.
     */
    default public Shape getUnitShape(final String unitId) throws NotAvailableException {
        try {
            return getUnitShape(getUnitConfigById(unitId));
        } catch (final CouldNotPerformException ex) {
            throw new NotAvailableException("Shape", "of unit " + unitId, ex);
        }
    }

    /**
     * Method returns the unit shape of the given unit configuration.
     * <p>
     * If this unit configuration does not provide any shape information the shape of the unit host will be returned.
     * In case the unit host even does not provide any shape information and the unit is a device than the shape of the device class will be used.
     *
     * @param unitConfig the unit configuration to resolve the unit shape.
     * @return the shape representing the unit.
     * @throws NotAvailableException is thrown if the unit shape is not available or the resolution has been failed.
     */
    Shape getUnitShape(final UnitConfig unitConfig) throws NotAvailableException;
}
