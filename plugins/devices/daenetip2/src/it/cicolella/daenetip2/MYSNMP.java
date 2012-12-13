package it.cicolella.daenetip2;

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
        pdu.setRemoteHost(IPaddress);
        pdu.setRemotePort(Port);
        pdu.setCommunity(Community);
        pdu.setCommand(SnmpAPI.SET_REQ_MSG);
        SnmpOID oid = new SnmpOID(OID);
        SnmpVar var = null;
        try {
            var = SnmpVar.createVariable(SetValue, dataType);
        } catch (SnmpException e) {
            System.out.println("SNMP exception "+e.toString());
            return 0;
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        try {
            SnmpPDU result = session.syncSend(pdu);

            if (result == null) {
                System.out.println("SNMP exception SnmpPDU null");
                return 0;
            }
        } catch (SnmpException e) {
            System.out.println("SNMP exception "+e.toString());
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
