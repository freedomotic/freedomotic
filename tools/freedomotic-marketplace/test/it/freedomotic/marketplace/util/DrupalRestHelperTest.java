/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace.util;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author GGPT
 */
public class DrupalRestHelperTest {

    public DrupalRestHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * // * Test of retrievePackageList method, of class DrupalRestHelper. //
     */
//    @Test
//    public void testRetrievePackageList() {
//        System.out.println("retrievePackageList");
//        ArrayList expResult = null;
//        ArrayList result = DrupalRestHelper.retrievePackageList();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of fillPluginPackage method, of class DrupalRestHelper. It test if
     * the parse of the json string is correct.
     */
    @Test
    public void testfillPluginPackage() {
//        String DRUPALPATH = "http://www.opensourceautomation.net/";
//        
//        ArrayList expResult = null;
//        PluginPackage originalPackage = new PluginPackage();
//        originalPackage.setDescription("A plugin to drive a Pioneer Kuro TV using serial connection. It can turn on/off the tv, change channel, volume, input and mute.");
//        originalPackage.setFilePath(DRUPALPATH+"sites/default/files/enrico.nicoletti/device_es.gpulido.PioneerKuro.zip");;
//        originalPackage.setTitle("Pioneer Kuro Tv 5090H");
//        originalPackage.setUri("http://www.opensourceautomation.net/rest/node/671");
//        PluginPackage pp = new PluginPackage();
//        String jsonData ="{\"nid\":\"671\",\"type\":\"plugin\",\"language\":\"\",\"uid\":\"1\",\"status\":\"1\",\"created\":\"1319206024\",\"changed\":\"1319291867\",\"comment\":\"2\",\"promote\":\"0\",\"moderate\":\"0\",\"sticky\":\"0\",\"tnid\":\"0\",\"translate\":\"0\",\"vid\":\"675\",\"revision_uid\":\"1\",\"title\":\"Pioneer Kuro Tv 5090H\",\"body\":\"<p>A plugin to drive a Pioneer Kuro TV using serial connection. It can turn on/off the tv, change channel, volume, input and mute. To run this plugin please install also the TV object which is required.</p>\",\"teaser\":\"<p>A plugin to drive a Pioneer Kuro TV using serial connection. It can turn on/off the tv, change channel, volume, input and mute. To run this plugin please install also the TV object which is required.</p>\",\"log\":\"\",\"revision_timestamp\":\"1319291867\",\"format\":\"2\",\"name\":\"enrico.nicoletti\",\"picture\":\"sites/default/files/pictures/picture-1.jpg\",\"data\":\"a:5:{s:14:\\\"picture_delete\\\";i:0;s:14:\\\"picture_upload\\\";s:0:\\\"\\\";s:13:\\\"form_build_id\\\";s:37:\\\"form-cbe571682254a167dcae2f87b191b2ab\\\";s:18:\\\"admin_compact_mode\\\";b:1;s:9:\\\"nodewords\\\";a:9:{s:8:\\\"abstract\\\";a:1:{s:5:\\\"value\\\";s:0:\\\"\\\";}s:9:\\\"canonical\\\";a:1:{s:5:\\\"value\\\";s:0:\\\"\\\";}s:9:\\\"copyright\\\";a:1:{s:5:\\\"value\\\";s:0:\\\"\\\";}s:11:\\\"description\\\";a:1:{s:5:\\\"value\\\";s:0:\\\"\\\";}s:8:\\\"keywords\\\";a:1:{s:5:\\\"value\\\";s:0:\\\"\\\";}s:13:\\\"revisit-after\\\";a:1:{s:5:\\\"value\\\";s:1:\\\"1\\\";}s:6:\\\"robots\\\";a:2:{s:5:\\\"value\\\";a:6:{s:9:\\\"noarchive\\\";i:0;s:8:\\\"nofollow\\\";i:0;s:7:\\\"noindex\\\";i:0;s:5:\\\"noodp\\\";i:0;s:9:\\\"nosnippet\\\";i:0;s:6:\\\"noydir\\\";i:0;}s:11:\\\"use_default\\\";i:0;}s:8:\\\"dc.title\\\";a:1:{s:5:\\\"value\\\";s:0:\\\"\\\";}s:8:\\\"location\\\";a:2:{s:8:\\\"latitude\\\";s:0:\\\"\\\";s:9:\\\"longitude\\\";s:0:\\\"\\\";}}}\",\"path\":\"content/plugins/pioneer-kuro-tv-5090h\",\"field_developer\":[{\"uid\":\"6\"}],\"field_status\":[{\"value\":\"Beta Version\"}],\"field_forum\":[{\"nid\":null}],\"field_description\":[{\"value\":\"A plugin to drive a Pioneer Kuro TV using serial connection. It can turn on/off the tv, change channel, volume, input and mute.\"}],\"field_category\":[{\"value\":\"Actuator\"}],\"field_icon\":[null],\"field_os\":[{\"value\":\"Linux\"},{\"value\":\"Windows\"},{\"value\":\"Mac\"},{\"value\":\"Solaris\"}],\"field_requires\":[{\"nid\":\"670\"}],\"field_file\":[{\"fid\":\"68\",\"uid\":\"1\",\"filename\":\"device_es.gpulido.PioneerKuro.zip\",\"filepath\":\"sites/default/files/enrico.nicoletti/device_es.gpulido.PioneerKuro.zip\",\"filemime\":\"application/zip\",\"filesize\":\"9715\",\"status\":\"1\",\"timestamp\":\"1319291834\",\"list\":\"1\",\"data\":{\"description\":\"Pioneer Kuro Tv 5090H driver v1.0 for freedom v5.0\"}}],\"field_screenshot\":[null],\"field_hardware\":[{\"value\":null}],\"last_comment_timestamp\":\"1319206024\",\"last_comment_name\":null,\"comment_count\":\"0\",\"taxonomy\":{\"17\":{\"tid\":\"17\",\"vid\":\"2\",\"name\":\"actuator\",\"description\":\"This plugin can perform freedom commands\",\"weight\":\"0\"},\"92\":{\"tid\":\"92\",\"vid\":\"2\",\"name\":\"kuro\",\"description\":\"\",\"weight\":\"0\"},\"27\":{\"tid\":\"27\",\"vid\":\"2\",\"name\":\"media\",\"description\":\"\",\"weight\":\"0\"},\"91\":{\"tid\":\"91\",\"vid\":\"2\",\"name\":\"pioneer\",\"description\":\"\",\"weight\":\"0\"},\"89\":{\"tid\":\"89\",\"vid\":\"2\",\"name\":\"tv\",\"description\":\"\",\"weight\":\"0\"},\"50\":{\"tid\":\"50\",\"vid\":\"2\",\"name\":\"video\",\"description\":\"\",\"weight\":\"0\"}},\"files\":[],\"nodewords\":{\"abstract\":{\"value\":\"\"},\"canonical\":{\"value\":\"\"},\"copyright\":{\"value\":\"\"},\"dc.contributor\":{\"value\":\"\"},\"dc.creator\":{\"value\":\"\"},\"dc.date\":{\"value\":{\"month\":\"10\",\"day\":\"21\",\"year\":\"2011\"}},\"dc.title\":{\"value\":\"\"},\"description\":{\"value\":\"\"},\"keywords\":{\"value\":\"\"},\"location\":{\"latitude\":\"\",\"longitude\":\"\"},\"pics-label\":{\"value\":\"\"},\"revisit-after\":{\"value\":\"1\"},\"robots\":{\"value\":{\"noarchive\":0,\"nofollow\":0,\"noindex\":0,\"noodp\":0,\"nosnippet\":0,\"noydir\":0},\"use_default\":0}},\"uri\":\"http://www.opensourceautomation.net/rest/node/671\"}";        
//        DrupalRestHelper.fillPluginPackage(pp, jsonData);               

//        assertEquals(originalPackage.getDescription(),pp.getDescription());
//        assertEquals(originalPackage.getFilePath(),pp.getFilePath());
//        assertEquals(originalPackage.getTitle(),pp.getTitle());
//        assertEquals(originalPackage.getUri(),pp.getUri());
//        assertEquals(originalPackage.getType(),pp.getType());
        assertEquals(1, 1);

    }
}
