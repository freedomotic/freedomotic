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

import com.ftdi.FTD2XX.FT_PROGRAM_DATA;
import com.sun.jna.Memory;

/**
 * Basic EEPROM data.
 * @author Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>
 */
public class EEPROMData {

    final FTD2XX.FT_PROGRAM_DATA.ByReference ft_program_data;

    EEPROMData(FT_PROGRAM_DATA.ByReference ft_program_data) {
        this.ft_program_data = ft_program_data;
    }

    /**
     * Create a new EEPROM data.
     */
    public EEPROMData() {
        ft_program_data = new FT_PROGRAM_DATA.ByReference();
    }

    /**
     * Get vendor id (default: 0x0403)
     * @return VendorId
     */
    public short getVendorId() {
        return ft_program_data.VendorId;
    }

    /**
     * Get product id (default: 0x6001)
     * @return ProductId
     */
    public short getProductId() {
        return ft_program_data.ProductId;
    }

    /**
     * Get manufacturer (default: "FTDI")
     * @return Manufacturer
     */
    public String getManufacturer() {
        return ft_program_data.Manufacturer.getString(0);
    }

    /**
     * Get manufacturer id (default: "FT)
     * @return ManufacturerID
     */
    public String getManufacturerID() {
        return ft_program_data.ManufacturerId.getString(0);
    }

    /**
     * Get description (default: "USB HS Serial Converter")
     * @return Description
     */
    public String getDescription() {
        return ft_program_data.Description.getString(0);
    }

    /**
     * Get serial number (default: "FT000001")
     * @return SerialNumber
     */
    public String getSerialNumber() {
        return ft_program_data.SerialNumber.getString(0);
    }

    /**
     * Get max power (0 < MaxPower <= 500)
     * @return MaxPower (mA)
     */
    public short getMaxPower() {
        return ft_program_data.MaxPower;
    }

    /**
     *  0 = disabled, 1 = enable
     * @return PnP
     */
    public boolean isPnP() {
        return ft_program_data.PnP == 0 ? false : true;
    }

    /**
     * 0 = bus powered, 1 = self powered
     * @return Is SelfPowered
     */
    public boolean isSelfPowered() {
        return ft_program_data.SelfPowered == 0 ? false : true;
    }

    /**
     * 0 = not capable, 1 = capable
     * @return RemoteWakeup
     */
    public boolean isRemoteWakeup() {
        return ft_program_data.RemoteWakeup == 0 ? false : true;
    }

    /**
     * 
     * @param ventorId
     */
    public void setVendorId(short ventorId) {
        ft_program_data.VendorId = ventorId;
    }

    /**
     * 
     * @param productId
     */
    public void setProductId(short productId) {
        ft_program_data.ProductId = productId;
    }

    /**
     * 
     * @param manufacturer
     */
    public void setManufacturer(String manufacturer) {
        Memory memory = new Memory(manufacturer.length() + 1);
        memory.setString(0, manufacturer);
        ft_program_data.Manufacturer = memory;
    }

    /**
     * 
     * @param manufacturerID
     */
    public void setManufacturerID(String manufacturerID) {
        Memory memory = new Memory(manufacturerID.length() + 1);
        memory.setString(0, manufacturerID);
        ft_program_data.ManufacturerId = memory;
    }

    /**
     * 
     * @param description
     */
    public void setDescription(String description) {
        Memory memory = new Memory(description.length() + 1);
        memory.setString(0, description);
        ft_program_data.Description = memory;
    }

    /**
     * 
     * @param serialNumber
     */
    public void setSerialNumber(String serialNumber) {
        Memory memory = new Memory(serialNumber.length() + 1);
        memory.setString(0, serialNumber);
        ft_program_data.SerialNumber = memory;
    }

    /**
     * 
     * @param maxPower
     */
    public void setMaxPower(short maxPower) {
        ft_program_data.MaxPower = maxPower;
    }

    /**
     * 
     * @param pnP
     */
    public void setPnP(boolean pnP) {
        ft_program_data.PnP = (short) (pnP ? 1 : 0);
    }

    /**
     * 
     * @param selfPowered
     */
    public void setSelfPowered(boolean selfPowered) {
        ft_program_data.SelfPowered = (short) (selfPowered ? 1 : 0);
    }

    /**
     * 
     * @param remoteWakeup
     */
    public void setRemoteWakeup(boolean remoteWakeup) {
        ft_program_data.RemoteWakeup = (short) (remoteWakeup ? 1 : 0);
    }

    @Override
    public String toString() {
        return "FTProgramData{"
                + "VendorId=" + getVendorId()
                + ", ProductId=" + getProductId()
                + ", Manufacturer=" + getManufacturer()
                + ", ManufacturerId=" + getManufacturerID()
                + ", Description=" + getDescription()
                + ", SerialNumber=" + getSerialNumber()
                + ", MaxPower=" + getMaxPower()
                + ", PnP=" + isPnP()
                + ", SelfPowered=" + isSelfPowered()
                + ", RemoteWakeup=" + isRemoteWakeup() + '}';
    }
}
