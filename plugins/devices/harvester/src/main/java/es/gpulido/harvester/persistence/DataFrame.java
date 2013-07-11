package es.gpulido.harvester.persistence;

/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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


import es.gpulido.harvester.persistence.DataToPersist;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class DataFrame implements Serializable {

    public final static int FULL_UPDATE = 1;
    public final static int INCREMENTAL_UPDATE = 2;
    private int frameType;
    private List<DataToPersist> data;

    public DataFrame(int type, List<DataToPersist> data) {
        this.frameType = type;
        this.data = data;
    }
    // used by Jackson , do not remove
    public DataFrame(){ }
    
    public int getFrameType(){
        return this.frameType;
    }
    
    public List<DataToPersist> getData(){
        return this.data;
    }
}
