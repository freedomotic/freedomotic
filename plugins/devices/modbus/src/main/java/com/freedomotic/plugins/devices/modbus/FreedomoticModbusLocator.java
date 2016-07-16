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
package com.freedomotic.plugins.devices.modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.locator.BinaryLocator;
import com.serotonin.modbus4j.locator.NumericLocator;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.model.ds.Config;

/**
 * This class is used to encapsulate the ModbusLocator used by Modbus4J and to
 * extend with Freedom specific functionality. The locators are the elements
 * that are used to configure the reads /writes on the modbus and to expose that
 * information in Freedom.
 *
 * @author gpt
 */
class FreedomoticModbusLocator {

    private BaseLocator modbusLocator;
    private final String name;
    private final int slaveId;
    private final int registerRange;
    private final int dataType;
    private final int offset;
    //private int numberOfRegisters;
    private final Byte bit;
    //private String characterEncoding;
    private final Double multiplier;
    private final Double additive;
    private final String eventName;
    private final String objectName;
    private final String objectClass;
    private final String objectBehavior;
    //used to store the lastvalue readed and only send if changes
    //TODO: to be used
    private Object lastValueReaded;

    /**
     * Constructor.
     *
     * @param configuration
     * @param i
     */
    FreedomoticModbusLocator(Config configuration, int i) {

        if (i < 0) {
            name = configuration.getStringProperty("Name", "DefaultName");
            slaveId = configuration.getIntProperty("SlaveId", 0);
            registerRange = parseRegisterRange(configuration.getStringProperty("RegisterRange", "COIL_STATUS"));
            dataType = parseDataType(configuration.getStringProperty("DataType", "BINARY"));
            offset = configuration.getIntProperty("Offset", 0);

            //protocol read link
            objectName = configuration.getStringProperty("objectName", "");
            objectClass = configuration.getStringProperty("objectClass", "");
            objectBehavior = configuration.getStringProperty("objectBehavior", "");

            //TODO: The Modbus4j functionality must be extend to allow to read and combine several "bit" values from
            // the same register. For example bit1&bit2 generates a 4 four states value and there are Slaves that uses this format,
            // and should be abstracted.
            //we try to parse the bit value.
            bit = Byte.valueOf(configuration.getStringProperty("Bit", "-1"));
            
            //TODO: use the number of registers
            //numberOfRegisters= configuration.getTuples().getIntProperty(i,"NumberOfRegisters",1);
            //At this moment the characterEncoding is not necesary
            //characterEncoding=configuration.getTuples().getStringProperty(i,"CharacterEncoding","ASCII");         
            multiplier = configuration.getDoubleProperty("Multiplier", 1);
            additive = configuration.getDoubleProperty("Additive", 0);
            eventName = configuration.getStringProperty("EventName", "Event");

        } else {
            name = configuration.getTuples().getStringProperty(i, "Name", "DefaultName");
            slaveId = configuration.getTuples().getIntProperty(i, "SlaveId", 0);
            registerRange = parseRegisterRange(configuration.getTuples().getStringProperty(i, "RegisterRange", "COIL_STATUS"));
            dataType = parseDataType(configuration.getTuples().getStringProperty(i, "DataType", "BINARY"));
            offset = configuration.getTuples().getIntProperty(i, "Offset", 0);

            //protocol read link
            objectName = configuration.getTuples().getStringProperty(i, "objectName", "");
            objectClass = configuration.getTuples().getStringProperty(i, "objectClass", "");
            objectBehavior = configuration.getTuples().getStringProperty(i, "objectBehavior", "");

            //TODO: The Modbus4j functionality must be extend to allow to read and combine several "bit" values from
            // the same register. For example bit1&bit2 generates a 4 four states value and there are Slaves that uses this format,
            // and should be abstracted.
            //we try to parse the bit value.
            bit = Byte.valueOf(configuration.getTuples().getStringProperty(i, "Bit", "-1"));
            
            //TODO: use the number of registers
            //numberOfRegisters= configuration.getTuples().getIntProperty(i,"NumberOfRegisters",1);
            //At this moment the characterEncoding is not necesary
            //characterEncoding=configuration.getTuples().getStringProperty(i,"CharacterEncoding","ASCII");         
            multiplier = configuration.getTuples().getDoubleProperty(i, "Multiplier", 1);
            additive = configuration.getTuples().getDoubleProperty(i, "Additive", 0);
            eventName = configuration.getTuples().getStringProperty(i, "EventName", "Event");

        }

        if (bit != -1) {
            if (registerRange != RegisterRange.HOLDING_REGISTER
                    && registerRange != RegisterRange.INPUT_REGISTER) {
                //throw a bad configuration exception.
            } else {
                modbusLocator = new BinaryLocator(slaveId, registerRange, offset, bit);
                //modbusLocator = new ModbusLocator(slaveId,registerRange,offset,dataType,bit);
            }
        } else if (registerRange == RegisterRange.COIL_STATUS) {
            modbusLocator = new BinaryLocator(slaveId, registerRange, offset);
        } else {
            modbusLocator = new NumericLocator(slaveId, registerRange, offset, dataType);
        }

    }

