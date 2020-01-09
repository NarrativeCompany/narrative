package org.narrative.common.util;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/19/17
 * Time: 12:32 PM
 */
public class CsvUtil {
    private CsvUtil() {
        throw UnexpectedError.getRuntimeException("Should never construct this utility class.");
    }

    // jw: lets centralize this logic, and whoever is calling shoud already be handling IOExceptions as part of all the other CSV Operations they will be doing.
    public static Map<String, Integer> getHeaderLookup(CSVReader reader) throws IOException {
        String[] headerRow = reader.readNext();
        if (isEmptyOrNull(headerRow)) {
            return null;
        }

        Map<String, Integer> headerLookup = newLinkedHashMap();
        for (int i = 0; i < headerRow.length; i++) {
            headerLookup.put(headerRow[i], i);
        }
        return headerLookup;
    }

    public static Map<String, String> getNextCsvDataLookup(Map<String, Integer> headerLookup, CSVReader reader, boolean includeEmptyValues) throws IOException {
        String[] row = reader.readNext();
        if (isEmptyOrNull(row)) {
            return null;
        }

        if (headerLookup.size() != row.length) {
            throw UnexpectedError.getRuntimeException("Encountered a row within User Import Data with a different column count than the header.  Should never happen with Valid data! expected: " + headerLookup.size() + " but had: " + row.length);
        }

        Map<String, String> userDataLookup = newLinkedHashMap();
        for (Map.Entry<String, Integer> column : headerLookup.entrySet()) {
            String value = row[column.getValue()];

            // jw: only add columns that have a value
            value = IPStringUtil.getTrimmedString(value);
            if (!isEmpty(value) || includeEmptyValues) {
                userDataLookup.put(column.getKey(), value);
            }
        }

        return userDataLookup;
    }
}
