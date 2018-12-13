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

package com.freedomotic.nlp;

import com.freedomotic.nlp.Nlp.DescendingRankComparator;
import com.freedomotic.nlp.Nlp.Rank;
import org.junit.rules.ExpectedException;
import org.junit.Rule;
import org.junit.Assert;
import org.junit.Test;

public class NlpTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void compareInputNotNullNotNullOutputZero() {

    // Arrange
    final DescendingRankComparator objectUnderTest = new DescendingRankComparator();
    final Rank ob1 = new Rank(0, null);
    final Rank ob2 = new Rank(0, null);

    // Act
    final int retval = objectUnderTest.compare(ob1, ob2);

    // Assert result
    Assert.assertEquals(0, retval);
  }

  @Test
  public void compareInputNullNotNullOutputNullPointerException() {

    // Arrange
    final DescendingRankComparator objectUnderTest = new DescendingRankComparator();
    final Rank ob1 = null;
    final Rank ob2 = new Rank(0, null);

    // Act
    thrown.expect(NullPointerException.class);
    objectUnderTest.compare(ob1, ob2);

    // Method is not expected to return due to exception thrown
  }
}
