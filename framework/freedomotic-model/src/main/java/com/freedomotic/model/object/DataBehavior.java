/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
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
package com.freedomotic.model.object;

import com.freedomotic.model.charting.UsageData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Matteo Mazzoni
 */
@XmlRootElement
public class DataBehavior extends Behavior {

    private ArrayList<UsageData> data;

    /**
     *
     * @param data
     */
    public void setData(Collection<UsageData> data) {
        this.data = new ArrayList<>(data);
    }

    /**
     *
     * @param data
     */
    public void addData(Collection<UsageData> data) {
        this.data.addAll(data);
    }

    /**
     *
     * @return
     */
    public List<UsageData> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataBehavior that = (DataBehavior) o;

        return data == null ? that.data == null : data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
