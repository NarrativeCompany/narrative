package org.narrative.common.util;

import org.apache.commons.codec.binary.Base64;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.YesNo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import static org.narrative.common.util.CoreUtils.*;

public class IPUtil {
    public static final int BYTES_PER_KB = 1024;
    public static final int BYTES_PER_MB = 1024 * BYTES_PER_KB;
    public static final int BYTES_PER_GB = 1024 * BYTES_PER_MB;
    public static final int MB_PER_GB = 1024;
    public static Object EMPTY_ARRAY[] = new Object[0];
    private static final NarrativeLogger logger = new NarrativeLogger(IPUtil.class);

    /**
     * Converts a Y or y value to true and N or n value to false.  If an invalid
     * character or string is passed a if(Debug.bDebug) Debug.assert is produced.
     *
     * @return a boolean value indicating either a Y or N was passed.
     * if s is null, returns false
     */
    public static Boolean getBooleanObjectFromString(String s) {
        if (IPStringUtil.isEmpty(s)) {
            return null;
        }
        if (s.charAt(0) == 'Y' || s.charAt(0) == 'y') {
            return Boolean.TRUE;
        }
        if (s.charAt(0) == 'N' || s.charAt(0) == 'n') {
            return Boolean.FALSE;
        }
        if (s.charAt(0) == '1') {
            return Boolean.TRUE;
        }
        if (s.charAt(0) == '0') {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("on")) {
            return Boolean.TRUE;
        }
        return null;
    }

    public static boolean getBooleanFromString(String s) {
        Boolean b = getBooleanObjectFromString(s);
        // need to choose a default.  go with false.
        if (b == null) {
            return false;
        }
        return b.booleanValue();
    }

    public static int getIntFromString(String val, int def) {
        if (IPStringUtil.isEmpty(val)) {
            return def;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static float getFloatFromString(String val, float def) {
        if (IPStringUtil.isEmpty(val)) {
            return def;
        }
        try {
            return Float.valueOf(val);
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static double getDoubleFromString(String val, double def) {
        if (IPStringUtil.isEmpty(val)) {
            return def;
        }
        try {
            return Double.valueOf(val);
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    static public byte[] resizeArray(byte[] array, int iNewBufferSize) {
        if (iNewBufferSize == array.length) {
            return array;
        }
        byte[] bTempBuffer = new byte[iNewBufferSize];
        System.arraycopy(array, 0, bTempBuffer, 0, Math.min(array.length, iNewBufferSize));
        return bTempBuffer;
    }

    /**
     * fetch an int value from an object that is either a bigdecimal
     * or and integer (assuming the value is from a result set)
     */
    public static int getIntFromObject(Object o) {
        if (o instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) o).intValue();
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Double) {
            return ((Double) o).intValue();
        } else if (o instanceof Long) {
            return ((Long) o).intValue();
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof String) {
            return getIntFromString((String) o, -1);
        }
        if (o == null) {
            logger.error("null column in getInt(object) at " + Debug.stackTraceFromException(new Throwable()));

            return 0;
        }
        logger.error("Unknown column type, assuming 0, type = " + o.getClass().getName());
        return 0;
    }

    /**
     * fetch an String value from an object that is either a String or byte[]
     */
    public static String getStringFromObject(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof byte[]) {
            return new String((byte[]) o);
        } else if (o instanceof String) {
            return ((String) o);
        } else if (o instanceof String[]) {
            String[] values = (String[]) o;
            if (values == null) {
                return null;
            } else if (IPStringUtil.isEmpty(values[0])) {
                return null;
            }
            return values[0];
        }

        Debug.assertMsg(logger, false, "Unknown type getting string from " + o.getClass().getName());
        return null;
    }

    public static int getIntFromHexString(String v) {
        if (IPStringUtil.isEmpty(v)) {
            throw UnexpectedError.getRuntimeException("empty");
        }
        try {
            if (v.startsWith("0x")) {
                return Long.valueOf(v.substring(2), 16).intValue();
            }
            if (v.startsWith("#")) {
                return Long.valueOf(v.substring(1), 16).intValue();
            }
            if (v.startsWith("0") && v.length() > 1) {
                return Long.valueOf(v.substring(1), 8).intValue();
            }
            return Long.valueOf(v).intValue();
        } catch (NumberFormatException e) {
            throw UnexpectedError.getRuntimeException("Invalid number", e);
        }
    }

    public static int getIntFromHexString(String s, int def) {
        try {
            return getIntFromHexString(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public static int getRandomNumber(int min, int max) {
        // e.g. 1,2 => 1 + 0.9999*(2-1+1=2) => 1+1 =2
        int i = min + (int) (Math.random() * (max - min + 1));
        return i;
    }

    public static final char VOWELS[] = "aeiou03".toCharArray();
    public static final char CONSONANTS[] = "bdfghjkmnprs5tvwz".toCharArray();

    public static String generatePassword() {
        return generatePassword(6, 10);
    }

    public static String generatePassword(int minPasswordLength, int maxPasswordLength) {
        return generatePassword(((int) (Math.random() * (maxPasswordLength - minPasswordLength))) + minPasswordLength);
    }

    public static String generatePassword(int passwordLength) {
        StringBuffer password = new StringBuffer();
        for (int i = 0; i < passwordLength; i++) {
            password.append(i % 2 == 0 ? CONSONANTS[Math.min((int) (Math.random() * CONSONANTS.length), CONSONANTS.length - 1)] : VOWELS[Math.min((int) (Math.random() * VOWELS.length), VOWELS.length - 1)]);
        }
        return password.toString();
    }

    public static String generateSecurityKey() {
        StringBuilder key = new StringBuilder();

        int characters = (int) (Math.random() * 5 + 10);
        for (int i = 0; i < characters; i++) {
            key.append((char) (Math.random() * 93 + 33));
        }

        return IPStringUtil.getMD5DigestFromString(key.toString());
    }

    public static final String getObjectFieldsAsQueryString(Object o) {
        return getObjectFieldsAsQueryString(o, null);
    }

    public static final String getObjectFieldsAsQueryString(Object o, Set<Field> fieldsToExclude) {
        Map<String, Collection<String>> args = new HashMap<String, Collection<String>>();
        try {
            Field fields[] = IPUtil.getDeclaredFields(o.getClass());
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (fieldsToExclude != null && fieldsToExclude.contains(f)) {
                    continue;
                }
                f.setAccessible(true);
                args.put(f.getName(), Collections.singleton(IPStringUtil.nullSafeToString(f.get(o))));
            }
        } catch (IllegalAccessException iae) {
            Debug.assertMsg(logger, false, "Failed getting debug info for object", iae);
        }
        return IPHTMLUtil.getParametersAsURLArgs(args);
    }

    /**
     * moves a file from a to b
     */
    public static final void doMoveFile(File from, File to) {
        Debug.assertMsg(logger, !to.exists(), "to file already exists, will not overwrite");
        try {
            FileInputStream fis = new FileInputStream(from);
            FileOutputStream fos = new FileOutputStream(to);
            IPIOUtil.doStreamInputToOutputWithBuffering(fis, fos, true);
            from.delete();
        } catch (Throwable t) {
            to.delete();
            Debug.assertMsg(logger, false, "Failed copying file", t);
        }
    }

    /**
     * copies a file from a to b
     */
    public static final void doCopyFile(File from, File to) {
        Debug.assertMsg(logger, !to.exists(), "to file already exists, will not overwrite");
        try {
            FileInputStream fis = new FileInputStream(from);
            FileOutputStream fos = new FileOutputStream(to);
            IPIOUtil.doStreamInputToOutputWithBuffering(fis, fos, true);
            // this is the diff between a copy and a move
            //from.delete();
        } catch (Throwable t) {
            to.delete();
            Debug.assertMsg(logger, false, "Failed copying file", t);
        }
    }

    /**
     * grabs a value from the hastable.  If that value is null or empty,
     * then it returns the current value.
     */
    public static String getValueOrDefaultFromHashtable(Map h, String currentValue, String key) {
        String s = IPUtil.getStringFromObject(h.get(key));
        return IPStringUtil.isEmpty(s) ? currentValue : s;
    }

    public static String getValueOrDefault(String newValue, String currentValue) {
        return IPStringUtil.isEmpty(newValue) ? currentValue : newValue;
    }

    public static YesNo getValueOrDefaultFromHashtable(Map h, YesNo currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof YesNo) {
            return (YesNo) o;
        }
        return YesNo.valueOf(getStringFromObject(o));
    }

    public static boolean getValueOrDefaultFromHashtable(Map h, boolean currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        }
        return IPUtil.getBooleanFromString(getStringFromObject(o));
    }

    public static int getValueOrDefaultFromHashtable(Map h, int currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        }
        return Integer.parseInt((String) o);
    }

    public static float getValueOrDefaultFromHashtable(Map h, float currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof Float) {
            return ((Float) o).floatValue();
        }
        return IPUtil.getFloatFromString((String) o, currentValue);
    }

    public static double getValueOrDefaultFromHashtable(Map h, double currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof Double) {
            return ((Double) o).floatValue();
        }
        return IPUtil.getDoubleFromString((String) o, currentValue);
    }

