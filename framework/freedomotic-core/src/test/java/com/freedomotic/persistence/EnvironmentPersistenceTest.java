/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.persistence;

import com.freedomotic.api.API;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.settings.AppConfig;
import com.freedomotic.settings.Info;
import com.freedomotic.testutils.GuiceJUnitRunner;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
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
 * @author matteo
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class EnvironmentPersistenceTest {
    private static final Logger LOG = LoggerFactory.getLogger(DataUpgradeServiceImplTest.class.getName());

    @Inject
    API api;
    

    public EnvironmentPersistenceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
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
        LOG.info("LOADING DEFAULT ENVIRONMENT");
        api.environments().initFromDefaultFolder();
        Assert.assertNotEquals("Testing environment list population", api.environments().findAll().isEmpty());
        LOG.info("ENVIRONMENT HAS " + api.environments().findAll().size() + " elements");
        
        
    }
    
    
    /**
     * Test switching a environment
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testEnvironmentSwitch() throws Exception {
        // load default environment
        testEnvironmentLoad();
        // save it in another position
        String newFolderName = "test-" + UUID.randomUUID().toString();
        File newFolder = new File(Info.PATHS.PATH_ENVIRONMENTS_FOLDER + "/" + newFolderName);
        newFolder.mkdirs();
        api.environments().saveEnvironmentsToFolder(newFolder);
        for (EnvironmentLogic env : api.environments().findAll()){
            File envFile = new File(newFolder + "/" + env.getPojo().getUUID() + ".xenv");
            Assert.assertTrue("Checking existance of persisted file for environment " + env.getPojo().getUUID(), envFile.exists());
        }
        // reopen new environment
        api.environments().init(newFolder);
        for (EnvironmentLogic env : api.environments().findAll()){
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
