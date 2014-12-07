/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.upgrade;

import com.freedomotic.persistence.DataUpgradeService;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.testutils.GuiceJUnitRunner;
import com.freedomotic.util.Info;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author enrico
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class DataUpgradeServiceImplTest {
    
    @Inject DataUpgradeService dataUpgradeService;
    
    public DataUpgradeServiceImplTest() {
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
     * Test of upgrade method, of class DataUpgradeServiceImpl.
     * @throws java.lang.Exception
     */
    @Test
    public void testUpgrade() throws Exception {
        System.out.println("Upgrade a v5.5.0 Thing data to v" + Info.getVersion());
        Class type = EnvObject.class;
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tag>some text</tag>";
        String fromVersion = "5.5.0";
        String expResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tag>some text</tag>";
        String result = (String) dataUpgradeService.upgrade(type, xml, fromVersion);
        assertEquals(expResult, result);
    }
    
}
