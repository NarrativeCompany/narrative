package org.narrative.common.persistence;

import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.UnexpectedError;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * The database cache:
 * + provides the access point for the application to obtain a database connection.
 * + caches SQL statements issued to the database.
 * + provides utility methods to generate insert, update, delete and select statements.
 */
public class PersistenceUtil {
    private static final NarrativeLogger logger = new NarrativeLogger(PersistenceUtil.class);

    /**
     * turn a string array String[] {"x","y",x") into a string " ('x', 'y', 'z')"
     * useful for some sql statements
     */
    public static String getBracketedCommaSeparatedQuotedValues(Collection values) {
        StringBuffer vals = getCommaSeparatedQuotedValues(values, new StringBuffer(" ("));
        vals.append(") ");
        return vals.toString();
    }

    public static String getBracketedCommaSeparatedValues(Collection values) {
        StringBuffer vals = new StringBuffer(" (");
        getCommaSeparatedValues(values, vals);
        vals.append(") ");
        return vals.toString();
    }

    /**
     * turn a string array String[] {"x","y",x") into a string "'x', 'y', 'z'"
     * useful for some sql statements
     */
    private static StringBuffer getCommaSeparatedQuotedValues(Collection values, StringBuffer toAppendTo) {
        if (null == toAppendTo) {
            toAppendTo = new StringBuffer();
        }
        if (isEmptyOrNull(values)) {
            // empty will return no rows
            toAppendTo.append(" NULL ");
        }
        int i = 0;
        for (Object value : values) {
            toAppendTo.append((i == 0 ? ' ' : ',')).append('\'').append(PersistenceUtil.getSQLEscapedString(value.toString())).append('\'');
            i++;
        }
        return toAppendTo;
    }