    private int parseRegisterRange(String stringProperty) {

        //TODO: Check that the RegisterRange is correct
        //TODO: use an enum?
        // Admited values: CoilStatus, InputStatus, HoldingRegister, InputRegister
        switch (stringProperty) {
            case "COIL_STATUS":
                return 1;
            case "INPUT_STATUS":
                return 2;
            case "HOLDING_REGISTER":
                return 3;
            case "INPUT_REGISTER":
                return 4;
            default:
                return -1; //TODO: Handle format error
        }
    }

    private int parseDataType(String stringProperty) {

        switch (stringProperty) {
            case "BINARY":
                return 1;
            case "TWO_BYTE_INT_UNSIGNED":
                return 2;
            case "TWO_BYTE_INT_SIGNED":
                return 3;
            case "FOUR_BYTE_INT_UNSIGNED":
                return 4;
            case "FOUR_BYTE_INT_SIGNED":
                return 5;
            case "FOUR_BYTE_INT_UNSIGNED_SWAPPED":
                return 6;
            case "FOUR_BYTE_INT_SIGNED_SWAPPED":
                return 7;
            case "FOUR_BYTE_FLOAT":
                return 8;
            case "FOUR_BYTE_FLOAT_SWAPPED":
                return 9;
            case "EIGHT_BYTE_INT_UNSIGNED":
                return 10;
            case "EIGHT_BYTE_INT_SIGNED":
                return 11;
            case "EIGHT_BYTE_INT_UNSIGNED_SWAPPED":
                return 12;
            case "EIGHT_BYTE_INT_SIGNED_SWAPPED":
                return 13;
            case "EIGHT_BYTE_FLOAT":
                return 14;
            case "EIGHT_BYTE_FLOAT_SWAPPED":
                return 15;
            case "TWO_BYTE_BCD":
                return 16;
            case "FOUR_BYTE_BCD":
                return 17;
            default:
                return -1; //TODO: Handle format error
        }
    }

    /**
     * Fills the BatchRead with the information of the ModbusLocators
     *
     * @param batchRead the batchRead that is going to be updated
     */
    void updateBatchRead(BatchRead<String> batchRead) {
        //TODO: The Name is not a good id.
        batchRead.addLocator(name, getModbusLocator());
    }

    /**
     * Uses the FreedomModbusLocator to fill an Event with the correct
     * information Used by the ModbusSensor
     *
     * @param results the readed values
     * @param event The event that is filled with the information
     */
    void fillEvent(BatchResults<String> results, GenericEvent event) {
        //GenericEvent event = new GenericEvent(sensor);
        //TODO: We can use a switch over the eventName to send a more specialized event               
        String value;
        if (bit != -1) //it's a bit value
        {
            value = results.getValue(name).toString();
        } else if (dataType == parseDataType("BINARY")) {
            value = results.getValue(name).toString();
        } else //it's a numeric value
        {
            value = Double.toString(getAdjustedValue(Double.parseDouble((results.getValue(name).toString()))));
        }

        event.addProperty(getName(), value);
    }

    void fillProtocolEvent(BatchResults<String> results, ProtocolRead event) {
        String value;
        //System.out.println("value: " + results.getValue(name));
        if (bit != -1) //it's a bit value
        {
            value = results.getValue(name).toString();
        } else if (dataType == parseDataType("BINARY")) {
            value = results.getValue(name).toString();
        } else //it's a numeric value
        {
            value = Double.toString(getAdjustedValue(Double.parseDouble((results.getValue(name).toString()))));
        }
        event.addProperty("object.class", objectClass);
        event.addProperty("object.name", objectName);
        event.addProperty("behavior", objectBehavior);
        event.addProperty("behaviorValue", value);
    }

    /**
     * @return the modbusLocator
     */
    protected BaseLocator getModbusLocator() {
        return modbusLocator;
    }

    /**
     * Transforms the value using the FreedomoticModbusLocator information to
     * translate from/to Freedomotic to Modbus scales
     *
     * @param value the value to transform
     * @return the transformed value
     */
    private double getAdjustedValue(double value) {
        return value * multiplier + additive;
    }

    Object parseValue(Config properties, int i) {
        //TODO: use the DataType to parse the correct type
        String value = properties.getTuples().getStringProperty(0, "value", "0");
        if (bit != -1) {
            return value;
        } else if (dataType == parseDataType("BINARY")) {
            return Boolean.parseBoolean(value);
        } else {
            return getAdjustedValue(Double.parseDouble(value));
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
