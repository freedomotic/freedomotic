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
package com.freedomotic.bus;

import com.freedomotic.settings.Info;
import org.slf4j.LoggerFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;

/**
 * Bus broker implementation holder.
 * <p>
 * It is life cycle managed, see {@link LifeCycle}
 *
 * @author Freedomotic Team
 *
 */
class BusBroker extends LifeCycle {

    private static final Logger LOG = LoggerFactory.getLogger(BusBroker.class.getName());

    private BrokerService broker; // the broker itself.

    private void configureBroker() throws Exception {

        // websocket connector for javascript apps
        broker.addConnector(Info.MESSAGING.BROKER_STOMP);
        // websocket connector for javascript apps
        // broker.addConnector(Info.MESSAGING.BROKER_WEBSOCKET);
        // broker setup
        broker.setPersistent(false); // do not save messages on disk
        broker.setUseJmx(false);
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void start() throws Exception {

        LOG.info("Creating new messaging broker");
        this.broker = new BrokerService();

        LOG.info("Configuring messaging broker");
        configureBroker();

        LOG.info("Starting messaging broker");
        broker.start();
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void stop() throws Exception {

        LOG.info("Stopping messaging broker");
        broker.stop();
    }
}
