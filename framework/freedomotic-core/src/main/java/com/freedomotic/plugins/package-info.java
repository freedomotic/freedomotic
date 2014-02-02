/*
 * This package contains three major components:
 * <ul>
 * <li>
 *   Storage: a class used as repository to collect plugins (Client) with
 *   methods to add/remove and check elements.
 * </li>
 * <li>
 *   Data Access Object (DAO): a pattern used to load from filesystem the different
 *   plugin classes.
 * </li>
 * <li>
 *   Loader: an helper class that hides the DAO pattern to the caller
 *   plugin classes.
 * </li>
 * </ul>
 */
package com.freedomotic.plugins;