    public static StringBuffer getCommaSeparatedValues(Collection values, StringBuffer toAppendTo) {
        if (null == toAppendTo) {
            toAppendTo = new StringBuffer();
        }
        if (values == null || values.isEmpty()) {
            // empty will return no rows
            toAppendTo.append(" NULL ");
        } else {
            int i = 0;
            for (Object value : values) {
                toAppendTo.append((i == 0 ? ' ' : ',')).append(value.toString());
                i++;
            }
        }
        return toAppendTo;
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException sqle) {
            logger.error("Failed trying to close result set", sqle);
        }
    }

    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw UnexpectedError.getRuntimeException("Error closing sql connection", e);
            }
        }
    }

    /**
     * calls rs.next but hides the sql exception in a runtime error.
     */
    public static String getString(ResultSet rs, String columnName) {
        try {
            //IPStringUtil.getStringAfterStrippingUnicodeMarker was killing perfomance and we don't seem to need it anymore
            //return IPStringUtil.getStringAfterStrippingUnicodeMarker(rs.getString(columnName));
            return rs.getString(columnName);
        } catch (SQLException sqle) {
            Debug.assertMsg(logger, false, "Failed trying to get string from result set", sqle);
        }
        return null;
    }

    /**
     * Convert a string so it can be included in an SQL statement,
     * by replacing each apostrophe with a pair of apostrophes
     */

    public static String getSQLEscapedString(String s) {
        if (s == null) {
            return null;
        }
        s = s.replace('\n', ' ').trim();

        if (!s.contains("\'")) {
            return s;
        }
        StringBuilder b = new StringBuilder(s.length() + 10);
        int i = 0;
        int j;
        while ((j = s.indexOf("\'", i)) >= 0) {
            b.append(s.substring(i, j));
            b.append("\'\'");
            i = j + 1;
        }
        b.append(s.substring(i));
        return b.toString();
    }

    /**
     * a utility class for working with mysql databases (used to be on DatabaseType, since
     * they have no equivalent in Oracle, I've moved them into their own class
     * namespace).
     * <p>
     * pb:iw
     */
    public static class MySQLUtils {

        private static final Pattern PERCENT_PATTERN = Pattern.compile("%", Pattern.LITERAL);
        // replace a % with \%
        private static final String PERCENT_REPLACEMENT = "\\\\%";

        private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_", Pattern.LITERAL);
        // replace a _ with \_
        private static final String UNDERSCORE_REPLACEMENT = "\\\\_";

        private static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'", Pattern.LITERAL);
        // replace a ' with \'
        private static final String APOSTROPHE_REPLACEMENT = "\\\\'";

        private static final Pattern DOUBLE_QUOTE_PATTERN = Pattern.compile("\"", Pattern.LITERAL);
        // replace a ' with \'
        private static final String DOUBLE_QUOTE_REPLACEMENT = "\\\\\"";

        // jw: despite what IntelliJ reports, this pattern is actually correct.  Silly IntelliJ
        private static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\", Pattern.LITERAL);
        // replace a \ with \\
        private static final String BACKSLASH_REPLACEMENT = Matcher.quoteReplacement("\\\\");

        /**
         * for any string supplied by an end user that is going to go through a mysql 'like' query,
         * we should run the string through this method to ensure that underscores and percents
         * are properly escaped so as to prevent end users from embedding wildcards in their search strings.
         *
         * @param pattern the pattern to escape wildcards in
         * @return the pattern with wildcard characters escaped.
         */
        public static String getStringAfterEscapingWildcardCharsForHqlLikePattern(String pattern) {
            if (isEmpty(pattern)) {
                return pattern;
            }
            pattern = PERCENT_PATTERN.matcher(pattern).replaceAll(PERCENT_REPLACEMENT);
            pattern = UNDERSCORE_PATTERN.matcher(pattern).replaceAll(UNDERSCORE_REPLACEMENT);
            return pattern;
        }

        public static String getStringAfterEscapingWildcardCharsForSqlLikePattern(String pattern, boolean escapeApostrophes) {
            if (isEmpty(pattern)) {
                return pattern;
            }
            // jw: since this is for SQL, we need to manually escape the backslash to ensure that the input does not artificially close its container.
            // note: we need to do this up front, since the replacements below will be adding backslashes of their own.
            pattern = BACKSLASH_PATTERN.matcher(pattern).replaceAll(BACKSLASH_REPLACEMENT);

            pattern = getStringAfterEscapingWildcardCharsForHqlLikePattern(pattern);
            // jw: its vital that we escape apostrophe's as well.
            if (escapeApostrophes) {
                pattern = APOSTROPHE_PATTERN.matcher(pattern).replaceAll(APOSTROPHE_REPLACEMENT);
            } else {
                pattern = DOUBLE_QUOTE_PATTERN.matcher(pattern).replaceAll(DOUBLE_QUOTE_REPLACEMENT);
            }
            return pattern;
        }

        public static final String DEFAULT_TIMEZONE = "UTC";

        public static String getJDBCURL(String serverName, String databaseName, String userName, String password) {
            // tried url encoded params (IPHTMLUtil.getURLEncodedString(xxx)) but that does not work
            String baseUrl = "jdbc:mysql://" + serverName + "/" + (isEmpty(databaseName) ? "" : databaseName);

            // jw: lets setup the parameters itemized so that its easier to see whats going on.
            Map<String, String> params = new LinkedHashMap<>();
            params.put("characterEncoding", "UTF-8");
            params.put("useUnicode", "true");
            params.put("useLegacyDatetimeCode", "false");
            // bl: adding noAccessToProcedureBodies=true so that we don't have to have
            // access to the mysql database in order to get metadata about stored procedures.
            params.put("noAccessToProcedureBodies", "true");
            params.put("serverTimezone", DEFAULT_TIMEZONE);
            if (!isEmpty(userName)) {
                params.put("user", userName);
            }
            if (!isEmpty(password)) {
                params.put("password", password);
            }

            // jw: explicitly set the useSSL flag to false so that the connection will not be established via SSL (prevents warnings from the connector)
            params.put("useSSL", "false");

            // jw: we cant use the standard IPHTMLUtil.getParametersAsUrl since it ensures that the path will end with a /
            //     which will cause problems with the JDBC driver.
            return baseUrl + "?" + IPHTMLUtil.getParametersAsURLArgs(params);
        }
    }

    public static String getInClause(int argNum) {
        Debug.assertMsg(logger, argNum > 0, "You must specify an argNum of at least 1.  Otherwise this could produce unxepected SQL results");
        StringBuilder sb = new StringBuilder();
        sb.append(" IN (");
        for (int i = 0; i < argNum; i++) {
            sb.append('?');
            if (i < argNum - 1) {
                sb.append(',');
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
