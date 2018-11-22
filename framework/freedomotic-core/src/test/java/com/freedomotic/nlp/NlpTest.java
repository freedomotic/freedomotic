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
