package com.freedomotic.rules;

import com.freedomotic.rules.ExpressionFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExpressionFactoryTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void createExpressionInputNullNotNullNullOutputUnsupportedOperationException() {

    // Arrange
    final ExpressionFactory objectUnderTest = new ExpressionFactory();
    final String left = null;
    final String operand = "NNO,_NONSCOWHON";
    final String right = null;

    // Act
    thrown.expect(UnsupportedOperationException.class);
    objectUnderTest.createExpression(left, operand, right);

    // Method is not expected to return due to exception thrown
  }

  @Test
  public void createExpressionInputNullNullNullOutputNullPointerException() {

    // Arrange
    final ExpressionFactory objectUnderTest = new ExpressionFactory();
    final String left = null;
    final String operand = null;
    final String right = null;

    // Act
    thrown.expect(NullPointerException.class);
    objectUnderTest.createExpression(left, operand, right);

    // Method is not expected to return due to exception thrown
  }

  @Test
  public void createExpressionInputNotNullNotNullNotNullOutputUnsupportedOperationException() {

    // Arrange
    final ExpressionFactory objectUnderTest = new ExpressionFactory();
    final String left = "        ";
    final String operand = "DDDEEFE";
    final String right = "    ";

    // Act
    thrown.expect(UnsupportedOperationException.class);
    objectUnderTest.createExpression(left, operand, right);

    // Method is not expected to return due to exception thrown
  }
}
