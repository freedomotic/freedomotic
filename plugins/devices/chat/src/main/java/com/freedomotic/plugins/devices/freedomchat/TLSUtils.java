/*
 Copyright (c) 2014 Florian Schmaus
   
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package com.freedomotic.plugins.devices.freedomchat;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jivesoftware.smack.ConnectionConfiguration;

public class TLSUtils {
    public static final String TLS = "TLS";

    /**
     * Accept all SSL/TLS certificates.
     * <p>
     * <b>Warning</b> Use with care. Only use this method if you understand the implications.
     * </p>
     * 
     * @param conf
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void acceptAllCertificates(ConnectionConfiguration conf) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance(TLS);
        context.init(null, new TrustManager[] { new AcceptAllTrustManager() }, new SecureRandom());
        conf.setCustomSSLContext(context);
    }

    public static class AcceptAllTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
            // Nothing to do here
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
            // Nothing to do here
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[]{};
        }
    }
}
