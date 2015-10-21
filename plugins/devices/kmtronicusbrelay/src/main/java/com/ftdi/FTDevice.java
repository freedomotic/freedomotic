/*
 * The MIT License
 *
 * Copyright 2011 Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ftdi;

import com.sun.jna.Memory;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java class to communicate easily to a FTDI device.
 * @author Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>
 */
public class FTDevice {

    static private final FTD2XX ftd2xx = FTD2XX.INSTANCE;
    private final int devID, devLocationID, flag;
    private final DeviceType devType;
    private int ftHandle;
    private final String devSerialNumber, devDescription;
    private FTDeviceInputStream fTDeviceInputStream = null;
    private FTDeviceOutputStream fTDeviceOutputStream = null;

    private FTDevice(DeviceType devType, int devID, int devLocationID,
            String devSerialNumber, String devDescription, int ftHandle,
            int flag) {
        this.devType = devType;
        this.devID = devID;
        this.devLocationID = devLocationID;
        this.devSerialNumber = devSerialNumber;
        this.devDescription = devDescription;
        this.ftHandle = ftHandle;
        this.flag = flag;
    }

    /**
     * Get device description.
     * @return device description
     */
    public String getDevDescription() {
        return devDescription;
    }

    /**
     * Get device ID.
     * @return device ID
     */
    public int getDevID() {
        return devID;
    }

    /**
     * Get device serial number.
     * @return device serial number
     */
    public String getDevSerialNumber() {
        return devSerialNumber;
    }

    /**
     * Get device type.
     * @return device type.
     */
    public DeviceType getDevType() {
        return devType;
    }

    /**
     * Get device location.
     * @return device location.
     */
    public int getDevLocationID() {
        return devLocationID;
    }

