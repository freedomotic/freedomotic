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
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;

/**
 *
 * @author http://www.javapractices.com/topic/TopicAction.do?Id=144
 */
public final class Mailer extends Protocol {

	/**
	 *
	 */
	public static final Logger LOG = LoggerFactory.getLogger(Mailer.class.getName());
	final String USERNAME = configuration.getProperty("username");
	final String PASSWORD = configuration.getProperty("password");
	private int sentMails;

	/**
	 *
	 */
	public Mailer() {
		super("Mailer", "/mailer/mailer-manifest.xml");
		addEventListener("app.event.sensor.messages.mail");
	}
	
	@Override
	protected void onRun() {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void onCommand(Command c) throws IOException, UnableToExecuteException {
		String from = c.getProperty("from");
		if (from == null) {
			from = "your.home@freedomotic.com";
		}
		String to = c.getProperty("to");
		if (to == null || to.isEmpty()) {
			to = USERNAME;
		}
		String subject = c.getProperty("subject");
		if (subject == null || subject.isEmpty()) {
			subject = "A notification from Freedomotic";
		}
		final String text = c.getProperty("message");

		try {
			send(from, to, subject, text, this.buildAttachmentFromAbsolutePath(c.getProperty("attachment")));
		} catch (MessagingException ex) {
			this.notifyCriticalError("Error while sending email '" + subject + "' to '" + to + "' for "
					+ Freedomotic.getStackTraceInfo(ex));
		}

	}

	private File buildAttachmentFromAbsolutePath(String path) {
		if (path == null || path.isEmpty())
			return null;

		File attachment = new File(path);

		if (attachment.exists())
			return attachment;
		else {
			this.notifyCriticalError("Error while retrieving mail attachment, no existing file at path " + path);
			return null;
		}
	}

	/**
	 * Sends the mail with an attachment, if any.
	 *
	 * @param from
	 *            mail sender
	 * @param to
	 *            mail receiver
	 * @param subject
	 *            mail subject
	 * @param text
	 *            mail body
	 * @param attached file
	 *            mail attachment, if any
	 *            
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void send(String from, String to, String subject, String text, File attachment)
			throws AddressException, MessagingException, IOException {
		Properties props = new Properties();
		props.put("mail.smtp.auth", configuration.getProperty("mail.smtp.auth"));
		if (configuration.getProperty("mail.smtp.connection").equalsIgnoreCase("TLS")) {
			props.put("mail.smtp.starttls.enable", "true");
		} else {
			props.put("mail.smtp.socketFactory.port", configuration.getProperty("mail.smtp.port"));
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}
		props.put("mail.smtp.host", configuration.getProperty("mail.smtp.host"));
		props.put("mail.smtp.port", configuration.getProperty("mail.smtp.port"));

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		});

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject(subject);

		if (attachment != null) {
			MimeBodyPart textMsg = new MimeBodyPart();
			textMsg.setText(text);
			MimeBodyPart attachedFile = new MimeBodyPart();
			attachedFile.attachFile(attachment);

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(textMsg);
			mp.addBodyPart(attachedFile);
			message.setContent(mp);
		}
		
		else {
			message.setText(text);
		}

		message.setSentDate(new Date());

		Transport.send(message);
		sentMails++;
		setDescription(sentMails + " emails sent");
	}
	
	private void sendWithAttachment(String from, String to, String subject, String text, String attachmentPath)
			throws AddressException, MessagingException, IOException {
		File attachment = this.buildAttachmentFromAbsolutePath(attachmentPath);
		this.send(from, to, subject, text, attachment);
	}

	@Override
	protected boolean canExecute(Command c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void onEvent(EventTemplate event) {
		if (event instanceof MessageEvent) {
			MessageEvent mail = (MessageEvent) event;
			try {
				this.sendWithAttachment(mail.getFrom(), mail.getTo(), null, mail.getText(), mail.getAttachmentPath());
			} catch (Exception ex) {
				this.notifyCriticalError("Error while sending email to " + mail.getTo() + ": " + ex.getMessage());
			}
		}
	}
}
