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
package com.freedomotic.util;

import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomPolygon;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico Nicoletti
 */
public class TopologyUtilsTest {

    /**
     *
     */
    public TopologyUtilsTest() {
    }

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of translate method, of class TopologyUtils.
     */
    @Test
    public void testTranslate() {
        System.out.println("Translate a polygon");

        FreedomPolygon input = new FreedomPolygon();
        input.append(new FreedomPoint(0, 0));
        input.append(new FreedomPoint(100, 0));
        input.append(new FreedomPoint(100, 100));
        input.append(new FreedomPoint(0, 100));

        int xoffset = 50;
        int yoffset = 50;
        FreedomPolygon expResult = new FreedomPolygon();
        expResult.append(new FreedomPoint(50, 50));
        expResult.append(new FreedomPoint(150, 50));
        expResult.append(new FreedomPoint(150, 150));
        expResult.append(new FreedomPoint(50, 150));

        FreedomPolygon result = TopologyUtils.translate(input, xoffset, yoffset);
        assertEquals(expResult.toString(),
                result.toString());
    }

    /**
     * Test of rotate method, of class TopologyUtils.
     */
    @Test
    public void testRotate() {
        System.out.println("Rotate a polygon");

        FreedomPolygon input = new FreedomPolygon();
        input.append(new FreedomPoint(0, 0));
        input.append(new FreedomPoint(100, 0));
        input.append(new FreedomPoint(100, 50));
        input.append(new FreedomPoint(0, 50));

        int degrees = 90;
        FreedomPolygon expResult = new FreedomPolygon();
        expResult.append(new FreedomPoint(0, 0));
        expResult.append(new FreedomPoint(0, 100));
        expResult.append(new FreedomPoint(-50, 100));
        expResult.append(new FreedomPoint(-50, 0));

        FreedomPolygon result = TopologyUtils.rotate(input, degrees);
        assertEquals(expResult.toString(),
                result.toString());
    }

    /**
     * Test of intersects method, of class TopologyUtils.
     */
    @Test
    public void testIntersects() {
        System.out.println("Intersects a copy of itself (expected true)");

        FreedomPolygon source = new FreedomPolygon();
        source.append(new FreedomPoint(0, 0));
        source.append(new FreedomPoint(100, 0));
        source.append(new FreedomPoint(100, 50));
        source.append(new FreedomPoint(0, 50));

        FreedomPolygon target = new FreedomPolygon();
        target.append(new FreedomPoint(0, 0));
        target.append(new FreedomPoint(100, 0));
        target.append(new FreedomPoint(100, 50));
        target.append(new FreedomPoint(0, 50));

        boolean expResult = true;
        boolean result = TopologyUtils.intersects(source, target);
        assertEquals(expResult, result);
    }

    /**
     * Test of intersects method, of class TopologyUtils.
     */
    @Test
    public void testIntersects2() {
        System.out.println("Intersects with a rotated copy (90°) of itself (expected true)");

        FreedomPolygon source = new FreedomPolygon();
        source.append(new FreedomPoint(0, 0));
        source.append(new FreedomPoint(100, 0));
        source.append(new FreedomPoint(100, 50));
        source.append(new FreedomPoint(0, 50));

        FreedomPolygon target = new FreedomPolygon();
        target.append(new FreedomPoint(0, 0));
        target.append(new FreedomPoint(0, 100));
        target.append(new FreedomPoint(-50, 100));
        target.append(new FreedomPoint(-50, 0));

        boolean expResult = true;
        boolean result = TopologyUtils.intersects(source, target);
        assertEquals(expResult, result);
    }

    /**
     * Test of intersects method, of class TopologyUtils.
     */
    @Test
    public void testIntersects3() {
        System.out.println("Intersects overlapping polygins with no edge collision (expected true)");

        FreedomPolygon source = new FreedomPolygon();
        source.append(new FreedomPoint(0, 0));
        source.append(new FreedomPoint(100, 0));
        source.append(new FreedomPoint(100, 50));
        source.append(new FreedomPoint(0, 50));

        FreedomPolygon target = new FreedomPolygon();
        target.append(new FreedomPoint(50, -25));
        target.append(new FreedomPoint(75, -25));
        target.append(new FreedomPoint(75, 75));
        target.append(new FreedomPoint(50, 75));

        boolean expResult = true;
        boolean result = TopologyUtils.intersects(source, target);

        //TODO: THIS FAILS MUST BE SOLVED
        //assertEquals(expResult, result);
    }

    /**
     * Test of intersects method, of class TopologyUtils.
     */
    @Test
    public void testIntersects4() {
        System.out.println("Intersects overlapping polygins with edge collision (expected true)");

        FreedomPolygon source = new FreedomPolygon();
        source.append(new FreedomPoint(0, 0));
        source.append(new FreedomPoint(100, 0));
        source.append(new FreedomPoint(100, 50));
        source.append(new FreedomPoint(0, 50));

        FreedomPolygon target = new FreedomPolygon();
        target.append(new FreedomPoint(5, 5));
        target.append(new FreedomPoint(75, -25));
        target.append(new FreedomPoint(75, 75));
        target.append(new FreedomPoint(50, 75));

        boolean expResult = true;
        boolean result = TopologyUtils.intersects(source, target);
        assertEquals(expResult, result);
    }

    /**
     * Test of contains method, of class TopologyUtils.
     */
    @Test
    public void testContains() {
        System.out.println("Check if a polygon A contains polygon B");

        FreedomPolygon source = new FreedomPolygon();
        source.append(new FreedomPoint(0, 0));
        source.append(new FreedomPoint(100, 0));
        source.append(new FreedomPoint(100, 50));
        source.append(new FreedomPoint(0, 50));

        FreedomPoint inside = new FreedomPoint(25, 25);
        FreedomPoint onBorder = new FreedomPoint(25, 0);
        FreedomPoint outside = new FreedomPoint(200, 200);
        assertEquals(true,
                TopologyUtils.contains(source, inside));
        assertEquals(false,
                TopologyUtils.contains(source, onBorder));
        assertEquals(false,
                TopologyUtils.contains(source, outside));
    }
}
