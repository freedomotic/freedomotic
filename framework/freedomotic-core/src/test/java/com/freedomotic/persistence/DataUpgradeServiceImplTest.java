/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.persistence;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.testutils.GuiceJUnitRunner;
import com.freedomotic.settings.Info;
import com.thoughtworks.xstream.XStream;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
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
 * @author enrico
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class DataUpgradeServiceImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(DataUpgradeServiceImplTest.class.getName());

    @Inject
    DataUpgradeService dataUpgradeService;

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
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testUpgradeThingFromBenderToCurrent() throws Exception {
        LOG.info("Upgrade a v5.5.0 Thing data to v" + Info.getVersion());
        // Using ".xml" extension instead of ".xobj" otherwise resource file is not loaded correctly
        String xml = IOUtils.toString(
                this.getClass().getResourceAsStream("thing-5.5.0.xml"),
                "UTF-8");
        String result = (String) dataUpgradeService.upgrade(EnvObject.class, xml, "5.5.0");
        // Convert resulting xml into an object
        XStream xstream = FreedomXStream.getXstream();
        EnvObject thing = (EnvObject) xstream.fromXML(result);
        LOG.info("Loaded from upgraded XML: " + thing);
        Assert.assertNotNull(thing);
        //TODO: would be good to test if the two XML are equivalent but there are
        //a lot of possible variations: 
        //assertXMLEquals(expResult, result);
    }

//    private void assertXMLEquals(String expectedXML, String actualXML) throws Exception {
//        XMLUnit.setIgnoreWhitespace(true);
//        XMLUnit.setIgnoreAttributeOrder(true);
//
//        DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expectedXML, actualXML));
//
//        List<?> allDifferences = diff.getAllDifferences();
//        Assert.assertEquals("Differences found: " + diff.toString(), 0, allDifferences.size());
//    }
}
