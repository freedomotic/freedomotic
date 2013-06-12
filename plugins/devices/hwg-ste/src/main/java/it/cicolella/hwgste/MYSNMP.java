/*
 Copyright FILE Mauro Cicolella, 2012-2013

 This file is part of FREEDOMOTIC.

 FREEDOMOTIC is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 FREEDOMOTIC is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Freedomotic.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.cicolella.hwgste;

import com.adventnet.snmp.beans.SnmpTarget;
import com.adventnet.snmp.snmp2.*;

public class MYSNMP {

    public int SNMP_SET(String IPaddress, int Port, String OID, byte dataType, String SetValue, String Community) {
        SnmpAPI api = new SnmpAPI();
        SnmpSession session = new SnmpSession(api);
        try {
            session.open();
        } catch (SnmpException e) {
            e.printStackTrace();
        }
        SnmpPDU pdu = new SnmpPDU();
        pdu.setCommunity(Community);
        pdu.setCommand(SnmpAPI.SET_REQ_MSG);
        SnmpOID oid = new SnmpOID(OID);
        SnmpVar var = null;
        try {
            var = SnmpVar.createVariable(SetValue, dataType);
        } catch (SnmpException e) {
            return 0;
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        try {
            SnmpPDU result = session.syncSend(pdu);

            if (result == null) {
                return 0;
            }
        } catch (SnmpException e) {
            return 0;
        }


        session.close();
        api.close();
        return 1;
    }

    public String SNMP_GET(String IPaddress, int Port, String OID, String Community) {
        SnmpTarget target = new SnmpTarget();
        target.setTargetHost(IPaddress);
        target.setTargetPort(Port);
        target.setCommunity(Community);
        target.setObjectID(OID);
        String result = target.snmpGet();
        return result;
    }
}
