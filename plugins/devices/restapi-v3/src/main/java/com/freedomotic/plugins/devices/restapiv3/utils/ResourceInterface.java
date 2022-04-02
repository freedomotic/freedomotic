/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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
package com.freedomotic.plugins.devices.restapiv3.utils;

import java.net.URISyntaxException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 * @param <T> the type of element that is used in the resource
 */
public interface ResourceInterface<T> extends ResourceReadOnlyInterface {

    @Override
    public Response list();

    @Override
    public Response get(String uuid);

    public Response delete(String uuid);

    public Response copy(String uuid);

    public Response create(T s);

    public Response update(String uuid, T s);

    @Override
    public Response options();

}
