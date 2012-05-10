/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.openwebnet;

import it.freedomotic.api.Sensor;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.*;

/*
 * A plugin for Open Web Net protocol author Mauro Cicolella - www.emmecilab.net
 *
 */
public class OpenWebNetSensor extends Sensor {

    private String address = null;
    private String host = null;
    private Integer port = 0;
    private String pass = null;
    private String setPassword = null;
    static BTicinoSocketReadManager gestSocketMonitor;
    static String frame;
    static Integer passwordOpen = 0;

    public OpenWebNetSensor() {
        super("OpenWebNet Sensor", "/it.cicolella.own/own-sensor.xml");
        gui = new OWNFrame();
        // get connection parameters for ethernet gateway
        host = configuration.getProperty("host");
        port = Integer.parseInt(configuration.getProperty("port"));

    }

    @Override
    public void onStart() {
        super.onStart();
        
        //connect to own gateway
        gestSocketMonitor = new BTicinoSocketReadManager(this);
        boolean connected = gestSocketMonitor.connect(host, port, passwordOpen);
        if (connected) {
            this.setDescription("Connected to " + host + ":" + port);
        } else {
            stop();
            this.setDescription("Plugin cannot start: Unable to connect to " + host + ":" + port);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        gestSocketMonitor.close();
        this.setDescription("Disconnected");
    }

    public ProtocolRead buildEventFromFrame(String frame) {
        Freedomotic.logger.info("Received OWN frame '" + frame + "' now translate it to an event.");
        String who = null;
        String what = null;
        String where = null;
        String messageType = null;
        String messageDescription = null;
        String[] frameParts = null;
        ProtocolRead event = null;
        //= new ProtocolRead(this, "OwnProtocol", where);

        int length = frame.length();
        if (frame.isEmpty() || !frame.endsWith("##")) {
            Freedomotic.logger.severe("Malformed frame " + frame + " " + frame.substring(length - 2, length));
        }

        if (frame.equals(OpenWebNetActuator.MSG_OPEN_ACK)) {
            messageType = "ack";
        } else if (frame.equals(OpenWebNetActuator.MSG_OPEN_NACK)) {
            messageType = "nack";
        } else if (frame.substring(0, 2).equalsIgnoreCase("*#")) {
            // remove *# and ##
            frame = frame.substring(2, length - 2);
            frameParts = frame.split("\\*"); // * is reserved so it must be escaped
            who = frameParts[0];
            where = frameParts[1];
            event = new ProtocolRead(this, "OwnProtocol", where);
            // LIGHTING
            if (who.equalsIgnoreCase("1")) {
                if (frameParts[2].equalsIgnoreCase("1")) {
                    String level = frameParts[3];
                    String speed = frameParts[4];
                    messageDescription = "Luminous intensity change";
                    if (level != null) {
                        event.addProperty("level", level);
                    }
                    if (speed != null) {
                        event.addProperty("speed", speed);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("2")) {
                    String hour = frameParts[3];
                    String min = frameParts[4];
                    String sec = frameParts[5];
                    messageDescription = "Luminous intensity change";
                    if (hour != null) {
                        event.addProperty("hour", hour);
                    }
                    if (min != null) {
                        event.addProperty("min", min);
                    }
                    if (sec != null) {
                        event.addProperty("sec", sec);
                    }
                }
            }
            // POWER MANAGEMENT  
            if (who.equalsIgnoreCase("3")) {
                String voltage = null;
                String current = null;
                String power = null;
                String energy = null;
                if (frameParts[3].equalsIgnoreCase("0")) {
                    voltage = frameParts[3];
                    current = frameParts[4];
                    power = frameParts[5];
                    energy = frameParts[6];
                    messageDescription = "Load control status";
                    if (voltage != null) {
                        event.addProperty("voltage", voltage);
                    }
                    if (current != null) {
                        event.addProperty("current", current);
                    }
                    if (power != null) {
                        event.addProperty("power", power);
                    }
                    if (energy != null) {
                        event.addProperty("energy", energy);
                    }
                }
                if (frameParts[3].equalsIgnoreCase("1")) {
                    voltage = frameParts[3];
                    if (voltage != null) {
                        event.addProperty("voltage", voltage);
                    }
                    messageDescription = "Voltage status";
                }
                if (frameParts[3].equalsIgnoreCase("2")) {
                    current = frameParts[3];
                    if (current != null) {
                        event.addProperty("current", current);
                    }
                    messageDescription = "Current status";
                }
                if (frameParts[3].equalsIgnoreCase("3")) {
                    power = frameParts[3];
                    if (power != null) {
                        event.addProperty("power", power);
                    }
                    messageDescription = "Power status";
                }
                if (frameParts[3].equalsIgnoreCase("4")) {
                    energy = frameParts[3];
                    if (energy != null) {
                        event.addProperty("energy", energy);
                    }
                    messageDescription = "Energy status";
                }
            }
            // TERMOREGULATION  
            if (who.equalsIgnoreCase("4")) {
                String temperature = null;
                if (frameParts[3].equalsIgnoreCase("0")) {
                    temperature = frameParts[3];
                    temperature = convertTemperature(temperature);
                    messageDescription = "Temperature value";
                    if (temperature != null) {
                        event.addProperty("temperature", temperature);
                    }
                }
            }

            // GATEWAY CONTROL
            if (who.equalsIgnoreCase("13")) {
                String hour = null;
                String minute = null;
                String second = null;
                String timeZone = null;
                String dayWeek = null;
                String day = null;
                String month = null;
                String year = null;
                String version = null;
                String release = null;
                String build = null;
                if (frameParts[2].equalsIgnoreCase("0")) {
                    hour = frameParts[3];
                    minute = frameParts[4];
                    second = frameParts[5];
                    timeZone = frameParts[6]; // aggiungere funzione conversione
                    messageType = "gatewayControl";
                    messageDescription = "Time request";
                    if (hour != null) {
                        event.addProperty("hour", hour);
                    }
                    if (minute != null) {
                        event.addProperty("minute", minute);
                    }
                    if (second != null) {
                        event.addProperty("second", second);
                    }
                    if (timeZone != null) {
                        event.addProperty("timeZone", timeZone);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("1")) {
                    dayWeek = dayName(frameParts[3]);
                    day = frameParts[4];
                    month = frameParts[5];
                    year = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Date request";
                    if (dayWeek != null) {
                        event.addProperty("dayWeek", dayWeek);
                    }
                    if (day != null) {
                        event.addProperty("day", day);
                    }
                    if (month != null) {
                        event.addProperty("month", month);
                    }
                    if (year != null) {
                        event.addProperty("year", year);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("10")) {
                    String ip1 = frameParts[3];
                    String ip2 = frameParts[4];
                    String ip3 = frameParts[5];
                    String ip4 = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "IP request";
                    event.addProperty("ip-address", ip1 + "." + ip2 + "." + ip3 + "." + ip4);
                }
                if (frameParts[2].equalsIgnoreCase("11")) {
                    String netmask1 = frameParts[3];
                    String netmask2 = frameParts[4];
                    String netmask3 = frameParts[5];
                    String netmask4 = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Netmask request";
                    event.addProperty("netmask", netmask1 + "." + netmask2 + "." + netmask3 + "." + netmask4);
                }
                if (frameParts[2].equalsIgnoreCase("12")) {
                    String mac1 = frameParts[3];
                    String mac2 = frameParts[4];
                    String mac3 = frameParts[5];
                    String mac4 = frameParts[6];
                    String mac5 = frameParts[7];
                    String mac6 = frameParts[8];
                    messageType = "gatewayControl";
                    messageDescription = "MAC request";
                    event.addProperty("mac-address", mac1 + ":" + mac2 + ":" + mac3 + ":" + mac4 + ":" + mac5 + ":" + mac6);
                }
                if (frameParts[2].equalsIgnoreCase("15")) {
                    String model = gatewayModel(frameParts[3]);
                    messageType = "gatewayControl";
                    messageDescription = "Model request";
                    event.addProperty("model", model);
                }
                if (frameParts[2].equalsIgnoreCase("16")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Firmware version request";
                    event.addProperty("firmware-version", version + "." + release + "." + build);
                }
                if (frameParts[2].equalsIgnoreCase("17")) {
                    String days = frameParts[3];
                    String hours = frameParts[4];
                    String minutes = frameParts[5];
                    String seconds = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Uptime request";
                    event.addProperty("uptime", days + "D:" + hours + "H:" + minutes + "m:" + seconds + "s");
                }
                if (frameParts[2].equalsIgnoreCase("22")) {
                    hour = frameParts[3];
                    minute = frameParts[4];
                    second = frameParts[5];
                    timeZone = frameParts[6];
                    String weekDay = dayName(frameParts[7]);
                    day = frameParts[8];
                    month = frameParts[9];
                    year = frameParts[10];
                    messageType = "gatewayControl";
                    messageDescription = "Date&Time request";
                    event.addProperty("date", weekDay + " " + day + "/" + month + "/" + year);
                    event.addProperty("time", hour + ":" + minute + ":" + second);
                }
                if (frameParts[2].equalsIgnoreCase("23")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Kernel version request";
                    event.addProperty("kernel-version", version + "." + release + "." + build);
                }
                if (frameParts[2].equalsIgnoreCase("24")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Distribution version request";
                    event.addProperty("distribution-version", version + "." + release + "." + build);

                }
            }
            event.addProperty("who", who);
            if (where != null) {
                event.addProperty("where", where);
            }
            if (messageDescription != null) {
                event.addProperty("messageDescription", messageDescription);
            }
            if (messageType != null) {
                event.addProperty("messageType", messageType);
            }
            // notify event
            notifyEvent(event);
        } // }
        else if (!(frame.substring(0, 2).equalsIgnoreCase("*#"))) {
            // remove * and ##
            frame = frame.substring(1, length - 2);
            frameParts = frame.split("\\*"); // * is reserved so it must be escaped
            who = frameParts[0];
            where = frameParts[2];
            event = new ProtocolRead(this, "OwnProtocol", where);

            // LIGHTING
            if (who.equalsIgnoreCase("1")) {
                what = frameParts[1];
                where = frameParts[2];
                messageType = "lighting";
                if (what.equalsIgnoreCase("1")) {
                    messageDescription = "Light ON";
                }
                if (what.equalsIgnoreCase("0")) {
                    messageDescription = "Light OFF";
                }
                if (Integer.parseInt(what) >= 2 && Integer.parseInt(what) <= 10) {
                    messageDescription = "Light Dimmer";
                }
            }

            // AUTOMATION
            if (who.equalsIgnoreCase("2")) {
                what = frameParts[1];
                where = frameParts[2];
                messageType = "automation";
                if (what.equalsIgnoreCase("0")) {
                    messageDescription = "Automation STOP";
                }
                if (what.equalsIgnoreCase("1")) {
                    messageDescription = "Automation UP";
                }
                if (what.equalsIgnoreCase("2")) {
                    messageDescription = "Automation DOWN";
                }
            }

            // TERMOREGULATION
            if (who.equalsIgnoreCase("4")) {
                what = frameParts[1];
                where = frameParts[2];
                messageType = "termoregulation";
                if (what.equalsIgnoreCase("0")) {
                    messageDescription = "Conditioning";
                }
                if (what.equalsIgnoreCase("1")) {
                    messageDescription = "Heating";
                }
                if (what.equalsIgnoreCase("20")) {
                    messageDescription = "Remote Control disabled";
                }
                if (what.equalsIgnoreCase("21")) {
                    messageDescription = "Remote Control enabled";
                }
                if (what.equalsIgnoreCase("22")) {
                    messageDescription = "At least one Probe OFF";
                }
                if (what.equalsIgnoreCase("23")) {
                    messageDescription = "At least one Probe in protection";
                }
                if (what.equalsIgnoreCase("24")) {
                    messageDescription = "At least one Probe in manual";
                }
                if (what.equalsIgnoreCase("30")) {
                    messageDescription = "Failure discovered";
                }
                if (what.equalsIgnoreCase("31")) {
                    messageDescription = "Central Unit battery KO";
                }
                if (what.equalsIgnoreCase("103")) {
                    messageDescription = "OFF Heating";
                }
                if (what.equalsIgnoreCase("110")) {
                    messageDescription = "Manual Heating";
                }
                if (what.equalsIgnoreCase("111")) {
                    messageDescription = "Automatic Heating";
                }
                if (what.equalsIgnoreCase("202")) {
                    messageDescription = "AntiFreeze";
                }
                if (what.equalsIgnoreCase("203")) {
                    messageDescription = "OFF Conditioning";
                }
                if (what.equalsIgnoreCase("210")) {
                    messageDescription = "Manual Conditioning";
                }
                if (what.equalsIgnoreCase("211")) {
                    messageDescription = "Automatic Conditioning";
                }
                if (what.equalsIgnoreCase("302")) {
                    messageDescription = "Thermal Protection";
                }
                if (what.equalsIgnoreCase("303")) {
                    messageDescription = "Generic OFF";
                }
                if (what.equalsIgnoreCase("311")) {
                    messageDescription = "Automatic Generic";
                }
            }
        }

        // BURGLAR ALARM
        if (who.equalsIgnoreCase("5")) {
            what = frameParts[1];
            where = frameParts[2];
            messageType = "alarm";
            if (what.equalsIgnoreCase("0")) {
                messageDescription = "System on maintenance";
            }
            if (what.equalsIgnoreCase("4")) {
                messageDescription = "Battery fault";
            }
            if (what.equalsIgnoreCase("5")) {
                messageDescription = "Battery OK";
            }
            if (what.equalsIgnoreCase("6")) {
                messageDescription = "No Network";
            }
            if (what.equalsIgnoreCase("7")) {
                messageDescription = "Network OK";
            }
            if (what.equalsIgnoreCase("8")) {
                messageDescription = "System engaged";
            }
            if (what.equalsIgnoreCase("9")) {
                messageDescription = "System disengaged";
            }
            if (what.equalsIgnoreCase("10")) {
                messageDescription = "Battery KO";
            }
            if (what.equalsIgnoreCase("11")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Zone " + where + " engaged";
            }
            if (what.equalsIgnoreCase("12")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Aux " + where + " in Technical alarm ON";
            }
            if (what.equalsIgnoreCase("13")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Aux " + where + " in Technical alarm RESET";
            }
            if (what.equalsIgnoreCase("15")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Zone " + where + " in Intrusion alarm";
            }
            if (what.equalsIgnoreCase("16")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Zone " + where + " in Tampering alarm";
            }
            if (what.equalsIgnoreCase("17")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Zone " + where + " in Anti-panic alarm";
            }
            if (what.equalsIgnoreCase("18")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Zone " + where + " divided";
            }
            if (what.equalsIgnoreCase("31")) // prelevare la sottostringa di where #N 
            {
                messageDescription = "Silent alarm from aux " + where;
            }
        }

        // }
        // SOUND SYSTEM
        if (who.equalsIgnoreCase("16")) {
            what = frameParts[1];
            where = frameParts[2];
            messageType = "soundSystem";
            if (what.equalsIgnoreCase("0")) {
                messageDescription = "ON Baseband";
            }
            if (what.equalsIgnoreCase("3")) {
                messageDescription = "ON Stereo channel";
            }
            if (what.equalsIgnoreCase("10")) {
                messageDescription = "OFF Baseband";
            }
            if (what.equalsIgnoreCase("13")) {
                messageDescription = "OFF Stereo channel";
            }
            if (what.equalsIgnoreCase("30")) {
                messageDescription = "Sleep on baseband";
            }
            if (what.equalsIgnoreCase("33")) {
                messageDescription = "Sleep on stereo channel";
            }
        }
        //}
        if (who != null) {
            event.addProperty("who", who);
        }
        if (what != null) {
            event.addProperty("what", what);
        }
        if (where != null) {
            event.addProperty("where", where);
        }
        if (messageType != null) {
            event.addProperty("messageType", messageType);
        }
        if (messageDescription != null) {
            event.addProperty("messageDescription", messageDescription);
        }

        Freedomotic.logger.info("Frame " + frame + " " + "is " + messageType + " message. Notify it as Freedomotic event " + messageDescription); // for debug
        OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Rx:" + frame + " " + "(" + messageType + ")");
        notifyEvent(event);
        // }
        return null;
    }

    public String gatewayModel(String modelNumber) {
        String model = null;
        switch (new Integer(Integer.parseInt(modelNumber))) {
            case (2):
                model = "MHServer";
            case (4):
                model = "MH20F0";
            case (6):
                model = "F452V";
            case (11):
                model = "MHServer2";
            case (12):
                model = "F453AV";
            case (13):
                model = "H4684";
            default:
                model = "Unknown";
        }
        return (model);
    }

    public String dayName(String dayNumber) {
        String dayName = null;
        switch (new Integer(Integer.parseInt(dayNumber))) {
            case (0):
                dayNumber = "Sunday";
            case (1):
                dayNumber = "Monday";
            case (2):
                dayNumber = "Tuesday";
            case (3):
                dayNumber = "Wednesday";
            case (4):
                dayNumber = "Thursday";
            case (5):
                dayNumber = "Friday";
            case (6):
                dayNumber = "Saturday";
        }
        return (dayName);
    }

    public String convertTemperature(String temperature) {
        String temp = null;
        if (!temperature.substring(0).equalsIgnoreCase("0")) {
            temp = "-";
        }
        temp += temperature.substring(1, 2);
        temp += ".";
        temp += temperature.substring(3);
        return (temp);
    }

    @Override
    protected void onInformationRequest() throws IOException, UnableToExecuteException {
        //maybe a publishResults could be use here
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
    }

    public boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
