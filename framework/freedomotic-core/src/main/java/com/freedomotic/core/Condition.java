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
package com.freedomotic.core;

import com.freedomotic.rules.Statement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class Condition {

    private String target;
    private Statement statement;

    /**
     *
     * @param target
     * @param statement
     */
    public Condition(String target, Statement statement) {
        this.target = target;
        this.statement = statement;
    }

    /**
     *
     */
    public Condition() {
    }

    /**
     *
     * @return
     */
    public String getTarget() {
        return target;
    }

    /**
     *
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     *
     * @return
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     *
     * @param statement
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
