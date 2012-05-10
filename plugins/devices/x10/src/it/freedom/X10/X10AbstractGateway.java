/*Copyright 2009 Enrico Nicoletti
 * eMail: enrico.nicoletti84@gmail.com
 *
 * This file is part of Freedom.
 *
 * Freedom is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Freedom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedom; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package it.freedom.X10;

import it.nicoletti.serial.SerialConnectionProvider;
import it.nicoletti.serial.SerialDataConsumer;
import java.io.IOException;

/**
 *
 * @author enrico
 */
public interface X10AbstractGateway {
    void connect();
    String parseReaded(String readed);
    String send(String message) throws IOException;
}
