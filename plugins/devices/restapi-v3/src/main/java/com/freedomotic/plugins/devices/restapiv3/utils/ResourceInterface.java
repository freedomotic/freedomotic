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
    Response list();

    @Override
    Response get(String uuid);

    Response delete(String uuid);

    Response copy(String uuid);

    Response create(T s);

    Response update(String uuid, T s);

    @Override
    Response options();

}