    /**
     * Get device flag.
     * @return flag.
     */
    public int getFlag() {
        return flag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FTDevice)) {
            return false;
        }
        FTDevice eq = (FTDevice) obj;
        return eq.ftHandle == this.ftHandle;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.ftHandle;
        return hash;
    }

    @Override
    public String toString() {
        return "FTDevice{" + "devDescription=" + devDescription
                + ", devSerialNumber=" + devSerialNumber + '}';
    }

    private static void ensureFTStatus(int ftstatus) throws FTD2XXException {
        if (!(ftstatus == FT_STATUS.OK.constant())) {
            throw new FTD2XXException(ftstatus);
        }
    }

    private static FTDevice getXthDevice(int Xth) throws FTD2XXException {
        IntByReference flag = new IntByReference();
        IntByReference devType = new IntByReference();
        IntByReference devID = new IntByReference();
        IntByReference locID = new IntByReference();
        IntByReference ftHandle = new IntByReference();
        Memory devSerNum = new Memory(16);
        Memory devDesc = new Memory(64);

        ensureFTStatus(ftd2xx.FT_GetDeviceInfoDetail(Xth, flag, devType, devID,
                locID, devSerNum, devDesc, ftHandle));

        return new FTDevice(DeviceType.values()[devType.getValue()],
                devID.getValue(), locID.getValue(), devSerNum.getString(0),
                devDesc.getString(0), ftHandle.getValue(), flag.getValue());
    }

    /**
     * Get the connected FTDI devices. It will not contain opened devices.
     * @return List contain available FTDI devices.
     * @throws FTD2XXException If something goes wrong.
     */
    public static List<FTDevice> getDevices() throws FTD2XXException {
        return getDevices(false);
    }

    /**
     * Get the connected FTDI devices.
     * @param isIncludeOpenedDevices Would you like to see opened devices?
     * @return List contain available FTDI devices.
     * @throws FTD2XXException If something goes wrong.
     */
    public static List<FTDevice> getDevices(boolean isIncludeOpenedDevices)
            throws FTD2XXException {
        IntByReference devNum = new IntByReference();

        ensureFTStatus(ftd2xx.FT_CreateDeviceInfoList(devNum));

        ArrayList<FTDevice> devs = new ArrayList<FTDevice>(devNum.getValue());

        for (int i = 0; i < devNum.getValue(); i++) {
            FTDevice device = getXthDevice(i);
            //device is occupied?
            if (isIncludeOpenedDevices) {
                devs.add(device);
            } else {
                if ((device.flag & FTD2XX.FT_FLAGS_OPENED) == 0) {
                    devs.add(device);
                }
            }

        }

        Logger.getLogger(FTDevice.class.getName()).log(Level.INFO,
                "Found devs: {0} (All:{1})",
                new Object[]{devs.size(), devNum.getValue()});

        return devs;
    }

    /**
     * Get the connected FTDI devices. It will not contain opened devices.
     * @param description Filtering option, exact match need.
     * @return List contain available FTDI devices.
     * @throws FTD2XXException If something goes wrong.
     */
    public static List<FTDevice> getDevicesByDescription(String description)
            throws FTD2XXException {
        IntByReference devNum = new IntByReference();

        ensureFTStatus(ftd2xx.FT_CreateDeviceInfoList(devNum));

        ArrayList<FTDevice> devs = new ArrayList<FTDevice>(devNum.getValue());

        for (int i = 0; i < devNum.getValue(); i++) {
            FTDevice device = getXthDevice(i);

            if (((device.flag & FTD2XX.FT_FLAGS_OPENED) == 0)
                    && description.equals(device.devDescription)) {
                devs.add(device);
            }

        }

        Logger.getLogger(FTDevice.class.getName()).log(Level.INFO,
                "Found devs: {0} (All:{1})",
                new Object[]{devs.size(), devNum.getValue()});

        return devs;
    }

    /**
     * Get the connected FTDI devices. It will not contain opened devices.
     * @param serialNumber Filtering option, exact match need.
     * @return List contain available FTDI devices.
     * @throws FTD2XXException If something goes wrong.
     */
    public static List<FTDevice> getDevicesBySerialNumber(String serialNumber)
            throws FTD2XXException {
        IntByReference devNum = new IntByReference();

        ensureFTStatus(ftd2xx.FT_CreateDeviceInfoList(devNum));

        ArrayList<FTDevice> devs = new ArrayList<FTDevice>(devNum.getValue());

        for (int i = 0; i < devNum.getValue(); i++) {
            FTDevice device = getXthDevice(i);

            if (((device.getFlag() & FTD2XX.FT_FLAGS_OPENED) == 0)
                    && serialNumber.equals(device.devSerialNumber)) {
                devs.add(device);
            }

        }

        Logger.getLogger(FTDevice.class.getName()).log(Level.INFO,
                "Found devs: {0} (All:{1})",
                new Object[]{devs.size(), devNum.getValue()});

        return devs;
    }

    /**
     * Get the connected FTDI devices. It will not contain opened devices.
     * @param deviceType Filtering option.
     * @return List contain available FTDI devices.
     * @throws FTD2XXException If something goes wrong.
     */
    public static List<FTDevice> getDevicesByDeviceType(DeviceType deviceType)
            throws FTD2XXException {
        IntByReference devNum = new IntByReference();

        ensureFTStatus(ftd2xx.FT_CreateDeviceInfoList(devNum));

        ArrayList<FTDevice> devs = new ArrayList<FTDevice>(devNum.getValue());

        for (int i = 0; i < devNum.getValue(); i++) {
            FTDevice device = getXthDevice(i);

            if (((device.flag & FTD2XX.FT_FLAGS_OPENED) == 0)
                    && device.devType.equals(deviceType)) {
                devs.add(device);
            }

        }

        Logger.getLogger(FTDevice.class.getName()).log(Level.INFO,
                "Found devs: {0} (All:{1})",
                new Object[]{devs.size(), devNum.getValue()});

        return devs;
    }

    /**
     * Open connection with device.
     * @throws FTD2XXException If something goes wrong.
     */
    public void open() throws FTD2XXException {
        Memory memory = new Memory(16);
        memory.setString(0, devSerialNumber);
        IntByReference handle = new IntByReference();
        ensureFTStatus(ftd2xx.FT_OpenEx(memory, FTD2XX.FT_OPEN_BY_SERIAL_NUMBER,
                handle));
        this.ftHandle = handle.getValue();
    }

    /**
     * Close connection with device.
     * @throws FTD2XXException If something goes wrong.
     */
    public void close() throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_Close(ftHandle));
    }

    /**
     * Set desired baud rate.
     * @param baudRate The baud rate.
     * @throws FTD2XXException If something goes wrong.
     */
    public void setBaudRate(long baudRate) throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetBaudRate(ftHandle, (int) baudRate));
    }

    /**
     * This function sets the data characteristics for the device
     * @param wordLength Number of bits per word 
     * @param stopBits Number of stop bits
     * @param parity Parity
     * @throws FTD2XXException If something goes wrong.
     */
    public void setDataCharacteristics(WordLength wordLength, StopBits stopBits,
            Parity parity) throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetDataCharacteristics(ftHandle,
                (byte) wordLength.constant(), (byte) stopBits.constant(),
                (byte) parity.constant()));
    }

    /**
     * Set the read and write timeouts for the device.
     * @param readTimeout Read timeout in milliseconds.
     * @param writeTimeout Write timeout in milliseconds.
     * @throws FTD2XXException If something goes wrong.
     */
    public void setTimeouts(long readTimeout, long writeTimeout)
            throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetTimeouts(ftHandle, (int) readTimeout,
                (int) writeTimeout));
    }

    /**
     * Sets the flow control for the device.
     * @param flowControl Flow control type.
     * @throws FTD2XXException If something goes wrong.
     */
    public void setFlowControl(FlowControl flowControl) throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetFlowControl(ftHandle,
                (short) flowControl.constant(), (byte) 0, (byte) 0));
    }

    /**
     * Sets the flow control for the device.
     * @param flowControl Flow control type.
     * @param uXon Character used to signal Xon. Only used if flow control is 
     * FT_FLOW_XON_XOFF
     * @param uXoff Character used to signal Xoff.  Only used if flow control is 
     * FT_FLOW_XON_XOFF
     * @throws FTD2XXException If something goes wrong.
     */
    public void setFlowControl(FlowControl flowControl, byte uXon, byte uXoff)
            throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetFlowControl(ftHandle,
                (short) flowControl.constant(), uXon, uXoff));
    }

    /**
     * Set the Data Terminal Ready (DTR) control signal.
     * @param status Status of DTR signal.
     * @throws FTD2XXException If something goes wrong.
     */
    public void setDtr(boolean status) throws FTD2XXException {
        if (status) {
            ensureFTStatus(ftd2xx.FT_SetDtr(ftHandle));
        } else {
            ensureFTStatus(ftd2xx.FT_ClrDtr(ftHandle));
        }
    }

    /**
     * Set the Request To Send (RTS) control signal
     * @param status Status of RTS signal.
     * @throws FTD2XXException If something goes wrong.
     */
    public void setRts(boolean status) throws FTD2XXException {
        if (status) {
            ensureFTStatus(ftd2xx.FT_SetRts(ftHandle));
        } else {
            ensureFTStatus(ftd2xx.FT_ClrRts(ftHandle));
        }
    }

    /**
     * Gets the modem status and line status from the device.
     * @return Modem and line statuses
     * @throws FTD2XXException If something goes wrong.
     */
    public EnumSet<DeviceStatus> getDeviceStatus() throws FTD2XXException {
        IntByReference modstat = new IntByReference();
        ensureFTStatus(ftd2xx.FT_GetModemStatus(ftHandle, modstat));
        return DeviceStatus.parseToEnumset(modstat.getValue());
    }

    /**
     * Gets the number of bytes in the receive queue.
     * @return The number of bytes in the receive queue
     * @throws FTD2XXException If something goes wrong.
     */
    public int getQueueStatus() throws FTD2XXException {
        IntByReference reference = new IntByReference();
        ensureFTStatus(ftd2xx.FT_GetQueueStatus(ftHandle, reference));
        return reference.getValue();
    }

    /**
     * Purge receive or transmit buffers in the device.
     * @param rxBuffer Will rxBuffer be purged?
     * @param txBuffer Will txBuffer be purged?
     * @throws FTD2XXException If something goes wrong.
     */
    public void purgeBuffer(boolean rxBuffer, boolean txBuffer)
            throws FTD2XXException {
        int mask = 0;
        if (rxBuffer) {
            mask |= Purge.PURGE_RX.constant();
        }
        if (txBuffer) {
            mask |= Purge.PURGE_TX.constant();
        }
        ensureFTStatus(ftd2xx.FT_Purge(ftHandle, mask));
    }

    /**
     * Send a reset command to the device.
     * @throws FTD2XXException If something goes wrong.
     */
    public void resetDevice() throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_ResetDevice(ftHandle));
    }

    /**
     * Set the latency timer value.
     * @param timer Latency timer value in milliseconds. 
     * Valid range is 2 – 255.
     * @throws FTD2XXException If something goes wrong.
     * @throws IllegalArgumentException If timer was not in range 2 - 255.
     */
    public void setLatencyTimer(short timer) throws FTD2XXException,
            IllegalArgumentException {
        if (!((timer > 2) && (timer < 255))) {
            throw new IllegalArgumentException("Valid range is 2 – 255!");
        }
        ensureFTStatus(ftd2xx.FT_SetLatencyTimer(ftHandle, (byte) timer));
    }

    /**
     * Get the current value of the latency timer.
     * @return latency timer value.
     * @throws FTD2XXException If something goes wrong.
     */
    public short getLatencyTimer() throws FTD2XXException {
        ByteByReference byReference = new ByteByReference();
        ensureFTStatus(ftd2xx.FT_GetLatencyTimer(ftHandle, byReference));
        return (short) ((short) byReference.getValue() & 0xFF);
    }

    /**
     * Enables different chip modes.
     * @param ucMask Required value for bit mode mask.  This sets up which bits
     * are  inputs and outputs.  A bit value of 0 sets the corresponding pin to 
     * an input, a bit value of 1 sets the corresponding pin to an output. In 
     * the case of CBUS Bit Bang, the upper nibble of this value controls which 
     * pins are inputs and outputs, while the lower nibble controls which of the
     * outputs are high and low.
     * @param bitMode Mode value.
     * @throws FTD2XXException If something goes wrong.
     */
    public void setBitMode(byte ucMask, BitModes bitMode)
            throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetBitMode(ftHandle, ucMask,
                (byte) bitMode.constant()));
    }

    /**
     * Gets the instantaneous value of the data bus.
     * @return instantaneous data bus value
     * @throws FTD2XXException If something goes wrong.
     */
    public BitModes getBitMode() throws FTD2XXException {
        ByteByReference byt = new ByteByReference();
        ensureFTStatus(ftd2xx.FT_GetBitmode(ftHandle, byt));
        return BitModes.parse(byt.getValue());
    }

    /**
     * Set the USB request transfer size.
     * This function can be used to change the transfer sizes from the default 
     * transfer size of 4096 bytes to better suit the application requirements.
     * Transfer sizes must be set to a multiple of 64 bytes between 64 bytes and
     * 64k bytes.
     * When FT_SetUSBParameters is called, the change comes into effect
     * immediately and any data that was held in the driver at the time of the
     * change is lost. Note that, at present, only dwInTransferSize is 
     * supported.
     * @param inTransferSize Transfer size for USB IN request
     * @param outTransferSize Transfer size for USB OUT request
     * @throws FTD2XXException If something goes wrong.
     */
    public void setUSBParameters(int inTransferSize, int outTransferSize)
            throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_SetUSBParameters(ftHandle, inTransferSize,
                outTransferSize));
    }

    /**
     * Program the EEPROM data
     * @param programData EEPROM to program
     * @throws FTD2XXException If something goes wrong.
     */
    public void writeEEPROM(EEPROMData programData)
            throws FTD2XXException {
        ensureFTStatus(ftd2xx.FT_EE_Program(ftHandle,
                programData.ft_program_data));
    }

    /**
     * Read device EEPROM data
     * @return EEPROM data
     * @throws FTD2XXException If something goes wrong.
     */
    public EEPROMData readEEPROM() throws FTD2XXException {
        FTD2XX.FT_PROGRAM_DATA.ByReference ftByReference =
                new FTD2XX.FT_PROGRAM_DATA.ByReference();
        ensureFTStatus(ftd2xx.FT_EE_Read(ftHandle, ftByReference));
        return new EEPROMData(ftByReference);
    }

    /**
     * Get the available size of the EEPROM user area
     * @return available size in bytes, of the EEPROM user area
     * @throws FTD2XXException If something goes wrong.
     */
    public int getEEPROMUserAreaSize() throws FTD2XXException {
        IntByReference size = new IntByReference();
        ensureFTStatus(ftd2xx.FT_EE_UASize(ftHandle, size));
        return size.getValue();
    }

    /**
     * Read the contents of the EEPROM user area
     * @param numberOfBytes Size in bytes, to be read
     * @return User EEPROM content
     * @throws FTD2XXException If something goes wrong.
     */
    public byte[] readEEPROMUserArea(int numberOfBytes)
            throws FTD2XXException {
        IntByReference actually = new IntByReference();
        Memory dest = new Memory(numberOfBytes);
        ensureFTStatus(ftd2xx.FT_EE_UARead(ftHandle, dest, numberOfBytes,
                actually));
        return dest.getByteArray(0, actually.getValue());
    }

    /**
     * Read all contents of the EEPROM user area
     * @return User EEPROM content
     * @throws FTD2XXException If something goes wrong.
     */
    public byte[] readFullEEPROMUserArea()
            throws FTD2XXException {
        int numberOfBytes = getEEPROMUserAreaSize();
        return readEEPROMUserArea(numberOfBytes);
    }

    /**
     * Read all contents of the EEPROM user area as String
     * @return User EEPROM content as String
     * @throws FTD2XXException If something goes wrong.
     */
    public String readFullEEPROMUserAreaAsString() throws IOException {
        IntByReference actually = new IntByReference();
        int numberOfBytes = getEEPROMUserAreaSize();
        Memory dest = new Memory(numberOfBytes);
        ensureFTStatus(ftd2xx.FT_EE_UARead(ftHandle, dest, numberOfBytes,
                actually));
        return dest.getString(0);
    }

    /**
     * Write data into the EEPROM user area
     * @param data byte[] to write
     * @throws FTD2XXException If something goes wrong.
     */
    public void writeEEPROMUserArea(byte[] data) throws FTD2XXException {
        Memory source = new Memory(data.length);
        source.write(0, data, 0, data.length);
        ensureFTStatus(ftd2xx.FT_EE_UAWrite(ftHandle, source, data.length));
    }

    /**
     * Write string into the EEPROM user area
     * @param data byte[] to write
     * @throws FTD2XXException If something goes wrong.
     */
    public void writeEEPROMUserArea(String data) throws FTD2XXException {
        Memory source = new Memory(data.length());
        source.setString(0, data);
        ensureFTStatus(ftd2xx.FT_EE_UAWrite(ftHandle, source, data.length()));
    }

    /**
     * Write bytes to device.
     * @param bytes Byte array to send
     * @param offset Start index
     * @param length Amount of bytes to write
     * @return Number of bytes actually written
     * @throws FTD2XXException If something goes wrong.
     */
    public int write(byte[] bytes, int offset, int length)
            throws FTD2XXException {
        Memory memory = new Memory(length);
        memory.write(0, bytes, offset, length);
        IntByReference wrote = new IntByReference();

        ensureFTStatus(ftd2xx.FT_Write(ftHandle, memory, length, wrote));

        return wrote.getValue();
    }

    /**
     * Write bytes to device.
     * @param bytes Byte array to send
     * @return Number of bytes actually written
     * @throws FTD2XXException If something goes wrong.
     */
    public int write(byte[] bytes) throws FTD2XXException {
        return write(bytes, 0, bytes.length);
    }

    /**
     * Write byte to device.
     * @param b Byte to send (0..255)
     * @return It was success?
     * @throws FTD2XXException  
     */
    public boolean write(int b) throws FTD2XXException {
        byte[] c = new byte[1];
        c[0] = (byte) b;
        return (write(c) == 1) ? true : false;
    }

    /**
     * Read bytes from device.
     * @param bytes Bytes array to store read bytes
     * @param offset Start index.
     * @param lenght Amount of bytes to read
     * @return Number of bytes actually read
     * @throws FTD2XXException If something goes wrong.
     */
    public int read(byte[] bytes, int offset, int lenght)
            throws FTD2XXException {
        Memory memory = new Memory(lenght);
        IntByReference read = new IntByReference();

        ensureFTStatus(ftd2xx.FT_Read(ftHandle, memory, lenght, read));

        memory.read(0, bytes, offset, lenght);

        return read.getValue();
    }

    /**
     * Read bytes from device.
     * @param bytes Bytes array to store read bytes
     * @return Number of bytes actually read
     * @throws FTD2XXException If something goes wrong.
     */
    public int read(byte[] bytes) throws FTD2XXException {
        return read(bytes, 0, bytes.length);
    }

    /**
     * Read a byte from device.
     * @return The byte what read or -1;
     * @throws FTD2XXException 
     */
    public int read() throws FTD2XXException {
        byte[] c = new byte[1];
        int ret = read(c);
        return (ret == 1) ? ((int) c[0] & 0xFF) : -1;
    }

    /**
     * Read given bytes from device.
     * @param number How many bytes do you want to read?
     * @return Read bytes
     * @throws FTD2XXException If something goes wrong.
     */
    public byte[] read(int number)
            throws FTD2XXException {
        byte[] ret = new byte[number];
        int actually = read(ret);
        if (actually != number) {
            byte[] shrink = new byte[actually];
            System.arraycopy(ret, 0, shrink, 0, actually);
            return shrink;
        } else {
            return ret;
        }
    }

    /**
     * Get an InputStream to device.
     * @return InputStream
     */
    public InputStream getInputStream() {
        if (fTDeviceInputStream == null) {
            fTDeviceInputStream = new FTDeviceInputStream(this);
        }
        return fTDeviceInputStream;
    }

    /**
     * Get an OutputStream to device.
     * @return OutputStream
     */
    public OutputStream getOutputStream() {
        if (fTDeviceOutputStream == null) {
            fTDeviceOutputStream = new FTDeviceOutputStream(this);
        }
        return fTDeviceOutputStream;
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        try {
            close();
        } catch (FTD2XXException ex) {
        }
        super.finalize();
    }
}
