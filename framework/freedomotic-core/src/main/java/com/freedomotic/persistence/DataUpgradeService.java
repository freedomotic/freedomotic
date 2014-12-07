/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.persistence;

import com.freedomotic.exceptions.DataUpgradeException;

/**
 * Upgrades the data making them compatible with the current version
 *
 * @author enrico
 * @param <T> The type of data to upgrade
 */
public interface DataUpgradeService<T> {

    /**
     * Returns an upgraded object
     *
     * @param classType Pass a the dataObject class to check if a suitable
     * upgrader can be instantiated
     * @param dataObject The object to upgrade
     * @param fromVersion The source version of the data to upgrade
     * @return
     * @throws DataUpgradeException
     */
    T upgrade(Class classType, T dataObject, String fromVersion) throws DataUpgradeException;

}