    public static long getValueOrDefaultFromHashtable(Map h, long currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof Long) {
            return ((Long) o).longValue();
        }
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        }
        return Long.parseLong((String) o);
    }

    public static OID getValueOrDefaultFromHashtable(Map h, OID currentValue, String key) {
        Object o = (Object) h.get(key);
        if (o == null) {
            return currentValue;
        }
        if (o instanceof OID) {
            return (OID) o;
        }
        if (o instanceof Number) {
            return OID.valueOf((Number) o);
        }
        if (o instanceof String) {
            return OID.valueOf((String) o);
        }
        return OID.valueOf(o.toString());
    }

    public static final BitSet COMMADELIMITEDSET = new BitSet();
    static Hashtable s_ianaToJavaEncoding = new Hashtable();
    static Hashtable s_javaToIanaEncoding = new Hashtable();
    public static final String IANA_ISO8859_ENCODING_NAME = "ISO-8859-1";
    public static final String IANA_UTF8_ENCODING_NAME = "UTF-8";
    public static final String IANA_ASCII_ENCODING_NAME = "US-ASCII";
    public static final String IANA_UNICODE_ENCODING = "UTF-16";

    static {
        COMMADELIMITEDSET.set(',');
        {
            String[][] encodingMap = new String[][]{
                    // IANA name, Java name
                    {"US-ASCII", "ASCII"}, {"ISO-8859-1", "ISO8859_1"}, {"ISO-8859-2", "ISO8859_2"}, {"ISO-8859-3", "ISO8859_3"}, {"ISO-8859-4", "ISO8859_4"}, {"ISO-8859-5", "ISO8859_5"}, {"ISO-8859-6", "ISO8859_6"}, {"ISO-8859-7", "ISO8859_7"}, {"ISO-8859-8", "ISO8859_8"}, {"ISO-8859-9", "ISO8859_9"}, {"Big5", "Big5"}, {"windows-1250", "Cp1250"}, {"windows-1251", "Cp1251"}, {"windows-1252", "Cp1252"}, {"windows-1253", "Cp1253"}, {"windows-1254", "Cp1254"}, {"windows-1255", "Cp1255"}, {"windows-1256", "Cp1256"}, {"windows-1257", "Cp1257"}, {"windows-1258", "Cp1258"}, {"EUC-JP", "EUC_JP"}, {"EUC-KR", "EUC_KR"}, {"ISO-2022-CN", "ISO2022CN"}, {"ISO-2022-JP", "ISO2022JP"}, {"ISO-2022-KR", "ISO2022KR"}, {"KOI8-R", "KOI8_R"}, {"TIS-620", "MS874"}, {"Shift_JIS", "SJIS"}, {"UTF-8", "UTF8"}, {"UTF-16", "Unicode"}, {"UTF-16LE", "UnicodeLittle"}, {"UTF-16BE", "UnicodeBig"}

            };

            // Workaround for some VMs (eg Microsoft's)
            // that use 8859_1 rather than ISO8859_1.
            try {
                "".getBytes("ISO8859_1");
            } catch (java.io.UnsupportedEncodingException e) {
                encodingMap[1][1] = "8859_1";
            }
            for (int i = 0; i < encodingMap.length; i++) {
                s_ianaToJavaEncoding.put(encodingMap[i][0].toUpperCase(), encodingMap[i][1]);
                s_javaToIanaEncoding.put(encodingMap[i][1].toUpperCase(), encodingMap[i][0]);
            }
        }
    }

    /**
     * does the same as the equal() method but is usable with null values and
     */
    public static boolean isEqual(Object oldVal, Object newVal) {
        if ((oldVal == null) && (newVal != null) || (oldVal != null) && (newVal == null)) {
            return false;
        }

        if (oldVal == newVal) {
            return true;
        }
        return oldVal.equals(newVal);
    }

    /**
     * returns if the array contains the same values
     */
    public static boolean isArrayEqual(Object oldVal[], Object newVal[]) {
        if ((oldVal == null) && (newVal != null) || (oldVal != null) && (newVal == null)) {
            return false;
        }

        if (oldVal == newVal) {
            return true;
        }
        if (oldVal.length != newVal.length) {
            return false;
        }
        for (int i = 0; i < oldVal.length; i++) {
            if (!IPUtil.isEqual(oldVal[i], newVal[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean is2DArrayEqual(Object oldVal[][], Object newVal[][]) {
        if ((oldVal == null) && (newVal != null) || (oldVal != null) && (newVal == null)) {
            return false;
        }

        if (oldVal == newVal) {
            return true;
        }
        if (oldVal.length != newVal.length) {
            return false;
        }
        for (int i = 0; i < oldVal.length; i++) {
            if (!IPUtil.isArrayEqual(oldVal[i], newVal[i])) {
                return false;
            }
        }
        return true;
    }

    public static String getIanaEncoding(String javaEncoding) {
        if (javaEncoding == null) {
            return null;
        }
        Object o = s_javaToIanaEncoding.get(javaEncoding);
        if (o != null) {
            return (String) o;
        }
        o = s_javaToIanaEncoding.get(javaEncoding.toUpperCase());
        if (o != null) {
            return (String) o;
        }
        return javaEncoding;
    }

    public static String getJavaEncoding(String ianaEncoding) {
        if (ianaEncoding == null) {
            return null;
        }
        Object o = s_ianaToJavaEncoding.get(ianaEncoding);
        if (o != null) {
            return (String) o;
        }
        o = s_ianaToJavaEncoding.get(ianaEncoding.toUpperCase());
        if (o != null) {
            return (String) o;
        }
        return ianaEncoding;
    }

    public static void joinArrays(Object arraya[], Object arrayb[], Object destination[]) {
        int len = (arraya == null ? 0 : arraya.length) + (arrayb == null ? 0 : arrayb.length);
        Debug.assertMsg(logger, len == destination.length, "destination size is differs from a.len + b.len");
        System.arraycopy(arraya, 0, destination, 0, arraya.length);
        System.arraycopy(arrayb, 0, destination, arraya.length, arrayb.length);
    }

    public static Object[] getJoinedArrays(Object arraya[], Object arrayb[]) {
        if (isEmptyOrNull(arraya)) {
            return arrayb == null ? new Object[0] : arrayb;
        }
        if (isEmptyOrNull(arrayb)) {
            return arraya == null ? new Object[0] : arraya;
        }
        Object arrayc[] = new Object[arraya.length + arrayb.length];
        System.arraycopy(arraya, 0, arrayc, 0, arraya.length);
        System.arraycopy(arrayb, 0, arrayc, arraya.length, arrayb.length);
        return arrayc;
    }

    public static byte[] getJoinedByteArrays(byte arraya[], byte arrayb[]) {
        if (arraya == null || arraya.length == 0) {
            return arrayb == null ? new byte[0] : arrayb;
        }
        if (arrayb == null || arrayb.length == 0) {
            return arraya == null ? new byte[0] : arraya;
        }
        byte arrayc[] = new byte[arraya.length + arrayb.length];
        System.arraycopy(arraya, 0, arrayc, 0, arraya.length);
        System.arraycopy(arrayb, 0, arrayc, arraya.length, arrayb.length);
        return arrayc;
    }

    public static boolean is3UTF8Bytes(byte b[]) {
        return b != null && b.length >= 3 && b[0] == 0xef - 0x100 && b[1] == 0xbb - 0x100 && b[2] == 0xbf - 0x100;
    }

    public static boolean is2UnicodeLEBytes(byte b[]) {
        return b != null && b.length >= 2 && b[0] == 0xff - 0x100 && b[1] == 0xfe - 0x100;
    }

    public static boolean is2UnicodeBEBytes(byte b[]) {
        return b != null && b.length >= 2 && b[0] == 0xfe - 0x100 && b[1] == 0xff - 0x100;
    }

    public static byte[] getBytesAfterReplacing2BytePair(byte bytes[], byte matcha, byte matchb, byte replacea, byte replaceb) {
        // cause we're matching two
        int stopLookingIndex = bytes.length - 1;
        int replacementCOunt = 0;
        for (int i = 0; i < stopLookingIndex; i += 2) {
            if (bytes[i] != matcha) {
                continue;
            }
            if (bytes[i + 1] != matchb) {
                continue;
            }
            bytes[i] = replacea;
            bytes[i + 1] = replaceb;
            replacementCOunt++;
        }
        if (replacementCOunt > 0) {
            if (logger.isInfoEnabled()) {
                logger.info("Replaced " + replacementCOunt + " bytes ");
            }
        }
        return bytes;
    }

    public static File getLocalFileSynchronizedOnRemote(File local, File remote) {
        if (local == remote) {
            return local;
        }
        if (local == null) {
            return remote;
        }
        if (remote == null) {
            return local;
        }
        // they're the same file
        if (remote.getAbsolutePath().equals(local.getAbsolutePath())) {
            return local;
        }
        if (!remote.exists()) {
            return local;
        }
        if (local.exists() && (local.lastModified() >= remote.lastModified())) {
            return remote;
        }
        // could do this but we were getting out of sync
        //return local;

        if (remote.exists()) {
            try {
                File dir = new File(local.getParent());
                if (!dir.exists()) {
                    IPIOUtil.mkdirs(dir);
                }
                FileInputStream is = new FileInputStream(remote);
                File outputFile = new File(local.getParent() + "\\" + System.currentTimeMillis() + ".tmp");
                FileOutputStream os = new FileOutputStream(outputFile);
                IPIOUtil.doStreamInputToOutputWithBuffering(is, os, true);
                if (local.exists()) {
                    local.delete();
                }
                if (outputFile.renameTo(local)) {
                    ;
                } else {
                    outputFile.delete();
                    return remote;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Synchronized remote tempFile to local file system: " + remote.getAbsolutePath());
                }
            } catch (Throwable t) {
                if (logger.isInfoEnabled()) {
                    logger.info("File '" + remote.toString() + "' could not be copied to file '" + local.toString() + "'!!!");
                }
            }
        }

        return remote;
    }

    public static Field[] getDeclaredFields(Class c) {
        Class superClass = c.getSuperclass();
        if (superClass == null || superClass.equals(Object.class)) {
            return getDeclaredFields0(c);
        }
        Field superFields[] = getDeclaredFields(superClass);
        Field baseClassFields[] = getDeclaredFields0(c);
        Field returns[] = new Field[superFields.length + baseClassFields.length];
        System.arraycopy(superFields, 0, returns, 0, superFields.length);
        System.arraycopy(baseClassFields, 0, returns, superFields.length, baseClassFields.length);
        return returns;
    }

    private static Field[] getDeclaredFields0(Class c) {
        return c.getDeclaredFields();
    }

    /**
     * returns string array.  each element has the name of a field.  the index of the string
     * is the value of the field (we expect only int fields).  We assert if there is more
     * than one field with the same value.
     */
    public static String[] getFieldNamesFromIntFieldsWhereRetIndexIsIntValue(Field f[]) {
        if (f == null) {
            return IPStringUtil.EMPTY_STRING_ARRAY;
        }
        String ret[] = new String[f.length];
        for (int iFieldIndex = 0; iFieldIndex < f.length; iFieldIndex++) {
            Class fieldType = f[iFieldIndex].getType();
            Class integerType = Integer.TYPE;
            if (!integerType.equals(fieldType)) {
                Debug.assertMsg(logger, false, "Field " + f[iFieldIndex].getName() + " is not an int ,its a " + f[iFieldIndex].getType().getName());
            }
            int iFieldValue = 0;
            try {
                iFieldValue = f[iFieldIndex].getInt(null);
            } catch (IllegalAccessException iae) {
                Debug.assertMsg(logger, false, "failed getting value for " + f[iFieldIndex].getName(), iae);
            }
            if (iFieldValue >= ret.length) {
                String tmp[] = new String[iFieldValue + 1];
                IPUtil.copyIntoArray(ret, tmp);
                ret = tmp;
            }
            Debug.assertMsg(logger, ret[iFieldValue] == null, "There is already a value for index " + iFieldValue + ": " + ret[iFieldValue]);
            ret[iFieldValue] = f[iFieldIndex].getName();
        }
        return ret;
    }

    public static Field[] getFieldsMatchingPrefix(Field f[], String prefix) {
        Debug.assertMsg(logger, !IPStringUtil.isEmpty(prefix), "empty prefix");
        int count = 0;
        Field ret[] = new Field[f.length];
        for (int i = 0; i < f.length; i++) {
            String fieldName = f[i].getName();
            if (!fieldName.startsWith(prefix)) {
                continue;
            }
            ret[count++] = f[i];
        }
        f = new Field[count];
        IPUtil.copyIntoArray(ret, f);
        return f;
    }

    public static Field[] getPublicFields(Field f[]) {
        int count = 0;
        Field ret[] = new Field[f.length];
        for (int i = 0; i < f.length; i++) {
            if (f[i] == null) {
                continue;
            }
            if (!Modifier.isPublic(f[i].getModifiers())) {
                continue;
            }
            // class declaring the field (which is public) is not itself public
            if (!Modifier.isPublic(f[i].getDeclaringClass().getModifiers())) {
                continue;
            }
            ret[count++] = f[i];
        }
        f = new Field[count];
        IPUtil.copyIntoArray(ret, f);
        return f;
    }

    public static Field[] getNonFinalFields(Field f[]) {
        int count = 0;
        Field ret[] = new Field[f.length];
        for (int i = 0; i < f.length; i++) {
            if (Modifier.isFinal(f[i].getModifiers())) {
                continue;
            }
            ret[count++] = f[i];
        }
        f = new Field[count];
        IPUtil.copyIntoArray(ret, f);
        return f;
    }

    public static Field[] getNonStaticFields(Field f[]) {
        int count = 0;
        Field ret[] = new Field[f.length];
        for (int i = 0; i < f.length; i++) {
            if (Modifier.isStatic(f[i].getModifiers())) {
                continue;
            }
            ret[count++] = f[i];
        }
        f = new Field[count];
        IPUtil.copyIntoArray(ret, f);
        return f;
    }

    public static Field[] getNonTransientFields(Field f[]) {
        int count = 0;
        Field ret[] = new Field[f.length];
        for (int i = 0; i < f.length; i++) {
            if (Modifier.isTransient(f[i].getModifiers())) {
                continue;
            }
            ret[count++] = f[i];
        }
        f = new Field[count];
        IPUtil.copyIntoArray(ret, f);
        return f;
    }

    final static public void copyIntoArray(Object source[], Object dest[]) {
        if (source == null || dest == null) {
            return;
        }
        System.arraycopy(source, 0, dest, 0, Math.min(source.length, dest.length));
    }

    /**
     * sleep without worrying about being interrupted, don't use for long durations
     * you may want to be awoken!
     */
    public static void uninterruptedSleep(long ms) {
        if (ms == 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            //ignore
        }
    }

    public static class SleepTest {
        public static void main(String args[]) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    do {
                                              /*try {
                                                  Thread.sleep(100000);
                                              } catch(java.lang.InterruptedException ie) {
                                              }
                                              System.out.println("is Interrupted = " + Thread.currentThread().isInterrupted());
                                              int i = 1;
                                              */
                    } while (true);
                }
            });
            t.start();
            do {
                t.interrupt();
                System.out.println("is Interrupted = " + t.isInterrupted());
                IPUtil.uninterruptedSleep(1000);
            } while (true);
        }
    }

    /**
     * Cooked this fella up to test waiting/notifying/synchronizing.
     * Specifically for some db connection creation code I want to
     * change.  (So that we don't block interminably getting new
     * connections)
     *
     * @author pb
     */
    public static class WaitTest {
        public static void main(String args[]) {
            final Object sync = new Object();
            final byte stopSignal[] = {0};
            final long startTime = System.currentTimeMillis();
            Runnable r1 = new Runnable() {
                public void run() {
                    do {
                        synchronized (sync) {
                            if (logger.isInfoEnabled()) {
                                logger.info("pre wait");
                            }
                            try {
                                sync.wait(1000);
                            } catch (InterruptedException ie) {
                                if (logger.isInfoEnabled()) {
                                    logger.info("interrupted");
                                }
                            }
                            if (logger.isInfoEnabled()) {
                                logger.info("post wait");
                            }
                        }
                        // run for 1 min
                        if (System.currentTimeMillis() - startTime > IPDateUtil.SECOND_IN_MS * 10) {
                            break;
                        }
                    } while (true);
                    stopSignal[0] = 1;
                }
            };
            new Thread(r1, "Running").start();
            Runnable r2 = new Runnable() {
                public void run() {
                    do {
                        synchronized (sync) {
                            sync.notify();
                        }
                        if (stopSignal[0] == 1) {
                            break;
                        }
                        // fast as you can for 30 secs, then slow
                        if (System.currentTimeMillis() - startTime > IPDateUtil.SECOND_IN_MS * 5) {
                            continue;
                        }
                        IPUtil.uninterruptedSleep(1000);
                    } while (true);
                }
            };
            new Thread(r2, "Notifying").start();
        }
    }

    /**
     * find a byte pattern inside a byte array starting from a given offset
     */
    public static int getByteIndex(byte searchThis[], byte forThis[], int startSearchingOffset) {
        if (searchThis == null) {
            return -1;
        }
        if (forThis == null) {
            return -1;
        }
        for (int i = startSearchingOffset; i < searchThis.length - forThis.length + 1; i++) {
            for (int j = 0; j < forThis.length; j++) {
                if (searchThis[i + j] != forThis[j]) {
                    break;
                }
                if (j == forThis.length - 1) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String getMD5DigestFromBytes(byte bytes[]) {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(bytes);
            StringBuilder sb = new StringBuilder();
            // bl: there must be a better way to do this, but this will have to work for now.
            for (byte b : md5Bytes) {
                String hexVal = Integer.toHexString(b & 0xFF);
                // 0-pad the string
                while (hexVal.length() < 2) {
                    hexVal = "0" + hexVal;
                }
                sb.append(hexVal.toUpperCase());
            }
            String ret = sb.toString();
            assert ret.length() == 32 : "Found an MD5 with length!=32? s/" + new String(bytes) + " md5/" + ret;
            return sb.toString();
            // the following two approaches seem to work as well,
            // though i think there is an issue with the sign (+/-) of
            // the result value.
            //return new BigInteger(1,bs).toString(16).toUpperCase();
            //return new BigInteger(bs).toString(16).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw UnexpectedError.getRuntimeException("unable to md5 bytes", e);
        }
    }

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String getHmacSha1DigestFromBytes(byte key[], byte bytes[]) {
        try {
            // Generate secret key for HMAC-MD5
            SecretKeySpec sk = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);

            // Get instance of Mac object implementing HMAC-MD5, and initialize it
            // with the above secret key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(sk);

            byte[] rawHmac = mac.doFinal(bytes);
            byte[] hexBytes = Base64.encodeBase64(rawHmac);

            return new String(hexBytes, "ISO-8859-1");
        } catch (NoSuchAlgorithmException ignore) {
            assert false : "No such algorithm " + HMAC_SHA1_ALGORITHM;
        } catch (InvalidKeyException ignore) {
            throw UnexpectedError.getRuntimeException("Invalid key exception for Hmac SHA1");
        } catch (UnsupportedEncodingException ignore) {

        }
        return "";
    }

//    public static class TestHarness implements RegressionTest.Testable {
//        public String getTestName() {
//            return "IPUtil";
//        }
//        public static void main(String args[]) {
//            Debug.APP_NAME = "IPUtil";
//            performTest();
//        }
//        public void performTest(RegressionTest.TestData td) {
//            performTest();
//        }
//        public static void performTest() {
//            {
//                if (logger.isInfoEnabled()) logger.info( "human readable " + getHumanReadableBytes(5));
//                if (logger.isInfoEnabled()) logger.info( "human readable " + getHumanReadableBytes(1023));
//                if (logger.isInfoEnabled()) logger.info( "human readable " + getHumanReadableBytes(1024));
//                if (logger.isInfoEnabled()) logger.info( "human readable " + getHumanReadableBytes(1024*1024*812+123456));
//                if (logger.isInfoEnabled()) logger.info( "human readable " + getHumanReadableBytes(1024*1024*1024+234567325));
//            }
//            {
//                int i = getByteIndex(null, null, 10);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(null, null, 10) with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{}, null, 10);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{}, null, 10); with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{}, new byte[]{}, 10);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{}, new byte[]{}, 10) with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{1}, new byte[]{1}, 10);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{1}, new byte[]{1}, 10); with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{1}, new byte[]{1}, 0);
//                if (i!=0)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{1}, new byte[]{1}, 0); with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{1}, new byte[]{1,2}, 0);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{1}, new byte[]{1,2}, 0); with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{1,2,3,4}, new byte[]{2,3}, 2);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{1,2,3,4}, new byte[]{2,3}, 2); with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{1,2,3,4}, new byte[]{2,3}, 1);
//                if (i!=1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{1,2,3,4}, new byte[]{2,3}, 1); with " + i);
//            }
//            {
//                int i = getByteIndex(new byte[]{1,2,3,4}, new byte[]{4,5}, 1);
//                if (i!=-1)
//                    Debug.assertMsg(logger, false, "failed getByteIndex(new byte[]{1,2,3,4}, new byte[]{4,5}, 1); with " + i);
//            }
//            {
//                IPUtil.copyIntoArray(new Object[]{"a", "b", "c"}, new Object[1]);
//                IPUtil.copyIntoArray(new Object[]{"a", "b", "c"}, new Object[2]);
//                IPUtil.copyIntoArray(new Object[]{"a", "b", "c"}, new Object[3]);
//                IPUtil.copyIntoArray(new Object[]{"a", "b", "c"}, new Object[4]);
//            }
//            {
//                Hashtable h = new Hashtable();
//                h.put(Boolean.FALSE, Boolean.FALSE);
//                Object o[] = IPUtil.getArrayFromEnumeration(h.keys());
//                Debug.assertMsg(logger, o.length==1, "expect o to be 1");
//                Debug.assertMsg(logger, o[0].equals(Boolean.FALSE), "expect o to be boolean.false");
//            }
//        }
//    }

    /**
     * End of Thread Runnables
     * End of thread runnables are objects that should be executed by at
     * the end of each thread's execution (via a call to IPUtil.AWTAutoShutdown.onEndOfThread()).
     * <p>
     * IPUtil allows different end of thread runnables to be registered
     * with it at runtime.
     * <p>
     * The end of thread runnables can perform such context as freeing up
     * database connections, tidying up JMS resources, and deallocating
     * other per-thread resources.
     * <p>
     * Why have these things?  Using the IPUtil.endOfThread() method is
     * more extensible than manually freeing resources at the end of thread.
     * That way if further end of thread actions need to be executed,
     * the code impact is minimal.   Also, the knowledge of which
     * resources to free up does not have to be spread around all
     * over the place.
     * <p>
     * In OpenTopic xml set up components are used to register end of
     * thread runnables.
     */

    public static class EOXThreadLocal<T extends Runnable> extends ThreadLocal<EndOfX<T>> {
        private EOXThreadLocal() {}

        public EndOfX<T> getEndOfX() {
            EndOfX<T> eox = get();
            if (eox == null) {
                eox = new EndOfX<T>();
                set(eox);
            }
            return eox;
        }
    }

    public static class EndOfX<T extends Runnable> {
        private final boolean isEndOfApp;

        public EndOfX() {
            this(false);
        }

        public EndOfX(boolean endOfApp) {
            isEndOfApp = endOfApp;
        }

        public static final EndOfX<Runnable> endOfThread = new EndOfX<Runnable>();
        /**
         * executed on end of thread and then removed.  e.g. used per instance
         * of photo album settings so that if an ftpConnection is created, it will
         * be cleaned up at the end of the request.
         * <p>
         * Runnables added here, will only be run on the current thread.
         */
        public static final EOXThreadLocal<Runnable> temporaryEndOfThreadThreadLocal = new EOXThreadLocal<Runnable>();
        /*public static EOXThreadLocal temporaryEndOfTaskThreadLocal = new EOXThreadLocal();
        public static EndOfX endOfTask = new EndOfX();*/
        public static final EndOfX<Runnable> endOfAppComing = new EndOfX<Runnable>(true);
        public static final EndOfX<Runnable> endOfApp = new EndOfX<Runnable>(true);

        private class EndOfXRunnable {
            private T r;
            private String name;
        }

        private ArrayList<EndOfXRunnable> ov = new ArrayList<EndOfXRunnable>();

        /**
         * add a runnable.  It will be run in name order.  The
         * goal is to make it so that the resource is not used
         * again after the clean up. e.g. db cleans up, then something
         * does some db activity, then we'll have a resource
         * that won't be cleaned up.
         * <p>
         * here are some sample orderings:
         * <p>
         * EndOfApp:
         * 80 - DatabaseCache
         * 70 - jms
         * 60 - im
         * 50 - debug
         * 40 - task scheduler
         * 30 - threadbucket
         * 20 - queuedispatcherprocessing
         * 10 - activityrecorderbean
         * <p>
         * <p>
         * EndOfAppComing:
         * 30 - activity recorder bean flushed
         * 20 - user requests stopped
         * 00 - threadbucket
         * <p>
         * EndOfTask
         * 80 - DatabaseCache
         * <p>
         * EndOfThread
         * 80 - DatabaseCache
         * 70 - jms
         * 60 - CacheState writeout
         * 55 - ResourceDispenser
         * 10 - IPDispatcher
         */
        public synchronized void addRunnable(String name, T r) {
            assert !isEndOfApp || !endOfAppCalled : "Can't attempt to add an EndOfApp runnable after endOfApp was called!  This could cause issues that may prevent shutdown.  We no longer allow you to add endOfApp runnables during endOfApp processing (or thereafter).";
            Debug.assertMsg(logger, name != null, "null names not allowed");
            Debug.assertMsg(logger, r != null, "null runnables not allowed");
            EndOfXRunnable eoxRunnable = new EndOfXRunnable();
            eoxRunnable.r = r;
            eoxRunnable.name = name;
            ov.add(eoxRunnable);
            Collections.sort(ov, new Comparator<EndOfXRunnable>() {
                public int compare(EndOfXRunnable a, EndOfXRunnable b) {
                    return Sorting.stringComparer.compare(a.name, b.name);
                }
            });
        }

        int executionCount = 0;

        void execute() {
            executionCount++;
            if (ov != null) {
                for (EndOfXRunnable endOfXRunnable : ov) {
                    try {
                        if (executionCount < 10) {
                            // gets a bit tedious after a while, but log some info about what we're doing
                            if (logger.isDebugEnabled()) {
                                logger.debug("Running an endofx task " + endOfXRunnable.name + " (" + endOfXRunnable.r.getClass().getName() + ")");
                            }
                        }
                        endOfXRunnable.r.run();
                    } catch (Throwable t) {
                        try {
                            logger.error("Ignoring problem tidying up a thread", t);
                        } catch (Throwable t2) {
                            // call me paranoid
                        }
                    }
                }
            }
        }
    }

    private static final ThreadLocal<Boolean> isInOnEndOfThread = new ThreadLocal<Boolean>();

    public static void onEndOfThread() {
        Boolean isAlreadyInOnEndOfThread = isInOnEndOfThread.get();
        if (isAlreadyInOnEndOfThread != null && isAlreadyInOnEndOfThread) {
            throw UnexpectedError.getRuntimeException("Coding error!  Can't call onEndOfThread recursively.  Most common problem is calling TaskRunner.doRootGlobalTask from within an onEndOfThread runnable.  Instead, use the UtilityThread or some other mechanism to handle processing.  Also, if it is a temporary end of thread runnable that is causing the problem, then see addTemporaryEndOfThreadThreadLocalForUtilityThread for a quick and easy way to add temporary runnables to the UtilityThread.  Or see PartitionGroup.addEndOfPartitionGroupRunnableForUtilityThread if the problematic thread is in an end of partition group runnable.");
        }
        isInOnEndOfThread.set(Boolean.TRUE);
        try {
            try {
                EndOfX<Runnable> eox = EndOfX.temporaryEndOfThreadThreadLocal.get();
                //logger.info( "Running end of thread runnables, tmp eox? " + (eox!=null));
                if (eox != null) {
                    eox.execute();
                }
            } finally {
                EndOfX.temporaryEndOfThreadThreadLocal.set(null);
            }
            EndOfX.endOfThread.execute();
        } finally {
            isInOnEndOfThread.set(null);
        }
    }
    /*public static void onEndOfTask() {
        EndOfX eox = (EndOfX)EndOfX.temporaryEndOfTaskThreadLocal.get();
        if (eox!=null) {
            eox.execute();
            // so the next use of this thread doesn't have to re-run these context
            EndOfX.temporaryEndOfTaskThreadLocal.set(null);
        }
        EndOfX.endOfTask.execute();
    }*/
    /**
     * does two rounds: first it runs the onEndOfAppComing, then it
     * runs the onEndOfApp runnables
     */

    private static boolean endOfAppCalled = false;

    public static void onEndOfApp() {
        synchronized (IPUtil.class) {
            if (!endOfAppCalled) {
                logger.info("Calling EndOfX.endOfThread(), shutdown step 1/4");
                EndOfX.endOfThread.execute();
                logger.info("Calling EndOfX.endOfAppComing(), shutdown step 2/4");
                EndOfX.endOfAppComing.execute();
                logger.info("Calling EndOfX.endOfThread(), shutdown step 3/4");
                EndOfX.endOfThread.execute();
                logger.info("Calling EndOfX.endOfApp(), shutdown step 4/4");
                EndOfX.endOfApp.execute();
                endOfAppCalled = true;
            }
        }
    }

    static public Object[] getObjectArrayUnion(Object firstSet[], Object secondSet[]) {
        if (secondSet == null) {
            secondSet = new Object[]{};
        }
        if (firstSet == null) {
            firstSet = new Object[]{};
        }
        if (secondSet.length == 0) {
            return firstSet;
        }
        if (firstSet.length == 0) {
            return secondSet;
        }
        Object ret[] = new Object[firstSet.length + secondSet.length];
        // put the two arrays in one array
        System.arraycopy(firstSet, 0, ret, 0, firstSet.length);
        System.arraycopy(secondSet, 0, ret, firstSet.length, secondSet.length);
        return ret;
    }

    static public byte[] getByteArrayUnion(byte firstSet[], byte secondSet[]) {
        if (secondSet == null) {
            secondSet = new byte[]{};
        }
        if (firstSet == null) {
            firstSet = new byte[]{};
        }
        if (secondSet.length == 0) {
            return firstSet;
        }
        if (firstSet.length == 0) {
            return secondSet;
        }
        byte ret[] = new byte[firstSet.length + secondSet.length];
        // put the two arrays in one array
        System.arraycopy(firstSet, 0, ret, 0, firstSet.length);
        System.arraycopy(secondSet, 0, ret, firstSet.length, secondSet.length);
        return ret;
    }

    /**
     * Takes two Object[] containing objects of a type xxx and returns a xxx[].
     */
    static public Object[] getArrayUnion(Object firstSet[], Object secondSet[], Class objectType) {
        if (secondSet == null) {
            secondSet = (Object[]) java.lang.reflect.Array.newInstance(objectType, 0);
        }
        if (firstSet == null) {
            firstSet = (Object[]) java.lang.reflect.Array.newInstance(objectType, 0);
        }
        if (secondSet.length == 0) {
            return firstSet;
        }
        if (firstSet.length == 0) {
            return secondSet;
        }
        Object ret[] = (Object[]) java.lang.reflect.Array.newInstance(objectType, firstSet.length + secondSet.length);
        // put the two arrays in one array
        System.arraycopy(firstSet, 0, ret, 0, firstSet.length);
        System.arraycopy(secondSet, 0, ret, firstSet.length, secondSet.length);
        return ret;
    }

    /**
     * sqlNameToCountTimeSpent tracks sql name to long[]{count, total time spent in query}
     */
    static private Hashtable s_taskNameToCountTimeSpent = new Hashtable();

    static public void logTaskTiming(String taskName, String desc, long startTime) {
        logTaskTiming(taskName, desc, startTime, 30);
    }

    static public void logTaskTiming(String taskName, String desc, long startTime, long onlyOutputAMessageIfTheTaskTookAtLeastThisManyMs) {
        long counts[] = (long[]) s_taskNameToCountTimeSpent.get(taskName);
        if (counts == null) {
            counts = new long[2];
            s_taskNameToCountTimeSpent.put(taskName, counts);
        }
        counts[0]++;
        long duration = (System.currentTimeMillis() - startTime);
        counts[1] += duration;
        // else its just too fast, and logging would probably double the time to do the task
        if (duration > onlyOutputAMessageIfTheTaskTookAtLeastThisManyMs) {
            if (logger.isDebugEnabled()) {
                logger.debug("SQL: " + duration + "ms for '" + taskName + "', detail '" + desc + '\'');
            }
        }
    }

    static public String getTaskTimingXML() {
        StringBuffer sqlXML = new StringBuffer();
        XMLUtil.openTag(sqlXML, "TASK_TIMINGS");
        // clone so no concurrency issues during an insert by another thread
        Hashtable h = (Hashtable) s_taskNameToCountTimeSpent.clone();
        Enumeration sqlEnum = h.keys();
        while (sqlEnum.hasMoreElements()) {
            String sqlName = (String) sqlEnum.nextElement();
            long count[] = ((long[]) h.get(sqlName));
            sqlXML.append("<TASK_TIMING ");
            XMLUtil.addNameEqualsValueAttribute(sqlXML, "name", sqlName);
            XMLUtil.addNameEqualsValueAttribute(sqlXML, "count", Long.toString(count[0]));
            XMLUtil.addNameEqualsValueAttribute(sqlXML, "ms_per_request", Long.toString(count[0] == 0 ? 0 : count[1] / count[0]));
            XMLUtil.addNameEqualsValueAttribute(sqlXML, "total_task_time_ms", Long.toString(count[1]));
            sqlXML.append("/>\n");
        }
        // Add the per action counts to the request profile xml
        XMLUtil.closeTag(sqlXML, "TASK_TIMINGS");
        return sqlXML.toString();
    }

    public static BigDecimal roundBigDecimalToNearestPenny(BigDecimal bd) {
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static String[] getKeysFromHashtable(Hashtable h) {
        if (h == null) {
            return IPStringUtil.EMPTY_STRING_ARRAY;
        }
        String ret[] = new String[h.size()];
        Enumeration aenum = h.keys();
        int i = 0;
        while (aenum.hasMoreElements()) {
            ret[i++] = aenum.nextElement().toString();
        }
        return ret;
    }

    public static class ct {
        public ct() {
            System.out.println("ct constructor");
        }

        static class ct2 extends ct {
            public ct2() {
                System.out.println("ct2 constructor");
            }
        }

        public static void main(String args[]) {
            new ct2();

        }
    }

    /**
     * @return 130=>130 bytes, 2046=> 2 KB, 1024000=> 1 MB, 1,024,000,000 => 1 GB
     */
    public static String getHumanReadableBytes(long bytes) {
        DecimalFormat df = new DecimalFormat("0.#");
        if (bytes >= IPUtil.BYTES_PER_GB) {
            return df.format(bytes * 1.0f / (float) IPUtil.BYTES_PER_GB) + " GB";
        }
        if (bytes >= IPUtil.BYTES_PER_MB) {
            return df.format(bytes * 1.0f / (float) IPUtil.BYTES_PER_MB) + " MB";
        }
        if (bytes >= IPUtil.BYTES_PER_KB) {
            return df.format(bytes * 1.0f / (float) IPUtil.BYTES_PER_KB) + " KB";
        }
        return "" + bytes + " bytes";
    }

    public static void moveDirectory(File fromDir, File toDir) {
        if (fromDir.renameTo(toDir)) {
            // nice and simple
            return;
        }
        // get all the parent dirs in place
        IPIOUtil.mkdirs(toDir);

        // copy the directory and make sure the copy was a success
        boolean wasCopied = IPIOUtil.copyDirectory(fromDir, toDir, true);
        Debug.assertMsg(logger, wasCopied, "failed copying from " + fromDir.getAbsolutePath() + " to " + toDir.getAbsolutePath());
        // delete the temp index directory
        IPIOUtil.deleteDirectory(fromDir);
    }

    /**
     * parse a Locale string. at time of this writing, it doesn't
     * appear that there is a built-in java utility to do this (!).
     *
     * @param localeString the locale string to parse, of the form en_US
     * @return the Locale for the given localeString
     */
    public static Locale parseLocaleString(String localeString) {
        if (IPStringUtil.isEmpty(localeString)) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(localeString, "_");
        String language = st.nextToken();
        if (!st.hasMoreTokens()) {
            return new Locale(language);
        }
        String country = st.nextToken();
        if (!st.hasMoreTokens()) {
            return new Locale(language, country);
        }
        String variant = st.nextToken();
        return new Locale(language, country, variant);
    }

    private static final Map<String, String> FQCN_TO_BINARY_CLASS_NAME = new ConcurrentHashMap<String, String>();

    /**
     * bl: this is really annoying.  we need to use dot separated class names in JSP tags
     * to reference class names.  this follows the JSP spec for what class names
     * should be returned in VariableInfo returned from TagExtraInfo.
     * unfortunately, dot separated class names (for inner classes) won't work
     * with Class.forName().  Class.forName() actually requires the "binary"
     * name.  so, in JSP, we need to use:
     * java.util.Map.Entry
     * but for Class.forName(), we need to use:
     * java.util.Map$Entry.
     * <p>
     * Sun says this is ok:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4378381
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4628117
     * <p>
     * This method is used to convert a proper FQCN to the binary counterpart.
     * This is done purely by java package and class naming conventions.  The string
     * will be tokenized on dots.  All dots will remain up until the first token
     * that starts with an uppercase letter.  After that point, all dots will be replaced
     * with $.
     * <p>
     * Note: the lookup of FQCN -> binary class name is cached so that we don't have
     * to parse the same string multiple times.
     *
     * @param className the fully qualified class name to convert to its binary counterpart
     * @return the binary class name (for use with Class.forName()) for the given FQCN
     */
    public static String getBinaryClassNameFromFullyQualifiedClassName(String className) {
        String ret = FQCN_TO_BINARY_CLASS_NAME.get(className);
        if (ret != null) {
            return ret;
        }

        StringBuilder sb = new StringBuilder();
        boolean foundClass = false;
        StringTokenizer st = new StringTokenizer(className, ".");
        while (st.hasMoreTokens()) {
            String packageOrClassName = st.nextToken();
            if (IPStringUtil.isEmpty(packageOrClassName)) {
                continue;
            }
            sb.append(packageOrClassName);
            // as soon as we find a package/class name that starts with an upper case letter, that means
            // we found the class, so start separating with $.
            if (Character.isUpperCase(packageOrClassName.charAt(0))) {
                foundClass = true;
            }
            if (st.hasMoreTokens()) {
                // separate with dots once we found the class
                if (foundClass) {
                    sb.append("$");
                } else {
                    sb.append(".");
                }
            }
        }

        ret = sb.toString();
        FQCN_TO_BINARY_CLASS_NAME.put(className, ret);
        return ret;
    }

    /**
     * Extremely simple function to simplify the centralize the code needed to read the length
     * of a potentially null array
     *
     * @param array
     * @return
     */
    public static int getArrayLength(Object[] array) {
        if (array == null) {
            return 0;
        }

        return array.length;
    }

    public static String getSerializedPropertiesObject(Properties prop) {
        ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
        try {
            prop.store(fileOut, null);
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed serializing Properties object!", e);
        }
        String props = fileOut.toString();
        return props.substring(props.indexOf(System.getProperty("line.separator")) + 1);
    }

    public static Properties getPropertiesObjectFromSerializedPropertiesString(String propString) {
        Properties ret = new Properties();
        if (IPStringUtil.isEmpty(propString)) {
            return ret;
        }
        try {
            ret.load(new ByteArrayInputStream(propString.getBytes()));
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Failed loading Properties object from Properties string! propString/\"" + propString + "\"", e);
        }
        return ret;
    }

    public static String getClassSimpleName(Class clss) {
        if (clss.isAnonymousClass()) {
            return clss.getName();
        }

        return clss.getSimpleName();
    }

    public static String getClassSimpleNameUnqualified(Class clss) {
        String ret = getClassSimpleName(clss);
        if (clss.isAnonymousClass()) {
            return IPStringUtil.getStringAfterLastIndexOf(ret, ".");
        }

        return ret;
    }
}


/*
This is not foolproof, but will work if the encoding scheme is such that
ASCII-valued octets stand for ASCII characters only at least until the META
element is parsed. Note that there are better ways for a server to obtain
character encoding information, instead of the unreliable META above; see
[NICOL2] for some details and a proposal.
For definiteness, the charset parameter received from the source of the document
 should be considered the most authoritative, followed in order of preference
by the contents of a META element such as the above, and finally the CHARSET
 parameter of the anchor that was followed (if any).

When HTML text is transmitted directly in UCS-2 or UCS-4 form, the question
of byte order arises: does the high-order byte of each multi-byte character
come first or last? For definiteness, this specification recommends that UCS-2
and UCS-4 be transmitted in big-endian byte order (high order byte first),
which corresponds to the established network byte order for two- and four-byte
 quantities, to the ISO 10646 requirement and Unicode recommendation for
 serialized text data and to RFC 1641. Furthermore, to maximize chances of
proper interpretation, it is recommended that documents transmitted as UCS-2
 or UCS-4 always begin with a ZERO-WIDTH NON-BREAKING SPACE character
(hexadecimal FEFF or 0000FEFF) which, when byte-reversed becomes number
FFFE or FFFE0000, a character guaranteed to be never assigned. Thus, a
user-agent receiving an FFFE as the first octets of a text would know
that bytes have to be reversed for the remainder of the text.

There exist so-called UCS Transformation Formats than can be used to
transmit UCS data, in addition to UCS-2 and UCS-4. UTF-7 [RFC1642] and
UTF-8 [UTF-8] have favorable properties (no byte-ordering problem, different
flavours of ASCII compatibility) that make them worthy of consideration,
especially for transmission of multilingual text. Another encoding scheme,
MNEM [RFC1345], also has interesting properties and the capability to transmit
the full UCS. The UTF-1 transformation format of ISO 10646:1993 (registered by
IANA as ISO-10646-UTF-1), has been removed from ISO 10646 by amendment 4, and
should not be used.

*/

/*
public static BufferedImage convertToBufferedImage2(Image img, int wid, int ht, int type){       
    AffineTransform at = AffineTransform.getScaleInstance(1.0f, 1.0f);
    AffineTransformOp ato = new AffineTransformOp( at, null);
    BufferedImage bi = ato.createCompatibleDestImage( img , null );
    return bi;
}
*/
/*
                    PlanarImage pi = JAI.create("fileload", file);
                    BufferedImage i = pi.getAsBufferedImage();
                    //BufferedImage i = ImageIO.read(original);
                    /*
                    {
                        JFrame f = new JFrame("WhiteToTransparent");        
                        JLabel label = new JLabel(new ImageIcon(i));        
                        label.setOpaque(true);        
                        label.setBackground(Color.GREEN);        
                        f.getContentPane().add(label);        
                        f.pack();        
                        f.setLocationRelativeTo(null);        
                        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
                        f.setHiddenForModeration(true);
                    }*/
//Image img = resizeImageToMaximums(i, 100, 100);
/*
BufferedImage bi = convertToBufferedImage(
                                     img,
                                     img.getWidth(null),
                                     img.getHeight(null),
                                     BufferedImage.TYPE_INT_ARGB
                                     //BufferedImage.TYPE_IND
                                     );
                                     */
/*
BufferedImage bi = resizeBufferedImageToMaximums(i, 100, 100);
File f = new File("thumb." + original.getName());
f.getParentFile().mkdirs();
//ImageIO.write(bi, "JPEG", new FileOutputStream(f));
//JAI.create("filestore", img, f.getAbsolutePath(), "jpeg");
*/

/*
{
xx this one worked
    OutputStream out = new FileOutputStream(f);
JPEGEncodeParam jep = new JPEGEncodeParam();
jep.setQuality( 1.0F );

ImageEncoder encoder = ImageCodec.createImageEncoder( "JPEG", out, jep );
encoder.encode( bi );

out.close();
}
*/
/*
{
    //TIFFEncodeParam params = new TIFFEncodeParam(); 
    //params.setCompression(TIFFEncodeParam.COMPRESSION_NONE); 
    JPEGEncodeParam params = new JPEGEncodeParam();
    OutputStream os = new FileOutputStream(f);
    ImageEncoder encoder = ImageCodec.createImageEncoder("JPEG",os,params); 
    os.close();
    encoder.encode(bi); 
}
*/

/*
GradientPaint gp = new GradientPaint(50.0f, 50.0f, Color.blue, 50.0f, 250.0f, Color.green);
g2d.setPaint(gp);
g2d.fillRect(0, 0, wid, ht);
*/
/*
AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,1.0f);
g2.setComposite(ac);
*/
/*
g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                //RenderingHints.VALUE_INTERPOLATION_BICUBIC
                    // RenderingHints.VALUE_INTERPOLATION_BILINEAR 
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                    );
*/
//g2d.setPaint(gp);
//g2d.fillRect(0, 0, wid, ht);

/**
 * Shrinks/scales an Image so that its new height does not exceed maxHeight
 * and width does not exceed maxWidth but the aspect ratio (width/height)
 * remains the same as the original Image.
 *
 * @param image the image to scale
 * @param maxWidth maximum width allowed in the resized image
 * @param maxHeight maximum height allowed in the resized image
 * @return the smaller Image
 * public static Image resizeImageToMaximums(Image image, int maxWidth, int maxHeight) {
 * int orgW = image.getWidth(null);
 * int orgH = image.getHeight(null);
 * int newW;
 * int newH;
 * double scaleFactor = 1;
 * <p>
 * if ( orgW > maxWidth ) {
 * scaleFactor = (double)maxWidth / orgW;
 * }
 * if ( orgH*scaleFactor > maxHeight ) {
 * scaleFactor = (double)maxHeight / orgH;
 * }
 * <p>
 * if ( scaleFactor == 1 )
 * return image;
 * <p>
 * newW = (int)(scaleFactor*orgW);
 * newH = (int)(scaleFactor*orgH);
 * return resizeImage( image, newW, newH );
 * }
 * <p>
 * changes the size of an Image.
 * @param image the image to resize
 * @param newWidth width of the resized image
 * @param newHeight height of the resized image
 * @return the resized image
 * public static Image resizeImage(Image image, int newWidth, int newHeight) {
 * int imageWidth = image.getWidth(null);
 * int imageHeight = image.getHeight(null);
 * Image littleGuy;
 * littleGuy = image.getScaledInstance( newWidth, newHeight
 * // Image.SCALE_SMOOTH ?
 * //, Image.SCALE_FAST
 * // , Image.SCALE_REPLICATE
 * , Image.SCALE_AREA_AVERAGING
 * );
 * return littleGuy;
 * }
 */
/**
 * changes the size of an Image.
 *
 * @param image the image to resize
 * @param newWidth width of the resized image
 * @param newHeight height of the resized image
 *
 * @return the resized image
public static Image resizeImage(Image image, int newWidth, int newHeight) {
int imageWidth = image.getWidth(null);
int imageHeight = image.getHeight(null);
Image littleGuy;
littleGuy = image.getScaledInstance( newWidth, newHeight
// Image.SCALE_SMOOTH ?
//, Image.SCALE_FAST
// , Image.SCALE_REPLICATE
, Image.SCALE_AREA_AVERAGING
);
return littleGuy;
}
 */
//import java.awt.*;
//import java.awt.image.*;
//import javax.imageio.*;
//import java.io.*;
/*
String readFormats[] = ImageIO.getReaderMIMETypes();
String writeFormats[] = ImageIO.getWriterMIMETypes();
System.out.println("Readers: " + 
    Arrays.asList(readFormats));
System.out.println("Writers: " + 
    Arrays.asList(writeFormats));
*/
