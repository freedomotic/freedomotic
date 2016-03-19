/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.persistence;

import com.freedomotic.api.API;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.settings.Info;
import com.freedomotic.testutils.GuiceJUnitRunner;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class EnvironmentPersistenceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataUpgradeServiceImplTest.class.getName());

    @Inject
    API api;

    static boolean hasDataDir = false;
    static boolean canPerformTest = false;

    public EnvironmentPersistenceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        // does data directory exist?
        if (Info.PATHS.PATH_DATA_FOLDER.exists()) {
            // yes: take a note not to delete it
            hasDataDir = true;
        } else {
            // no: copy data from data-example
            try {
                File dataExample = new File(Info.PATHS.PATH_WORKDIR.getParentFile().getParentFile() + "/data-example");
                LOG.info(dataExample.getAbsolutePath());
                copy(dataExample.getAbsolutePath(), Info.PATHS.PATH_DATA_FOLDER.getAbsolutePath());
                canPerformTest = true;
            } catch (IOException e) {
                canPerformTest = false;
            }
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // did data directory exist?
        // yes: skip deletion
        // no: delete it
        if (!hasDataDir) {
            // recursively delete newly created folder
            Path recDeleteMe = Paths.get(Info.PATHS.PATH_DATA_FOLDER.toURI());
            try {
                Files.walkFileTree(recDeleteMe, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                });
            } catch (IOException ex) {
                LOG.error("UNABLE TO FULLY DELETE TEST DATA DIRECTORY");
            }
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test loading a environment
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testEnvironmentLoad() throws Exception {
        if (canPerformTest) {
            LOG.info("LOADING DEFAULT ENVIRONMENT");
            api.environments().initFromDefaultFolder();
            Assert.assertNotEquals("Testing environment list population", api.environments().findAll().isEmpty());
            LOG.info("ENVIRONMENT HAS " + api.environments().findAll().size() + " elements");
        }
    }

    /**
     * Test switching a environment
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testEnvironmentSwitch() throws Exception {
        if (canPerformTest) {
            // load default environment
            testEnvironmentLoad();
            // save it in another position
            String newFolderName = "test-" + UUID.randomUUID().toString();
            File newFolder = new File(Info.PATHS.PATH_ENVIRONMENTS_FOLDER + "/" + newFolderName);
            newFolder.mkdirs();
            api.environments().saveEnvironmentsToFolder(newFolder);
            for (EnvironmentLogic env : api.environments().findAll()) {
                File envFile = new File(newFolder + "/" + env.getPojo().getUUID() + ".xenv");
                Assert.assertTrue("Checking existance of persisted file for environment " + env.getPojo().getUUID(), envFile.exists());
            }
            // reopen new environment
            api.environments().init(newFolder);
            for (EnvironmentLogic env : api.environments().findAll()) {
                File envFile = new File(newFolder + "/" + env.getPojo().getUUID() + ".xenv");
                Assert.assertTrue("Checking correspondance between file name and environment source location -  " + env.getPojo().getUUID(),
                        envFile.getAbsolutePath().equalsIgnoreCase(env.getSource().getAbsolutePath()));
                envFile.delete();
            }

            // recursively delete newly created folder
            Path recDeleteMe = Paths.get(newFolder.toURI());
            Files.walkFileTree(recDeleteMe, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });

            // reopen old environment
            testEnvironmentLoad();
        }
    }

    public static void copy(String sourceDir, String targetDir) throws IOException {

        abstract class MyFileVisitor implements FileVisitor<Path> {

            boolean isFirst = true;
            Path ptr;
        }

        MyFileVisitor copyVisitor = new MyFileVisitor() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Move ptr forward
                if (!isFirst) {
                    // .. but not for the first time since ptr is already in there
                    Path target = ptr.resolve(dir.getName(dir.getNameCount() - 1));
                    ptr = target;
                }
                Files.copy(dir, ptr, StandardCopyOption.COPY_ATTRIBUTES);
                isFirst = false;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path target = ptr.resolve(file.getFileName());
                Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                throw exc;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Path target = ptr.getParent();
                // Move ptr backwards
                ptr = target;
                return FileVisitResult.CONTINUE;
            }
        };

        copyVisitor.ptr = Paths.get(targetDir);
        Files.walkFileTree(Paths.get(sourceDir), copyVisitor);
    }
}
