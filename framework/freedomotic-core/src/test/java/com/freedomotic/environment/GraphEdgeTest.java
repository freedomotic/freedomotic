/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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

package com.freedomotic.environment;

import org.junit.Assert;
import org.junit.Test;

public class GraphEdgeTest {

  @Test
  public void compareToInputNotNullOutputNegative() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(null, -1, 232_501_675);
    final GraphEdge other = new GraphEdge(null, -1, 435_655_486);

    // Act
    final int retval = objectUnderTest.compareTo(other);

    // Assert result
    Assert.assertEquals(-1, retval);
  }

  @Test
  public void compareToInputNotNullOutputNegative2() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(null, 2_147_483_295, 536_871_680);
    final GraphEdge other = new GraphEdge(34_627_582, 2_147_483_551, 541_065_984);

    // Act
    final int retval = objectUnderTest.compareTo(other);

    // Assert result
    Assert.assertEquals(-1, retval);
  }

  @Test
  public void compareToInputNotNullOutputPositive() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(null, 2_147_483_295, 541_065_984);
    final GraphEdge other = new GraphEdge(34_627_582, 2_147_483_551, 536_871_680);

    // Act
    final int retval = objectUnderTest.compareTo(other);

    // Assert result
    Assert.assertEquals(1, retval);
  }

  @Test
  public void compareToInputNotNullOutputPositive2() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(null, -1, 2_147_483_323);
    final GraphEdge other = new GraphEdge(null, -1, 1_879_047_867);

    // Act
    final int retval = objectUnderTest.compareTo(other);

    // Assert result
    Assert.assertEquals(1, retval);
  }

  @Test
  public void compareToInputNotNullOutputPositive3() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(1_349_517_313, null, 558_365_760);
    final GraphEdge other = new GraphEdge(1_349_517_312, 0, 558_365_760);

    // Act
    final int retval = objectUnderTest.compareTo(other);

    // Assert result
    Assert.assertEquals(1, retval);
  }

  @Test
  public void compareToInputNotNullOutputZero() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(1_416_985_954, 0, 4_255_775);
    final GraphEdge other = new GraphEdge(1_416_985_954, 0, 4_255_775);

    // Act
    final int retval = objectUnderTest.compareTo(other);

    // Assert result
    Assert.assertEquals(0, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(-301_989_904, 134_217_728, null);
    final GraphEdge other = new GraphEdge(-301_989_904, null, -2_147_483_647);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse2() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(-301_989_890, null, null);
    final GraphEdge other = new GraphEdge(null, -2_147_483_647, -2_147_483_647);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse3() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(-303_054_864, 134_217_728, null);
    final GraphEdge other = new GraphEdge(-303_054_864, 0, -2_147_483_647);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse4() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(0, -536_870_913, null);
    final GraphEdge other = new GraphEdge(null, 1_610_612_735, null);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse5() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(0, -536_870_913, 0);
    final GraphEdge other = new GraphEdge(0, -536_870_913, null);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse6() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(0, -306_783_379, null);
    final GraphEdge other = new GraphEdge(0, null, null);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputFalse7() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(0, -536_870_913, null);
    final GraphEdge other = new GraphEdge(0, 1_610_612_735, null);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNullOutputFalse() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(null, null, null);
    final Object other = null;

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsInputNotNullOutputTrue() {

    // Arrange
    final GraphEdge objectUnderTest = new GraphEdge(0, -536_870_913, 0);
    final GraphEdge other = new GraphEdge(0, -536_870_913, 0);

    // Act
    final boolean retval = objectUnderTest.equals(other);

    // Assert result
    Assert.assertEquals(true, retval);
  }

}
