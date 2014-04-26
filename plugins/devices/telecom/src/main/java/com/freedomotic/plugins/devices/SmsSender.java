/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices;

import it.freedom.api.Tool;
import it.freedom.exceptions.UnableToExecuteException;
import com.freedomotic.plugins.devices.sms.SmsSenderGui;
import it.freedom.reactions.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 *
 * @author Enrico
 */
public class SmsSender extends Tool {

    String defaultNumber;

    public SmsSender() {
        super("Sms Sender", "/it.nicoletti.telecom/sms-sender.xml");
        defaultNumber = configuration.getStringProperty("default-phone-number", "+393922907161"); //TODO: cambiare prendendo il numero come parametro nel Command
        gui = new SmsSenderGui(this);
        start();
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String phoneNumber = c.getProperty("phone-number");
        String message = c.getProperty("message");
        if (!(phoneNumber.equalsIgnoreCase(""))
                && !(message.equalsIgnoreCase(""))) {
            sendSms(phoneNumber, message);
        }
    }

    /**
     * Invio di un sms testuale tramite il servizio Mobyt
     *
     * @param telNumber
     * @param content
     */
    public void sendSms(String telNumber, String content) {
        try {
            System.out.println("Try to send sms to " + telNumber);
            String data = URLEncoder.encode("key1", "UTF-8") + "=" + URLEncoder.encode("value1", "UTF-8");
            data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");

            String contentFormatted = "";
            //considerare che content non pu� contenere spazi perch� � usato come parametro in URL
            StringTokenizer st = new StringTokenizer(content);
            while (st.hasMoreTokens()) {
                if (st.countTokens() == 1) {
                    contentFormatted += st.nextToken();
                } else {
                    contentFormatted += st.nextToken() + "+";
                }
            }


            System.out.println("Sms content:\n " + contentFormatted);

            String parametri = "ADDRESS=" + contentFormatted;
            parametri += "&NUMBER=" + telNumber;

            System.out.println("Request->" + "http://andiamo.dit.unitn.it/sms/smsformiketxt.php?" + parametri);

            URL url = new URL("http://andiamo.dit.unitn.it/sms/smsformiketxt.php?" + parametri);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            //Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            wr.close();
            rd.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
