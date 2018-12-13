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

package com.freedomotic.rules;

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
