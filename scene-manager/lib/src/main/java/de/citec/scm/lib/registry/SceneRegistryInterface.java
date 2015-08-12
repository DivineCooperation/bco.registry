/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.scm.lib.registry;

import de.citec.jul.exception.CouldNotPerformException;
import java.util.List;
import rst.homeautomation.control.scene.SceneConfigType.SceneConfig;

/**
 *
 * @author mpohling
 */
public interface SceneRegistryInterface {

    public SceneConfig registerSceneConfig(SceneConfig sceneConfig) throws CouldNotPerformException;

    public Boolean containsSceneConfig(SceneConfig sceneConfig) throws CouldNotPerformException;

    public Boolean containsSceneConfigById(String sceneConfigId) throws CouldNotPerformException;

    public SceneConfig updateSceneConfig(SceneConfig sceneConfig) throws CouldNotPerformException;

    public SceneConfig removeSceneConfig(SceneConfig sceneConfig) throws CouldNotPerformException;

    public SceneConfig getSceneConfigById(final String sceneConfigId) throws CouldNotPerformException;

    public List<SceneConfig> getSceneConfigs() throws CouldNotPerformException;

    public void shutdown();
}