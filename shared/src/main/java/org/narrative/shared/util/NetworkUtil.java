package org.narrative.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Network utilities
 */
public class NetworkUtil {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

    public static String getHostName() {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException e) {
            return "ERROR_UNRESOLVED";
        }
    }
}
