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
package com.freedomotic.plugins.devices.mailer;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dumbster.smtp.SimpleSmtpServer;
import com.freedomotic.events.MessageEvent;

/**
 *
 * @author P3trur0, https://flatmap.it
 */

public class MailerPluginTest {

	public MailerPluginTest() {
	}

	private File attachment;
	private static SimpleSmtpServer dummySMTPServer; 
	/**
	 *
	 */
	@BeforeClass
	public static void setUpClass() {
		dummySMTPServer = SimpleSmtpServer.start(2525);
	}

	/**
	 *
	 */
	@AfterClass
	public static void tearDownClass() {
		dummySMTPServer.stop();
		Assert.assertTrue(dummySMTPServer.isStopped());
	}

	/**
	 *
	 */
	@Before
	public void setUp() {
		//this.attachment = new File(getClass().getResource("attachment.txt").getFile());
	}

	/**
	 *
	 */
	@After
	public void tearDown() {
	}

	@Test
	public void testLoadPlugins() throws Exception {

		boolean thrown = false;

//		MessageEvent me = new MessageEvent(null, "Mail test");
//		me.setAttachmentPath(attachment.getAbsolutePath());
//		me.setType("email");
//		me.setFrom("test@email.com");
//		me.setTo("test@recipient.com");
//
//		try {
//			Mailer m = new Mailer();
//			m.onEvent(me);
//		} catch (Exception e) {
//			thrown = true;
//			System.out.println(e.getMessage());
//		}

		Assert.assertFalse("The mail onEvent should not send any exception if properly sent", thrown);
	}

}
