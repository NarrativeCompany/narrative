package org.narrative.network.core.quartz.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.Encryption;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * User: barry
 * Date: Mar 3, 2010
 * Time: 1:09:30 PM
 */
public class QuartzInstaller extends GlobalTaskImpl {
    private static final NetworkLogger logger = new NetworkLogger(QuartzInstaller.class);

    @Override
    protected Object doMonitoredTask() {
        Connection conn = getNetworkContext().getGlobalSession().getConnection();

        Scanner s = new Scanner(NetworkRegistry.class.getResourceAsStream("/sql/quartz.sql"));

        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                line = line.trim();
                if (line.length() > 0) {
                    // jw: if encryption is enabled, we need to make sure that we append the encryption directive to the statement.
                    if (Encryption.INSTANCE.isUseMySqlEncryption() && line.toUpperCase().startsWith("CREATE TABLE ")) {
                        line += " ENCRYPTION='Y'";
                    }

                    st.execute(line);
                }
            }
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Failed installing Quartz!", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    logger.warn("error closing sql statement", e);
                }
            }
        }

        return null;
    }
}
