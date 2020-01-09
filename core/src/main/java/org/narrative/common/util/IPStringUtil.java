package org.narrative.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.common.persistence.ObjectPair;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

public class IPStringUtil {
    private static final NarrativeLogger logger = new NarrativeLogger(IPStringUtil.class);

    public static final String EMPTY_STRING_ARRAY[] = new String[]{};
    public static final String EMPTY_2D_STRING_ARRAY[][] = new String[][]{};

    public static final String UTF8_ENCODING = "UTF8";

    /**
     * return true if a string is null or empty string
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static int strLength(String s) {
        return s == null ? 0 : s.length();
    }

    /**
     * Method which takes an object and returns null if its null or the results of its toString()
     *
     * @param object
     * @return
     */
    public static String nullSafeToString(Object object) {
        if (object == null) {
            return null;
        }

        return object.toString();
    }

    /**
     * This function takes a camel case string and converts it to a lowercase string with underscores.
     * thisIsBefore -> this_is_after
     */
    public static String camelCaseToUnderscore(String name) {
        if (name == null) {
            return null;
        }
        StringBuilder newName = new StringBuilder();
        boolean inUpper = true;
        for (int i = 0; i < name.length(); i++) {
            String c = name.substring(i, i + 1);
            if (c.equals(c.toUpperCase())) {
                if (!inUpper) {
                    newName.append('_');
                }
                newName.append(c.toLowerCase());
                inUpper = true;
            } else {
                newName.append(c);
                inUpper = false;
            }
        }
        return newName.toString();
    }

    /**
     * Removes all occurrences of block comments in a string.
     *
     * @param value The string that contains the block comments
     * @return The string without the block comments
     */
    public static String stripBlockComments(String value) {
        StringBuilder ret = new StringBuilder();

        int pos = 0;
        while (true) {
            //find the next start comment based on the current pos
            int startComment = value.indexOf("/*", pos);
            if (startComment == -1) {
                ret.append(value.substring(pos));
                break;
            } else {
                ret.append(value.substring(pos, startComment));
            }

            //find the end of the comment
            int endComment = value.indexOf("*/", startComment);

            //if no end then assume end of file
            if (endComment == -1) {
                break;
            }

            pos = endComment + 2;
        }

        return ret.toString();
    }

    /**
     * ensures value is no longer than size characters
     */
    public static String getTruncatedString(String value, int size) {
        if (value == null) {
            return null;
        }
        if (value.length() <= size) {
            return value;
        }
        return value.substring(0, size);
    }

    /**
     * Takes a string and truncates it to a specific length with a ... truncating the middle
     * Ex: string="http://site.com/yaml/classic_6_7_2.yaml" (size 45)
     * leftLength=25
     * rightLength=10
     * Result:"http://site.com/yaml/u...6_7_2.yaml"
     *
     * @param string      The string we need to appreviate if it is too long
     * @param leftLength  The length of the right side of the ...
     * @param rightLength the legnth of the left side of the ...
     * @return The potentially abbreviated String.
     */
    public static String getTruncatedString(final String string, final int leftLength, final int rightLength) {
        // is the string smaller than what we are worried about?
        if (string == null || string.length() <= leftLength + rightLength + 3) {
            return string;
        }

        StringBuilder finalString = new StringBuilder();

        // Add the first part and the ... middle
        finalString.append(string.substring(0, leftLength));
        finalString.append("...");

        // Add the part to the right of the ...
        finalString.append(string.substring(string.length() - rightLength));

        return finalString.toString();
    }

    private static final Pattern WORD_PATTERN = Pattern.compile("([^\\s]*)([$|\\s])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * Truncate a string to the specified number of characters, but don't break in the middle of a word unless
     * the string passed in is only 1 word and longer then specified number of characters.
     * add an ellipsis "..." if string was truncated.
     *
     * @param value String to truncate
     * @param size  size to truncate to
     * @return truncated string
     */
    public static String getStringTruncatedToEndOfWord(String value, int size) {
        if (value == null) {
            return null;
        }
        if (value.length() <= size) {
            return value;
        }

        StringBuffer buffer = new StringBuffer();
        int remainingLength = size - 3;
        Matcher matcher = WORD_PATTERN.matcher(value);
        while (matcher.find()) {
            int endOfWord = matcher.end(1);
            if (endOfWord >= remainingLength) {
                if (buffer.length() > 0) {
                    // strip off the last space in this case.
                    buffer.deleteCharAt(buffer.length() - 1);
                }
                break;
            } else if ((endOfWord + 1) == remainingLength) {
                // the word ends at the position we need to truncate to.  great!  just append the word, and we're done.
                matcher.appendReplacement(buffer, "$1");
                break;
            }
            // keep appending the replacement until there is no more room.
            matcher.appendReplacement(buffer, "$1$2");
        }

        //adding this so we at least return something
        if (buffer.length() == 0) {
            buffer.append(value.substring(0, remainingLength));
        }

        // jw: since this is so generically used (for API, Email, Front End, lets use standard ellipses to be safe).
        return getStringWithEllipsis(buffer.toString(), false);
    }

    private static final Pattern ENDS_WITH_PUNCTUATION_PATTERN = Pattern.compile(".*[\\.\\!\\?]$");

    public static String getStringWithEllipsis(String value) {
        return getStringWithEllipsis(value, true);
    }

    public static String getStringWithEllipsis(String value, boolean useHtmlEllipses) {
        if (isEmpty(value)) {
            return value;
        }

        // jw: we want to only include the ellipsis if the string does not end with punctuation.
        if (ENDS_WITH_PUNCTUATION_PATTERN.matcher(value).find()) {
            return value;
        }

        return newString(value, useHtmlEllipses ? IPHTMLUtil.ELLIPSES_HTML_CODE : "...");
    }

    /**
     * Gets a simple CSV String,  does not handle any escaping of existing ,'s from the values
     *
     * @param values
     * @return
     */
    public static StringBuffer getCommaSeparatedList(Collection values) {
        return getSeparatedList(values, ",");

    }

    public static StringBuffer getSeparatedList(Collection values, String separator) {
        return getSeparatedList(values, separator, false);
    }

    public static StringBuffer getSeparatedList(Collection values, String separator, boolean excludeBlanks) {
        StringBuffer vals = new StringBuffer();
        if (isEmptyOrNull(values)) {
            return vals;
        }

        int i = 0;
        for (Object value : values) {
            if (excludeBlanks && (value == null || value.toString().trim().length() == 0)) {
                continue;
            }
            String str = value != null ? value.toString() : "";
            if (i > 0) {
                vals.append(separator);
            }
            vals.append(str);
            i++;
        }
        return vals;
    }

    /**
     * does a binary search of the given strings.  returns a value less than zero if not found.
     * nb: not necessarily -1 so check for index < 0!
     */
    public static int getIndexOfStringInArray(String sortedStringsToSearchIn[], String stringToFind) {
        if (sortedStringsToSearchIn == null || sortedStringsToSearchIn.length < 0) {
            return -1;
        }
        Sorting.Comparer stringComparer = Sorting.getInstance().getComparer(String.class);
        int ret = Sorting.binarySearch(sortedStringsToSearchIn, stringToFind, stringComparer);
        return ret;
    }

    /**
     * Returns true if the string passed in can be parsed as a number.
     *
     * @param numberString
     * @return
     */
    public static boolean isNumber(String numberString) {
        if (IPStringUtil.isEmpty(numberString)) {
            return false;
        }

        try {
            Double.parseDouble(numberString);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static Number getAsNumberSafe(String numberString) {
        if (IPStringUtil.isEmpty(numberString)) {
            return new Integer(0);
        }

        try {
            return Double.parseDouble(numberString);
        } catch (NumberFormatException e) {
            return new Integer(0);
        }
    }

    public static String repeatString(String string, int times) {
        StringBuilder sb = new StringBuilder();
        repeatString(sb, string, times);
        return sb.toString();
    }

    public static void repeatString(Appendable sb, String string, int times) {
        try {
            for (int i = 0; i < times; i++) {
                sb.append(string);
            }
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    /**
     * flag==true returns set1 xor set2
     * flag==false returns set1 AND set2
     *
     */
//    public static class IPStringUtilTestCases implements RegressionTest.Testable {
//        public String getTestName() {
//            return "IPStringUtilTestCases";
//        }
//        public void performTest(RegressionTest.TestData td) {
//
//            String origReplExpected[][] = {
//                {"xaxaxax","a","b","xbxbxbx"}
//                , {"","","",""}
//                , {null,null,null,null}
//                , {null,"xxx","yyy", null}
//                , {"","xxx","yyy", ""}
//                , {"a","a","b","b"}
//                , {"aaa","a","b","bbb"}
//                , {"a","a",null,"null"}
//                , {"aaa","a","bbb","bbbbbbbbb"}
//                , {"aaaaaaaaa","aaa","b","bbb"}
//            };
//            for(int i=0;i<origReplExpected.length;i++) {
//                String out = IPStringUtil.getStringAfterSubstitutingAllOccurrences(origReplExpected[i][0]
//                                                                                  , origReplExpected[i][1]
//                                                                                  , origReplExpected[i][2]);
//                Debug.assertMsg(logger, IPUtil.isEqual(out, origReplExpected[i][3]), "not what we expected.  expected '" + origReplExpected[i][3]
//                                                                             + "', got '"  + out + "' for input of replace '" + origReplExpected[i][1]
//                                                                             + "' with '" + origReplExpected[i][2] + "'  in '" + origReplExpected[i][0]
//                                                                             +"'");
//            }
//            {
//                byte b[] = {1,2,3};
//                if(logger.isInfoEnabled()) logger.info( "byte array = " + IPStringUtil.getBytesAsString(b, "test", true));
//            }
//            {
//                //eg. {"foo", "bar"}, 1, 1 => {"bar"}
//                {
//                    String s[] = getStringArraySubset(new String[]{"foo", "bar"}, 1, 1);
//                    Debug.assertMsg(logger, s.length==1 && s[0].equals("bar"), "Failed getStringArraySubject: {\"foo\", \"bar\"}, 1, 1 => {\"bar\"}");
//                }
//                //eg. {"foo", "bar"}, 0, 1 => {"foo"}
//                {
//                    String s[] = getStringArraySubset(new String[]{"foo", "bar"}, 0, 1);
//                    Debug.assertMsg(logger, s.length==1 && s[0].equals("foo"), "Failed getStringArraySubject: {\"foo\", \"bar\"}, 0, 1 => {\"foo\"}");
//                }
//                //eg. {"foo", "bar"}, 1, 55 => {"bar"}
//                {
//                    String s[] = getStringArraySubset(new String[]{"foo", "bar"}, 1, 55);
//                    Debug.assertMsg(logger, s.length==1 && s[0].equals("bar"), "Failed getStringArraySubject: {\"foo\", \"bar\"}, 1, 55 => {\"bar\"}");
//                }
//                //eg. {"foo", "bar"}, 2, 55 => {}
//                {
//                    String s[] = getStringArraySubset(new String[]{"foo", "bar"}, 2, 55);
//                    Debug.assertMsg(logger, s.length==0, "Failed getStringArraySubject: {\"foo\", \"bar\"}, 2, 55 => {}");
//                }
//
//            }
//            {
//                String set1[] = {"1","2","3"};
//                String set2[] = {"1","2"};
//                String result[] = getStringsInSet1NotInSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"3"}), "Failed getStringsInSet1NotInSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInSet1NotInSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            {
//                String set1[] = {"1","2","3"};
//                String set2[] = {"1","2"};
//                String result[] = getStringsInBothSet1AndSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"1", "2"}), "Failed getStringsInBothSet1AndSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInBothSet1AndSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//
//            /*
//            {
//                String array1[] = new String[]{"1", "2", "3"};
//                String array2[] = new String[]{"a", "b", "c"};
//                String result[][] = get2DArrayFrom2Arrays(array1, array2);
//                if(!IPUtil.is2DArrayEqual(result, new String[][]{{"1","a"}, {"2", "b"}, {"3", "c"}})) {
//                    System.err.println("Failed get2DArrayFrom2Arrays(set1, set2)");
//                } else {
//                    System.out.println("Succeeded get2DArrayFrom2Arrays");
//                }
//            }
//            // This is a redundent code block,  look up two code blocks
//            {
//                String set1[] = {"1","2","3"};
//                String set2[] = {"1","2"};
//                String result[] = getStringsInSet1NotInSet2(set1, set2);
//                if(!IPUtil.isArrayEqual(result, new String[]{"3"})) {
//                    System.err.println("Failed getStringsInSet1NotInSet2(set1, set2)");
//                } else {
//                    System.out.println("Succeeded getStringsInSet1NotInSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//                }
//            }
//            */
//            {
//                StringPair stringPairs[] = new StringPair[]{new StringPair("1","a")
//                    , new StringPair("2", "b"), new StringPair("1", "c")};
//                String[] result = getBStringsFromStringPairWhereAMatches(stringPairs, "1");
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"a", "c"}), "Failed getArrayOfBsMatchingAsFromStringPairArray(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getArrayOfBsMatchingAsFromStringPairArray");
//            }
//            {
//                String set1[] = {};
//                String set2[] = {"1","2"};
//                String result[] = getStringsInSet1NotInSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{}), "Failed getStringsInSet1NotInSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInSet1NotInSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            {
//                String set1[] = {"1","2","3"};
//                String set2[] = {};
//                String result[] = getStringsInSet1NotInSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"1","2","3"}), "Failed getStringsInSet1NotInSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInSet1NotInSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            {
//                String set1[] = {"1","2","3"};
//                String set2[] = {"1","2"};
//                String result[] = getStringsInBothSet1AndSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"1", "2"}), "Failed getStringsInBothSet1AndSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInBothSet1AndSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            {
//                String set1[] = {};
//                String set2[] = {"1"};
//                String result[] = getStringsInBothSet1AndSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{}), "Failed getStringsInBothSet1AndSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInBothSet1AndSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//
//            {
//                String set1[] = {};
//                String set2[] = {};
//                String result[] = getStringsInBothSet1AndSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{}), "Failed getStringsInBothSet1AndSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInBothSet1AndSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            {
//                String set1[] = {"1"};
//                String set2[] = {};
//                String result[] = getStringsInBothSet1AndSet2(set1, set2);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{}), "Failed getStringsInBothSet1AndSet2(set1, set2)");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getStringsInBothSet1AndSet2, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            // Test the upperCaser
//            {
//                String set[] = {"one","Two",null,"Four fivE"};
//                String result[] = IPStringUtil.getUpperCaseStrings(set);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"ONE", "TWO", null, "FOUR FIVE"})
//                                , "Failed getUpperCaseStrings, got:" + IPStringUtil.getCommaSeparatedList(result)
//                                  + " expected:ONE,TWO,null,FOUR FIVE");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getUpperCaseStrings, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            // Test the trim String
//            {
//                String dataAndReturn[][] = {{"a","a"}
//                    ,{"a a","a a"}
//                    ,{"a ","a"}
//                    ,{" a ","a"}
//                    ,{" a\n","a"}
//                    ,{" a\na ","a\na"}
//                    ,{null,null}
//                };
//                for(int i = 0; i < dataAndReturn.length; i++) {
//                    String result = IPStringUtil.getTrimmedString(dataAndReturn[i][0]);
//                    Debug.assertMsg(logger, IPUtil.isEqual(result,dataAndReturn[i][1])
//                                    , "getTrimmedString result:" + result + " is not what was expected"
//                                      + dataAndReturn[i][1]);
//                }
//            }
//            // Test the lowerCaser
//            {
//                String set[] = {"one","Two",null,"Four fivE"};
//                String result[] = IPStringUtil.getLowerCaseStrings(set);
//                Debug.assertMsg(logger, IPUtil.isArrayEqual(result, new String[]{"one", "two", null, "four five"})
//                                , "Failed getLowerCaseStrings, got:" + IPStringUtil.getCommaSeparatedList(result)
//                                  + " Expected:one,two,null,four five");
//                if(logger.isInfoEnabled()) logger.info( "Succeeded getLowerCaseStrings, result = " + IPStringUtil.getCommaSeparatedList(result));
//            }
//            // todo:->?? Still need to add tests for:
//            //           getTruncatedString
//            //           getStringFromBytesWithUnknownEncoding
//            //           getCommaSeparatedList - This needs to be one of the first tests
//            //                                   because we use it in here
//            //           more...
//        }
//    }

    /**
     * sort the string array
     */
    public static String[] getSortedStringArray(String array[]) {
        if (array == null || array.length < 2) {
            return array;
        }
        Sorting sorter = Sorting.getInstance();
        Sorting.Comparer comp = sorter.getComparer(String.class);
        array = (String[]) Sorting.getSortedArray(array, comp, String.class);
        return array;
    }

    /**
     * getStringAfterStripFromEnd)"foobar", "bar") returns "foo"
     * getStringAfterStripFromEnd)"foobar", "xxx") returns "foobar"
     */
    public static String getStringAfterStripFromEnd(String value, String stringToStripFromEnd) {
        if (value == null) {
            return value;
        }
        int message_end = value.length() - stringToStripFromEnd.length();
        if (message_end < 0) {
            return value;
        }
        if (!value.endsWith(stringToStripFromEnd)) {
            return value;
        }
        String stripped = 0 == message_end ? "" : value.substring(0, message_end);
        return stripped;
    }

    public static String getStringAfterStripFromStart(String value, String stringToStripFromStart) {
        if (value == null) {
            return value;
        }
        if (!value.startsWith(stringToStripFromStart)) {
            return value;
        }
        return value.substring(stringToStripFromStart.length());
    }

    /**
     * e.g. getStringAfterStrippingEverythingBeforeText("this is a string here", "a string")
     * => "a string here".
     */
    public static String getStringAfterStrippingEverythingBeforeText(String value, String stringToStripEverythingBefore) {
        return getStringAfterStrippingEverythingBeforeText(value, stringToStripEverythingBefore, false);
    }

    public static String getStringAfterStrippingEverythingBeforeText(String value, String stringToStripEverythingBefore, boolean inclusiveStrip) {
        if (value == null) {
            return "";
        }
        if (stringToStripEverythingBefore == null || IPStringUtil.isEmpty(stringToStripEverythingBefore)) {
            return value;
        }
        int i = value.indexOf(stringToStripEverythingBefore);
        if (i < 0) {
            return value;
        }
        int stripPos = i - 1;
        if (inclusiveStrip) {
            stripPos += stringToStripEverythingBefore.length();
        }
        return IPStringUtil.getStringAfterTruncatingToPosition(value, stripPos);
    }

    public static String getStringAfterLastIndexOf(String value, String stringToStripAfter) {
        return getStringAfterLastIndexOf(value, stringToStripAfter, true);
    }

    public static int getLastIndexOfStringInString(String value, String stringToFind) {
        if (IPStringUtil.isEmpty(stringToFind)) {
            return -1;
        }
        int i = -1;
        do {
            int j = value.indexOf(stringToFind, i + 1);
            if (j < 0) {
                break;
            }
            i = j;
        } while (true);
        return i;
    }

    public static String getStringAfterLastIndexOf(String value, String stringToStripAfter, boolean returnTheOriginalStringIfTheStripAfterNotFound) {
        if (value == null) {
            return "";
        }
        if (stringToStripAfter == null || IPStringUtil.isEmpty(stringToStripAfter)) {
            return value;
        }

        int i = getLastIndexOfStringInString(value, stringToStripAfter);

        if (i < 0) {
            return returnTheOriginalStringIfTheStripAfterNotFound ? value : "";
        }
        int stripToPos = i - 1;
        stripToPos += stringToStripAfter.length();
        return IPStringUtil.getStringAfterTruncatingToPosition(value, stripToPos);
    }

    public static String getStringBeforeLastIndexOf(String value, String stringToStripAfter) {
        if (value == null) {
            return "";
        }
        if (stringToStripAfter == null || IPStringUtil.isEmpty(stringToStripAfter)) {
            return value;
        }

        int i = getLastIndexOfStringInString(value, stringToStripAfter);
        if (i == 0) {
            return "";
        }
        if (i < 0) {
            return value;
        }
        return IPStringUtil.getStringAfterTruncatingFromPosition(value, i);
    }

    /**
     * e.g. getStringAfterStrippingEverythingBeforeInclusive("this is a string here", "a string ")
     * => "here".
     */
    public static String getStringAfterStrippingEverythingUpToEndOfText(String value, String stringToStripEverythingBefore) {
        if (value == null) {
            return "";
        }
        if (stringToStripEverythingBefore == null || IPStringUtil.isEmpty(stringToStripEverythingBefore)) {
            return value;
        }
        int i = value.indexOf(stringToStripEverythingBefore);
        if (i < 0) {
            return value;
        }
        value = IPStringUtil.getStringAfterTruncatingToPosition(value, i - 1 + stringToStripEverythingBefore.length());
        return value;

    }

    /**
     * replace the first occurrence of replaceThis in value with withThis
     */
    public static String getStringAfterSubstitution(String value, String replaceThis, String withThis) {
        return getStringAfterSubstitution0(value, replaceThis, withThis, false, 0, -1);
    }

    public static String getStringAfterSubstitutingAllOccurrences(String value, String replaceThis, String withThis) {
        return getStringAfterSubstitution0(value, replaceThis, withThis, true, 0, -1);
    }

    private static String getStringAfterSubstitution0(String value, String replaceThis, String withThis, boolean replaceAllOccurrences, int positionToStartReplaceSearch, int positionToStopReplaceSearch) {
        //pm:making some tweeks to this method to improve performance
        if (value == null)       //these were calling IPStringUtil.isEmpty() but that was the majority of this methods time,
        {
            return value;       //and 99.9% of the time, the args were not null and not empty string
        }
        if (replaceThis == null) //so instead I'm doing a simple null check and the empty case will be handled
        {
            return value;       //during the length check below
        }
        int i = 0;
        int replaceThisIndex = positionToStartReplaceSearch;
        int replaceThisLen = replaceThis.length();
        int valueLen = value.length();
        if (replaceThisIndex >= valueLen) {
            return value;
        }
        if (positionToStopReplaceSearch < 0) {
            positionToStopReplaceSearch = valueLen;
        }
        StringBuilder buf = new StringBuilder((int) (valueLen * 1.2));
        int firstOriginalIndexNotUsed = 0;
        char valueCharArray[] = value.toCharArray();
        do {
            replaceThisIndex = value.indexOf(replaceThis, replaceThisIndex);
            boolean isMatch = replaceThisIndex > -1 && replaceThisIndex < positionToStopReplaceSearch;
            // first iteration, no change
            if (!isMatch && i == 0) {
                return value;
            }

            // end otherwise
            if (!isMatch || (!replaceAllOccurrences && i > 0)) {
                int lenRemaining = value.length() - firstOriginalIndexNotUsed;
                if (lenRemaining > 0) {
                    buf.append(valueCharArray, firstOriginalIndexNotUsed, lenRemaining);
                }
                return buf.toString();
            }

            if (replaceThisIndex > firstOriginalIndexNotUsed) {
                buf.append(valueCharArray, firstOriginalIndexNotUsed, replaceThisIndex - firstOriginalIndexNotUsed);
            }
            buf.append(withThis);
            firstOriginalIndexNotUsed = replaceThisIndex + replaceThisLen;
            replaceThisIndex += replaceThis == null ? 1 : replaceThis.length();
            i++;
        } while (true);
    }

    /**
     * e.g. original = "1 xyz 2 xyz 3 xyz" marker ="2", replaceThis ="xyz", withThis ="abc" returns "1 xyz 2 abc 3 xyz"
     * added specifically so I can search/replace some text in xml
     */
    public static String getStringAfterReplacingAfterMarker(String original, String start_marker, String end_marker, String replaceThis, String withThis) {
        if (original == null) {
            return "";
        }
        if (IPStringUtil.isEmpty(start_marker)) {
            return original;
        }
        int iStartMarker = original.indexOf(start_marker);
        if (iStartMarker < 0) {
            return original;
        }
        int iReplaceThis = original.indexOf(replaceThis, iStartMarker);
        if (iReplaceThis < 0) {
            return original;
        }
        int iEndMarkerIndex = original.length();
        if (!IPStringUtil.isEmpty(end_marker)) {
            iEndMarkerIndex = original.indexOf(end_marker, iStartMarker);
        }
        return getStringAfterSubstitution0(original, replaceThis, withThis, false, iReplaceThis, iEndMarkerIndex);
    }

    public static String getStringAfterReplacingRange(String value, int replaceThisIndex, int replaceThisLen, String withThis) {

        return (replaceThisIndex == 0 ? "" : value.substring(0, replaceThisIndex)) + withThis + (replaceThisIndex + replaceThisLen >= value.length() ? "" : value.substring(replaceThisIndex + replaceThisLen));

    }

    public static String getStringAfterMaskingChars(String value, int iMaskFrom, int numToMask, char mask) {
        Debug.assertMsg(logger, iMaskFrom >= 0, "iMaskFrom must be non-negative!");
        Debug.assertMsg(logger, numToMask >= 0, "maskTo must be non-negative!");
        if (IPStringUtil.isEmpty(value)) {
            return value;
        }

        if (numToMask <= 0) {
            return value;
        }

        int length = value.length();

        if (iMaskFrom > (length - 1)) {
            return value;
        }

        StringBuilder ret = new StringBuilder();

        int i = 0;
        // the original characters preceding the mask
        for (; i < iMaskFrom; i++) {
            ret.append(value.charAt(i));
        }

        // the mask
        for (; i < (iMaskFrom + numToMask); i++) {
            ret.append(mask);
        }

        for (; i < length; i++) {
            ret.append(value.charAt(i));
        }

        return ret.toString();
    }

    public static String getStringAfterTruncatingFromPosition(String s, int positionToTruncateFrom) {
        return getStringAfterTruncatingFromPosition(s, positionToTruncateFrom, false);
    }

    public static String getStringAfterTruncatingFromPosition(String s, int positionToTruncateFrom, boolean appendEllipsis) {
        if (IPStringUtil.isEmpty(s)) {
            return "";
        }
        if (positionToTruncateFrom < 0) {
            return s;
        }
        if (positionToTruncateFrom > s.length() - 1) {
            return s;
        }

        String value = s.substring(0, positionToTruncateFrom);
        if (appendEllipsis) {
            return getStringWithEllipsis(value, false);
        }
        return value;
    }

    /**
     * getStringAfterTruncatingToPosition("0123",1) ==> "23".
     *
     * @param positionToTruncateToInclusive we truncate up to and including
     *                                      this index.
     */
    public static String getStringAfterTruncatingToPosition(String s, int positionToTruncateToInclusive) {
        if (IPStringUtil.isEmpty(s)) {
            return "";
        }
        if (positionToTruncateToInclusive < 0) {
            return s;
        }
        if (positionToTruncateToInclusive >= s.length() - 1) {
            return "";
        }
        return s.substring(positionToTruncateToInclusive + 1);
    }

    private static Boolean isStringEqualBase(String a, String b) {
        // really equal?
        if (a == b) {
            return true;
        }
        if (a == null) {
            // bl: treat null and "" as equal
            return b.length() == 0;
        }
        if (b == null) {
            // bl: treat null and "" as equal
            return a.length() == 0;
        }
        // bl: don't know whether equality matches, so return null so the caller can finish the checks
        return null;
    }

    /**
     * return true if these strings are the same, case-sensitively
     * null==""
     * null==null
     * ""==""
     * "a"=="a"
     * "a"!="A"
     */
    public static boolean isStringEqual(String a, String b) {
        Boolean isEqual = isStringEqualBase(a, b);
        if (isEqual != null) {
            return isEqual;
        }
        // bl: at this point, we know that they are both non-null
        return a.equals(b);
    }

    /**
     * return true if these strings are the same, case-insensitively
     * null==""
     * null==null
     * ""==""
     * "a"=="a"
     * "a"=="A"
     */
    public static boolean isStringEqualIgnoreCase(String a, String b) {
        Boolean isEqual = isStringEqualBase(a, b);
        if (isEqual != null) {
            return isEqual;
        }
        // bl: at this point, we know that they are both non-null
        return a.equalsIgnoreCase(b);
    }

    public static String getTrimmedString(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    // bl: i give you the dreaded zero-width space. whoever invented this character...
    // refer: https://en.wikipedia.org/wiki/Zero-width_space
    private static final Pattern ZERO_WIDTH_SPACE_PATTERN = Pattern.compile("\\u200b");

    @Nullable
    public static String removeZeroWidthSpaces(String s) {
        if (s == null) {
            return null;
        }
        return ZERO_WIDTH_SPACE_PATTERN.matcher(s).replaceAll("");
    }

    /**
     * reverse a gnirts
     */
    public static String getReversedString(String s) {
        if (s == null) {
            return s;
        }
        int len = s.length();
        if (len < 2) {
            return s;
        }
        char ch[] = new char[len];
        s.getChars(0, len, ch, 0);
        StringBuilder b = new StringBuilder();
        for (int i = len - 1; i > -1; i--) {
            b.append(ch[i]);
        }
        return b.toString();
    }

    /**
     * turn array[] into an array[] of arrays where no array has more than maxItemsPerArray
     * this is used, e.g., by sql that only lets you have n elements in an
     * expression list at a time.
     */
    public static String[][] getManageableArraysFromArray(String array[], int maxItemsPerArray) {
        Collection<Collection<String>> ret = IPCollectionUtil.getManageableCollectionsFromCollection(IPCollectionUtil.getListFromArray(array), maxItemsPerArray);
        return ret.toArray(EMPTY_2D_STRING_ARRAY);
    }

    public static String[] getStringArrayFromObjectArray(Object o[]) {
        if (o == null) {
            return null;
        }
        String stringArray[] = new String[o.length];
        for (int i = 0; i < o.length; i++) {
            stringArray[i] = nullSafeToString(o[i]);
        }
        return stringArray;
    }

    public static final String[] getArrayFromStrings(String a) {
        return new String[]{a};
    }

    public static final String[] getArrayFromStrings(String a, String b) {
        return new String[]{a, b};
    }

    public static final String[] getArrayFromStrings(String a, String b, String c) {
        return new String[]{a, b, c};
    }

    public static final String[] getArrayFromStrings(String a, String b, String c, String d) {
        return new String[]{a, b, c, d};
    }

    public static final String[] getArrayFromStrings(String a, String b, String c, String d, String e) {
        return new String[]{a, b, c, d, e};
    }

    /**
     * return an array of upper case strings based on the mixed case
     * array of originals (which is left unchanged).
     */
    public static String[] getUpperCaseStrings(String originals[]) {
        if (originals == null) {
            return null;
        }
        String result[] = new String[originals.length];
        for (int i = 0; i < originals.length; i++) {
            result[i] = originals[i] == null ? null : originals[i].toUpperCase();
        }
        return result;
    }

    /**
     * return an array of upper case strings based on
     * array of originals (which is left unchanged).
     */
    public static String[] getLowerCaseStrings(String originals[]) {
        if (originals == null) {
            return null;
        }
        String result[] = new String[originals.length];
        for (int i = 0; i < originals.length; i++) {
            result[i] = originals[i] == null ? null : originals[i].toLowerCase();
        }
        return result;
    }

    public static String getStringAfterStrippingUnicodeMarker(String data) {
        if (data == null) {
            return null;
        }
        int len = data.length();
        if (len == 0) {
            return data;
        }
        char ch = data.charAt(0);
        if (ch == 0xfffe) {
            try {
                byte b[] = data.getBytes(IPUtil.getJavaEncoding("UTF-16LE"));
                // comes back ff fe fe ff
                if (b.length <= 4) {
                    return "";
                }
                data = new String(b, 2, b.length - 2, IPUtil.getJavaEncoding("UTF-16BE"));
                return data;
            } catch (UnsupportedEncodingException use) {
                Debug.assertMsg(logger, false, "Failed getting chars", use);
            }
        }
        if (ch != 0xfeff) {
            return data;
        }
        // nothing after unicode marker
        if (len == 0) {
            return "";
        }
        return data.substring(1);
    }

    static final String ianaEncodingsToTry[] = {"ISO-8859-1"};

    /**
     * gets a string from a set of bytes with uncertain encoding.  checks
     * for a utf8 signature on the bytes, otherwise tries iso8859-1
     */
    public static String getStringFromBytesWithUnknownEncoding(byte b[], String nameOfResource) {
        if (b == null) {
            return null;
        }
        if (b.length == 0) {
            return new String();
        }
        int startIndex = 0;
        // is there a utf8 marker? zero width non breaking space?  UTF-8 signature: EF BB BF?
        if (IPUtil.is3UTF8Bytes(b)) {
            startIndex += 3; //EF BB BF

            if (b.length <= startIndex) {
                return new String();
            }
            try {
                return new String(b, startIndex, b.length - startIndex, "UTF8");
            } catch (UnsupportedEncodingException use) {
                Debug.assertMsg(logger, false, "no utf8", use);
            }
        }
        //fe ff
        if (IPUtil.is2UnicodeBEBytes(b)) {
            startIndex += 2;
            if (b.length <= startIndex) {
                return new String();
            }
            try {
                String s = new String(b, startIndex, b.length - startIndex, "UnicodeBig");
                return s;
            } catch (UnsupportedEncodingException use) {
                Debug.assertMsg(logger, false, "no utf8", use);
            }

        }
        // ff fe
        if (IPUtil.is2UnicodeLEBytes(b)) {
            startIndex += 2;
            if (b.length <= startIndex) {
                return new String();
            }
            try {
                //b = IPUtil.getBytesAfterReplacing2BytePair(b, (byte)0xff, (byte)0xc0, (byte)0x00, (byte)0xc0);
                //b = IPUtil.getBytesAfterReplacing2BytePair(b, (byte)0xff, (byte)0x80, (byte)0x00, (byte)0x80);
                //getBytesAsHexStringDump(b) +
                String s = new String(b, startIndex, b.length - startIndex, "UnicodeLittle");
                return s;
            } catch (UnsupportedEncodingException use) {
                Debug.assertMsg(logger, false, "no utf8", use);
            }

        }
        String javaEncoding = null;

        for (int i = 0; i < ianaEncodingsToTry.length; i++) {
            try {
                javaEncoding = IPUtil.getJavaEncoding(ianaEncodingsToTry[i]);
                ByteArrayInputStream bais = new ByteArrayInputStream(b);
                InputStreamReader isr = new InputStreamReader(bais, javaEncoding);
                StringWriter output = new StringWriter(b.length + 500);
                IPIOUtil.doStreamInputToOutput(isr, output, true);
                //if(logger.isInfoEnabled()) logger.info( "File encoding for " + nameOfResource + " was detected as: " + javaEncoding);
                return output.getBuffer().toString();
            } catch (UnsupportedEncodingException use) {
                continue;
            } catch (IOException ioe) {
                if (logger.isInfoEnabled()) {
                    logger.info("File encoding was NOT : " + javaEncoding);
                }
                continue;
            }
        }
        Debug.assertMsg(logger, false, "unknown encoding type");
        return null;
    }

    /**
     * Adds the Encoding characters to the front of this byte array
     *
     * @param bytes
     * @param javaEncoding
     * @return new byte array with encoding characters added.
     */
    static public byte[] getBytesWithEncodingPrefix(byte bytes[], String javaEncoding) {
        if (bytes == null || bytes.length == 0) {
            return new byte[]{};
        }

        if ("UTF8".equals(javaEncoding)) {
            return IPUtil.getJoinedByteArrays(new byte[]{0xef - 0x100, 0xbb - 0x100, 0xbf - 0x100}, bytes);
        }

        if ("UnicodeBig".equals(javaEncoding)) {
            return IPUtil.getJoinedByteArrays(new byte[]{0xfe - 0x100, 0xff - 0x100}, bytes);
        }

        if ("UnicodeLittle".equals(javaEncoding)) {
            return IPUtil.getJoinedByteArrays(new byte[]{0xff - 0x100, 0xfe - 0x100}, bytes);
        }

        return bytes;
    }

    static public String getBytesAsHexStringDump(byte bytes[]) {
        StringBuilder buf = new StringBuilder(bytes.length * 5);
        for (int i = 0; i < bytes.length; i++) {
            String byteStr = Integer.toHexString((int) bytes[i]);
            if (byteStr.length() == 1) {
                byteStr = "0" + byteStr;
            } else if (byteStr.length() > 2) {
                byteStr = byteStr.substring(byteStr.length() - 2);
            }
            buf.append(byteStr).append(' ');
            if (bytes[i] == 0xa) {
                buf.append('\n');
            }

        }
        return buf.toString();
    }

    static public String getCharsAsHexStringDump(char chars[]) {
        StringBuilder buf = new StringBuilder(chars.length * 5);
        for (int i = 0; i < chars.length; i++) {
            String byteStr = Integer.toHexString(chars[i]);
            while (byteStr.length() < 4) {
                byteStr = "0" + byteStr;
            }
            buf.append(byteStr).append(' ');
            if (chars[i] == 0xa) {
                buf.append('\n');
            }

        }
        return buf.toString();
    }
    /**
     * get2DArrayFrom2Arrays: if input {"1", "2", "3"}, {"a", "b", "c"}
     * returns {{"1","a"}, {"2", "b"}, {"3", "c"}}
     */

    /**
     * getSubArray(["0","1","2"], 1) returns ["1","2"]
     * (similar to string.substring but works on arrays
     */
    public static String[] getSubArray(String array[], int index) {
        if (array == null || index >= array.length) {
            return EMPTY_STRING_ARRAY;
        }
        if (index == 0) {
            return array;
        }
        String ret[] = new String[array.length - index];
        System.arraycopy(array, index, ret, 0, ret.length);
        return ret;
    }

    public static String getMD5DigestFromString(String s) {
        if (s == null) {
            return s;
        }
        return IPUtil.getMD5DigestFromBytes(s.getBytes());
    }

    public static String getMD5DigestFromObjects(Collection<?> objs) {
        return getMD5DigestFromObjects(objs.toArray(new Object[0]));
    }

    public static String getMD5DigestFromObjects(Object... objs) {
        if (isEmptyOrNull(objs)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Object o : objs) {
            if (sb.length() > 0) {
                sb.append("//");
            }
            sb.append(o);
        }

        return IPUtil.getMD5DigestFromBytes(sb.toString().getBytes());
    }

    /**
     * return a string (32 chars it seems) digest of a given string.  uses the md5 digest.
     * the strings virtually (mathematically) have to be the same to get the same digest.
     *
     * @return null if s is null, else the md5 digest for s.
     */
    /* this wasn't working
    public static String getMD5DigestFromString(String s, String encoding) {
        //long startTime = System.currentTimeMillis();
        // need to check for null here to avoid NPEs if null gets passed in
        if(s==null)
            return s;
        
        try {
            return new String(
                    MessageDigest.getInstance("MD5").digest(
                            IPStringUtil.getBytesFromString(s, encoding)));
        } catch (NoSuchAlgorithmException e) {
            throw UnexpectedError.getRuntimeException("Can't load MD5 security algorithm",e,true);
        }
    }*/
    public static String getStringFromUTF8Chars(char ch[]) {
        if (ch == null || ch.length == 0) {
            return "";
        }
        int startIndex = 0;
        // is there a ucs2/utf16 marker? zero width non breaking space?
        // UCS-2 signature: FEFF
        // UTF-16 signature: FEFF
        if (ch[0] == 0xfeff) {
            startIndex++;
        } else {
            //logger.error( "No UTF8 marker reading reader.  Ignoring this and carrying on, but thought you'd like to know.", new Throwable("No exception, just thought you wanted to see a stack dump"));
        }
        if (ch.length <= startIndex) {
            return "";
        }
        return new String(ch, startIndex, ch.length - startIndex);
    }

    private static final BitSet s_delimiters = new BitSet();
    private static final BitSet s_semicolonDelimiters = new BitSet();

    static {
        //pb: don't use '.' here without being careful (e.g. it'll break the expections
        // for siteOptions.valid_referrers parsing
        s_delimiters.set(' ');
        s_delimiters.set(',');
        s_delimiters.set(';');
        s_semicolonDelimiters.set(';');
        s_delimiters.set('\r');
        s_delimiters.set('\n');
        s_delimiters.set('\t');
    }

    /**
     * breaks a string into an array of strings delimited by ' ', ',' ,; , \r ,\n, \t
     */
    public static List<String> getArrayFromDelimitedString(String delimitedString) {
        return getListFromDelimitedString(delimitedString, s_delimiters);
    }

    public static List<String> getListFromDelimitedString(String delimitedString) {
        return getListFromDelimitedString(delimitedString, s_delimiters);
    }

    public static List<String> getListFromDelimitedString(String delimitedString, BitSet delimiters) {
        if (IPStringUtil.isEmpty(delimitedString)) {
            return Collections.emptyList();
        }

        try {
            return Alias.tokenizeWithDelimiters(delimitedString.toCharArray(), new int[]{0}, false, delimiters);
        } catch (ParseException pe) {
            if (logger.isInfoEnabled()) {
                logger.info("failed parsing string", pe);
            }
        }
        return Collections.emptyList();
    }

    /**
     * breaks a string into an array of strings delimited by the supplied delimiter.
     */
    public static List<String> getListFromDelimitedString(String delimitedString, char delimiter) {
        return getArrayFromDelimitedString(delimitedString, delimiter, false);
    }

    /**
     * breaks a string into an array of strings delimited by the supplied delimiter.
     *
     * @param includeEmptyTokens e.g. a string of a,,c,d would return {'a', '', 'c', 'd'} when true, else  {'a', 'c', 'd'}
     */
    public static List<String> getArrayFromDelimitedString(String delimitedString, char delimiter, boolean includeEmptyTokens) {
        if (IPStringUtil.isEmpty(delimitedString)) {
            return Collections.emptyList();
        }

        BitSet tmpDelimiters = new BitSet();
        tmpDelimiters.set(delimiter);

        if (includeEmptyTokens) {
            try {
                return Alias.getTokensIncludingEmptyOnes(delimitedString, delimiter);
            } catch (ParseException pe) {
                if (logger.isInfoEnabled()) {
                    logger.info("failed parsing string", pe);
                }
            }
        }

        return getListFromDelimitedString(delimitedString, tmpDelimiters);
    }

    public static String getBytesAsString(byte data[], String name, boolean printAsHex) {
        StringBuilder message = new StringBuilder("/* file = " + name + ", size = " + data.length + " bytes */\n");
        message.append("byte b[] = {\n");
        int rowCount = 8;
        StringBuilder rowEnd = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                if (i % rowCount == 1 && i > 1) {
                    message.append("  // " + rowEnd.toString() + '\n');
                    rowEnd = new StringBuilder();
                }
                message.append(", ");
            }
            String num = null;
            if (printAsHex) {
                num = Integer.toHexString(data[i]);
                if (num.length() < 2) {
                    num = "0" + num;
                }
                num = "0x" + num;
            } else {
                num = Integer.toString(data[i]);
            }

            char ch = (char) data[i];
            if ('\n' == ch || '\r' == ch || ch < 0x20 || ch > 0x7e) {
                rowEnd.append('.');
            } else {
                rowEnd.append((char) (data[i] & 0xff));
            }
            message.append(num);
        }
        if (rowEnd.length() > 0) {
            message.append("  // " + rowEnd.toString());
        }
        message.append("\n};\n");
        message.append("/* " + data.length + " bytes in the array */\n");
        return message.toString();
    }

    /**
     * return an subset array.  eg. {"foo", "bar"}, 1, 1 => {"bar"}
     * eg. {"foo", "bar"}, 0, 1 => {"foo"}
     * eg. {"foo", "bar"}, 1, 55 => {"bar"}
     * eg. {"foo", "bar"}, 2, 55 => {}
     */
    public static String[] getStringArraySubset(String strings[], int startIndex, int itemsToReturn) {
        if (strings == null) {
            return EMPTY_STRING_ARRAY;
        }
        // they want everything
        if (startIndex == 0 && itemsToReturn == strings.length) {
            return strings;
        }
        // start index after end?
        if (startIndex >= strings.length) {
            return EMPTY_STRING_ARRAY;
        }
        itemsToReturn = Math.min(itemsToReturn, strings.length - startIndex);
        String ret[] = new String[itemsToReturn];
        System.arraycopy(strings, startIndex, ret, 0, itemsToReturn);
        return ret;
    }

    public static int getNumberOfOccurrencesOfStringInString(String stringToSearch, String stringOfWhichToCountOccurrences) {
        if (stringToSearch == null || stringOfWhichToCountOccurrences == null) {
            return 0;
        }
        int i = -1;
        int occurrences = 0;
        do {
            if (i >= stringToSearch.length()) {
                break;
            }
            int j = stringToSearch.indexOf(stringOfWhichToCountOccurrences, i + 1);
            if (j < 0) {
                break;
            }
            i = j + stringOfWhichToCountOccurrences.length();
            occurrences++;
        } while (true);
        return occurrences;
    }

    public static String stripNonNumericCharacters(String text) {
        if (isEmpty(text)) {
            return text;
        }
        StringBuilder parsedText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                parsedText.append(c);
            }
        }
        return parsedText.toString();
    }

    public static String stripAlphabeticCharacters(String text) {
        if (isEmpty(text)) {
            return text;
        }
        StringBuilder parsedText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                continue;
            }
            parsedText.append(c);
        }
        return parsedText.toString();
    }

    public static String getAsCurrencyString(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);
        currencyFormat.setGroupingUsed(false);
        return currencyFormat.format(amount);
    }

    /**
     * Utility function to simplify concatinating two strings together without
     * having to worry about testing for nulls.
     *
     * @param beginning The beginning of the resulting concatinated String
     * @param end       The end of the resulting cancatinated String
     * @return The result of the Concatinating the two Strings together.  null if beginning is empty and end is null
     */
    public static String concat(String beginning, String end) {
        if (IPStringUtil.isEmpty(beginning)) {
            return end;
        }

        if (IPStringUtil.isEmpty(end)) {
            return beginning;
        }

        return beginning.concat(end);
    }

    public static InputStream getAsInputStream(String str) {
        if (isEmpty(str)) {
            return null;
        }
        byte[] bytes = new byte[0];
        try {
            bytes = str.getBytes(UTF8_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            logger.error("Failed getting a InputStream for String:" + str);
        }
        return new ByteArrayInputStream(bytes);
    }

    public static String getStringFromInputStream(InputStream is) {
        try {
            StringBuilder out = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = is.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
            return out.toString();
        } catch (IOException ioex) {
            return null;
        }
    }

    public static String stripNonAlphaNumericCharacters(String text) {
        if (isEmpty(text)) {
            return text;
        }
        StringBuilder parsedText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                parsedText.append(c);
            }
        }
        return parsedText.toString();
    }

    /**
     * Returns true if the string has a capitol letter in the first position only.  Helpfull for determining if
     * a word is a proper noun or not.
     * i.e
     * Paul -> true
     * cat -> false
     * SHOUT -> false
     * CamelCase -> false
     * pascalCase -> false
     *
     * @param token
     * @return
     */
    public static boolean isFirstLetterCapitalizedOnly(String token) {
        if (isEmpty(token)) {
            return false;
        }

        if (!isFirstLetterCapitalized(token)) {
            return false;
        }

        String rest = token.substring(1);
        return rest.equals(rest.toLowerCase());
    }

    public static boolean isFirstLetterCapitalized(String token) {
        if (isEmpty(token)) {
            return false;
        }

        String first = token.substring(0, 1);
        // todo: why not this instead?
        /*char firstChar = token.charAt(0);
        return Character.isLetter(firstChar) && Character.isUpperCase(firstChar);*/
        boolean isLetter = first.charAt(0) >= 'A' && first.charAt(0) <= 'z';
        if (!isLetter || !first.equals(first.toUpperCase())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * null-safe version of getLevenshteinDistance from StringUtils
     *
     * @param s1 the first string to compare
     * @param s2 the second string to compare
     * @return the distance between the two strings
     */
    public static int getLevenshteinDistance(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }

        if (s1 == null) {
            return s2.length();
        }

        if (s2 == null) {
            return s1.length();
        }

        return StringUtils.getLevenshteinDistance(s1, s2);
    }

    /**
     * This ensures that the string str contains the repeatString at least n times.
     *
     * @param str          String to check for the repeat string
     * @param repeatString the string that should repeat
     * @param n            the minimum number of times the character should be repeated
     * @return true or false
     */
    public static boolean ensureCharacterRepeatTimes(String str, String repeatString, int n) {
        if (n == 0) {
            return true;
        }

        int index = -1;
        int count = 0;

        do {
            index = str.indexOf(repeatString, index + 1);
            if (index > 0) {
                ++count;
            }
        } while (index > 0);

        return count >= n;
    }

    public static ObjectPair<String, List<String>> getStringAndMatchesAfterStrippingFromEnd(String str, String... stripFromEnd) {
        String result = str;
        List<String> matches = newArrayList();

        if (!isEmptyOrNull(stripFromEnd)) {
            boolean stripped;
            do {
                stripped = false;
                String strLower = result.toLowerCase();
                for (String strip : stripFromEnd) {
                    if (strLower.endsWith(strip)) {
                        matches.add(strip);
                        result = result.substring(0, result.length() - strip.length());
                        stripped = true;

                        break;
                    }
                }

            } while (stripped);
        }

        // jw: probably doesnt matter, but lets reverse the matches so that they are in the same order as they were
        //     on the string
        Collections.reverse(matches);
        return newObjectPair(result, matches);
    }

    private static final Map<String, String> HTML4_MAP = createHTML4Map();

    private static Map<String, String> createHTML4Map() {
        Map<String, String> result = new HashMap<String, String>();

        result.put("quot", "34"); // " - double-quote
        result.put("amp", "38"); // & - ampersand
        result.put("lt", "60"); // < - less-than
        result.put("gt", "62"); // > - greater-than
        result.put("nbsp", "160"); // non-breaking space
        result.put("iexcl", "161"); // inverted exclamation mark
        result.put("cent", "162"); // cent sign
        result.put("pound", "163"); // pound sign
        result.put("curren", "164"); // currency sign
        result.put("yen", "165"); // yen sign = yuan sign
        result.put("brvbar", "166"); // broken bar = broken vertical bar
        result.put("sect", "167"); // section sign
        result.put("uml", "168"); // diaeresis = spacing diaeresis
        result.put("copy", "169"); //  - copyright sign
        result.put("ordf", "170"); // feminine ordinal indicator
        result.put("laquo", "171"); // left-pointing double angle quotation mark = left pointing guillemet
        result.put("not", "172"); // not sign
        result.put("shy", "173"); // soft hyphen = discretionary hyphen
        result.put("reg", "174"); //  - registered trademark sign
        result.put("macr", "175"); // macron = spacing macron = overline = APL overbar
        result.put("deg", "176"); // degree sign
        result.put("plusmn", "177"); // plus-minus sign = plus-or-minus sign
        result.put("sup2", "178"); // superscript two = superscript digit two = squared
        result.put("sup3", "179"); // superscript three = superscript digit three = cubed
        result.put("acute", "180"); // acute accent = spacing acute
        result.put("micro", "181"); // micro sign
        result.put("para", "182"); // pilcrow sign = paragraph sign
        result.put("middot", "183"); // middle dot = Georgian comma = Greek middle dot
        result.put("cedil", "184"); // cedilla = spacing cedilla
        result.put("sup1", "185"); // superscript one = superscript digit one
        result.put("ordm", "186"); // masculine ordinal indicator
        result.put("raquo", "187"); // right-pointing double angle quotation mark = right pointing guillemet
        result.put("frac14", "188"); // vulgar fraction one quarter = fraction one quarter
        result.put("frac12", "189"); // vulgar fraction one half = fraction one half
        result.put("frac34", "190"); // vulgar fraction three quarters = fraction three quarters
        result.put("iquest", "191"); // inverted question mark = turned question mark
        result.put("Agrave", "192"); //  - uppercase A, grave accent
        result.put("Aacute", "193"); //  - uppercase A, acute accent
        result.put("Acirc", "194"); //  - uppercase A, circumflex accent
        result.put("Atilde", "195"); //  - uppercase A, tilde
        result.put("Auml", "196"); //  - uppercase A, umlaut
        result.put("Aring", "197"); //  - uppercase A, ring
        result.put("AElig", "198"); //  - uppercase AE
        result.put("Ccedil", "199"); //  - uppercase C, cedilla
        result.put("Egrave", "200"); //  - uppercase E, grave accent
        result.put("Eacute", "201"); //  - uppercase E, acute accent
        result.put("Ecirc", "202"); //  - uppercase E, circumflex accent
        result.put("Euml", "203"); //  - uppercase E, umlaut
        result.put("Igrave", "204"); //  - uppercase I, grave accent
        result.put("Iacute", "205"); //  - uppercase I, acute accent
        result.put("Icirc", "206"); //  - uppercase I, circumflex accent
        result.put("Iuml", "207"); //  - uppercase I, umlaut
        result.put("ETH", "208"); //  - uppercase Eth, Icelandic
        result.put("Ntilde", "209"); //  - uppercase N, tilde
        result.put("Ograve", "210"); //  - uppercase O, grave accent
        result.put("Oacute", "211"); //  - uppercase O, acute accent
        result.put("Ocirc", "212"); //  - uppercase O, circumflex accent
        result.put("Otilde", "213"); //  - uppercase O, tilde
        result.put("Ouml", "214"); //  - uppercase O, umlaut
        result.put("times", "215"); // multiplication sign
        result.put("Oslash", "216"); //  - uppercase O, slash
        result.put("Ugrave", "217"); //  - uppercase U, grave accent
        result.put("Uacute", "218"); //  - uppercase U, acute accent
        result.put("Ucirc", "219"); //  - uppercase U, circumflex accent
        result.put("Uuml", "220"); //  - uppercase U, umlaut
        result.put("Yacute", "221"); //  - uppercase Y, acute accent
        result.put("THORN", "222"); //  - uppercase THORN, Icelandic
        result.put("szlig", "223"); //  - lowercase sharps, German
        result.put("agrave", "224"); //  - lowercase a, grave accent
        result.put("aacute", "225"); //  - lowercase a, acute accent
        result.put("acirc", "226"); //  - lowercase a, circumflex accent
        result.put("atilde", "227"); //  - lowercase a, tilde
        result.put("auml", "228"); //  - lowercase a, umlaut
        result.put("aring", "229"); //  - lowercase a, ring
        result.put("aelig", "230"); //  - lowercase ae
        result.put("ccedil", "231"); //  - lowercase c, cedilla
        result.put("egrave", "232"); //  - lowercase e, grave accent
        result.put("eacute", "233"); //  - lowercase e, acute accent
        result.put("ecirc", "234"); //  - lowercase e, circumflex accent
        result.put("euml", "235"); //  - lowercase e, umlaut
        result.put("igrave", "236"); //  - lowercase i, grave accent
        result.put("iacute", "237"); //  - lowercase i, acute accent
        result.put("icirc", "238"); //  - lowercase i, circumflex accent
        result.put("iuml", "239"); //  - lowercase i, umlaut
        result.put("eth", "240"); //  - lowercase eth, Icelandic
        result.put("ntilde", "241"); //  - lowercase n, tilde
        result.put("ograve", "242"); //  - lowercase o, grave accent
        result.put("oacute", "243"); //  - lowercase o, acute accent
        result.put("ocirc", "244"); //  - lowercase o, circumflex accent
        result.put("otilde", "245"); //  - lowercase o, tilde
        result.put("ouml", "246"); //  - lowercase o, umlaut
        result.put("divide", "247"); // division sign
        result.put("oslash", "248"); //  - lowercase o, slash
        result.put("ugrave", "249"); //  - lowercase u, grave accent
        result.put("uacute", "250"); //  - lowercase u, acute accent
        result.put("ucirc", "251"); //  - lowercase u, circumflex accent
        result.put("uuml", "252"); //  - lowercase u, umlaut
        result.put("yacute", "253"); //  - lowercase y, acute accent
        result.put("thorn", "254"); //  - lowercase thorn, Icelandic
        result.put("yuml", "255"); //  - lowercase y, umlaut
        // <!-- Latin Extended-B -->
        result.put("fnof", "402"); // latin small f with hook = function= florin, U+0192 ISOtech -->
        // <!-- Greek -->
        result.put("Alpha", "913"); // greek capital letter alpha, U+0391 -->
        result.put("Beta", "914"); // greek capital letter beta, U+0392 -->
        result.put("Gamma", "915"); // greek capital letter gamma,U+0393 ISOgrk3 -->
        result.put("Delta", "916"); // greek capital letter delta,U+0394 ISOgrk3 -->
        result.put("Epsilon", "917"); // greek capital letter epsilon, U+0395 -->
        result.put("Zeta", "918"); // greek capital letter zeta, U+0396 -->
        result.put("Eta", "919"); // greek capital letter eta, U+0397 -->
        result.put("Theta", "920"); // greek capital letter theta,U+0398 ISOgrk3 -->
        result.put("Iota", "921"); // greek capital letter iota, U+0399 -->
        result.put("Kappa", "922"); // greek capital letter kappa, U+039A -->
        result.put("Lambda", "923"); // greek capital letter lambda,U+039B ISOgrk3 -->
        result.put("Mu", "924"); // greek capital letter mu, U+039C -->
        result.put("Nu", "925"); // greek capital letter nu, U+039D -->
        result.put("Xi", "926"); // greek capital letter xi, U+039E ISOgrk3 -->
        result.put("Omicron", "927"); // greek capital letter omicron, U+039F -->
        result.put("Pi", "928"); // greek capital letter pi, U+03A0 ISOgrk3 -->
        result.put("Rho", "929"); // greek capital letter rho, U+03A1 -->
        // <!-- there is no Sigmaf, and no U+03A2 character either -->
        result.put("Sigma", "931"); // greek capital letter sigma,U+03A3 ISOgrk3 -->
        result.put("Tau", "932"); // greek capital letter tau, U+03A4 -->
        result.put("Upsilon", "933"); // greek capital letter upsilon,U+03A5 ISOgrk3 -->
        result.put("Phi", "934"); // greek capital letter phi,U+03A6 ISOgrk3 -->
        result.put("Chi", "935"); // greek capital letter chi, U+03A7 -->
        result.put("Psi", "936"); // greek capital letter psi,U+03A8 ISOgrk3 -->
        result.put("Omega", "937"); // greek capital letter omega,U+03A9 ISOgrk3 -->
        result.put("alpha", "945"); // greek small letter alpha,U+03B1 ISOgrk3 -->
        result.put("beta", "946"); // greek small letter beta, U+03B2 ISOgrk3 -->
        result.put("gamma", "947"); // greek small letter gamma,U+03B3 ISOgrk3 -->
        result.put("delta", "948"); // greek small letter delta,U+03B4 ISOgrk3 -->
        result.put("epsilon", "949"); // greek small letter epsilon,U+03B5 ISOgrk3 -->
        result.put("zeta", "950"); // greek small letter zeta, U+03B6 ISOgrk3 -->
        result.put("eta", "951"); // greek small letter eta, U+03B7 ISOgrk3 -->
        result.put("theta", "952"); // greek small letter theta,U+03B8 ISOgrk3 -->
        result.put("iota", "953"); // greek small letter iota, U+03B9 ISOgrk3 -->
        result.put("kappa", "954"); // greek small letter kappa,U+03BA ISOgrk3 -->
        result.put("lambda", "955"); // greek small letter lambda,U+03BB ISOgrk3 -->
        result.put("mu", "956"); // greek small letter mu, U+03BC ISOgrk3 -->
        result.put("nu", "957"); // greek small letter nu, U+03BD ISOgrk3 -->
        result.put("xi", "958"); // greek small letter xi, U+03BE ISOgrk3 -->
        result.put("omicron", "959"); // greek small letter omicron, U+03BF NEW -->
        result.put("pi", "960"); // greek small letter pi, U+03C0 ISOgrk3 -->
        result.put("rho", "961"); // greek small letter rho, U+03C1 ISOgrk3 -->
        result.put("sigmaf", "962"); // greek small letter final sigma,U+03C2 ISOgrk3 -->
        result.put("sigma", "963"); // greek small letter sigma,U+03C3 ISOgrk3 -->
        result.put("tau", "964"); // greek small letter tau, U+03C4 ISOgrk3 -->
        result.put("upsilon", "965"); // greek small letter upsilon,U+03C5 ISOgrk3 -->
        result.put("phi", "966"); // greek small letter phi, U+03C6 ISOgrk3 -->
        result.put("chi", "967"); // greek small letter chi, U+03C7 ISOgrk3 -->
        result.put("psi", "968"); // greek small letter psi, U+03C8 ISOgrk3 -->
        result.put("omega", "969"); // greek small letter omega,U+03C9 ISOgrk3 -->
        result.put("thetasym", "977"); // greek small letter theta symbol,U+03D1 NEW -->
        result.put("upsih", "978"); // greek upsilon with hook symbol,U+03D2 NEW -->
        result.put("piv", "982"); // greek pi symbol, U+03D6 ISOgrk3 -->
        // <!-- General Punctuation -->
        result.put("bull", "8226"); // bullet = black small circle,U+2022 ISOpub -->
        // <!-- bullet is NOT the same as bullet operator, U+2219 -->
        result.put("hellip", "8230"); // horizontal ellipsis = three dot leader,U+2026 ISOpub -->
        result.put("prime", "8242"); // prime = minutes = feet, U+2032 ISOtech -->
        result.put("Prime", "8243"); // double prime = seconds = inches,U+2033 ISOtech -->
        result.put("oline", "8254"); // overline = spacing overscore,U+203E NEW -->
        result.put("frasl", "8260"); // fraction slash, U+2044 NEW -->
        // <!-- Letterlike Symbols -->
        result.put("weierp", "8472"); // script capital P = power set= Weierstrass p, U+2118 ISOamso -->
        result.put("image", "8465"); // blackletter capital I = imaginary part,U+2111 ISOamso -->
        result.put("real", "8476"); // blackletter capital R = real part symbol,U+211C ISOamso -->
        result.put("trade", "8482"); // trade mark sign, U+2122 ISOnum -->
        result.put("alefsym", "8501"); // alef symbol = first transfinite cardinal,U+2135 NEW -->
        // <!-- alef symbol is NOT the same as hebrew letter alef,U+05D0 although the
        // same glyph could be used to depict both characters -->
        // <!-- Arrows -->
        result.put("larr", "8592"); // leftwards arrow, U+2190 ISOnum -->
        result.put("uarr", "8593"); // upwards arrow, U+2191 ISOnum-->
        result.put("rarr", "8594"); // rightwards arrow, U+2192 ISOnum -->
        result.put("darr", "8595"); // downwards arrow, U+2193 ISOnum -->
        result.put("harr", "8596"); // left right arrow, U+2194 ISOamsa -->
        result.put("crarr", "8629"); // downwards arrow with corner leftwards= carriage return, U+21B5 NEW -->
        result.put("lArr", "8656"); // leftwards double arrow, U+21D0 ISOtech -->
        // <!-- ISO 10646 does not say that lArr is the same as the 'is implied by'
        // arrow but also does not have any other character for that function.
        // So ? lArr canbe used for 'is implied by' as ISOtech suggests -->
        result.put("uArr", "8657"); // upwards double arrow, U+21D1 ISOamsa -->
        result.put("rArr", "8658"); // rightwards double arrow,U+21D2 ISOtech -->
        // <!-- ISO 10646 does not say this is the 'implies' character but does not
        // have another character with this function so ?rArr can be used for
        // 'implies' as ISOtech suggests -->
        result.put("dArr", "8659"); // downwards double arrow, U+21D3 ISOamsa -->
        result.put("hArr", "8660"); // left right double arrow,U+21D4 ISOamsa -->
        // <!-- Mathematical Operators -->
        result.put("forall", "8704"); // for all, U+2200 ISOtech -->
        result.put("part", "8706"); // partial differential, U+2202 ISOtech -->
        result.put("exist", "8707"); // there exists, U+2203 ISOtech -->
        result.put("empty", "8709"); // empty set = null set = diameter,U+2205 ISOamso -->
        result.put("nabla", "8711"); // nabla = backward difference,U+2207 ISOtech -->
        result.put("isin", "8712"); // element of, U+2208 ISOtech -->
        result.put("notin", "8713"); // not an element of, U+2209 ISOtech -->
        result.put("ni", "8715"); // contains as member, U+220B ISOtech -->
        // <!-- should there be a more memorable name than 'ni'? -->
        result.put("prod", "8719"); // n-ary product = product sign,U+220F ISOamsb -->
        // <!-- prod is NOT the same character as U+03A0 'greek capital letter pi'
        // though the same glyph might be used for both -->
        result.put("sum", "8721"); // n-ary summation, U+2211 ISOamsb -->
        // <!-- sum is NOT the same character as U+03A3 'greek capital letter sigma'
        // though the same glyph might be used for both -->
        result.put("minus", "8722"); // minus sign, U+2212 ISOtech -->
        result.put("lowast", "8727"); // asterisk operator, U+2217 ISOtech -->
        result.put("radic", "8730"); // square root = radical sign,U+221A ISOtech -->
        result.put("prop", "8733"); // proportional to, U+221D ISOtech -->
        result.put("infin", "8734"); // infinity, U+221E ISOtech -->
        result.put("ang", "8736"); // angle, U+2220 ISOamso -->
        result.put("and", "8743"); // logical and = wedge, U+2227 ISOtech -->
        result.put("or", "8744"); // logical or = vee, U+2228 ISOtech -->
        result.put("cap", "8745"); // intersection = cap, U+2229 ISOtech -->
        result.put("cup", "8746"); // union = cup, U+222A ISOtech -->
        result.put("int", "8747"); // integral, U+222B ISOtech -->
        result.put("there4", "8756"); // therefore, U+2234 ISOtech -->
        result.put("sim", "8764"); // tilde operator = varies with = similar to,U+223C ISOtech -->
        // <!-- tilde operator is NOT the same character as the tilde, U+007E,although
        // the same glyph might be used to represent both -->
        result.put("cong", "8773"); // approximately equal to, U+2245 ISOtech -->
        result.put("asymp", "8776"); // almost equal to = asymptotic to,U+2248 ISOamsr -->
        result.put("ne", "8800"); // not equal to, U+2260 ISOtech -->
        result.put("equiv", "8801"); // identical to, U+2261 ISOtech -->
        result.put("le", "8804"); // less-than or equal to, U+2264 ISOtech -->
        result.put("ge", "8805"); // greater-than or equal to,U+2265 ISOtech -->
        result.put("sub", "8834"); // subset of, U+2282 ISOtech -->
        result.put("sup", "8835"); // superset of, U+2283 ISOtech -->
        // <!-- note that nsup, 'not a superset of, U+2283' is not covered by the
        // Symbol font encoding and is not included. Should it be, for symmetry?
        // It is in ISOamsn --> <!ENTITY nsub", "8836");
        // not a subset of, U+2284 ISOamsn -->
        result.put("sube", "8838"); // subset of or equal to, U+2286 ISOtech -->
        result.put("supe", "8839"); // superset of or equal to,U+2287 ISOtech -->
        result.put("oplus", "8853"); // circled plus = direct sum,U+2295 ISOamsb -->
        result.put("otimes", "8855"); // circled times = vector product,U+2297 ISOamsb -->
        result.put("perp", "8869"); // up tack = orthogonal to = perpendicular,U+22A5 ISOtech -->
        result.put("sdot", "8901"); // dot operator, U+22C5 ISOamsb -->
        // <!-- dot operator is NOT the same character as U+00B7 middle dot -->
        // <!-- Miscellaneous Technical -->
        result.put("lceil", "8968"); // left ceiling = apl upstile,U+2308 ISOamsc -->
        result.put("rceil", "8969"); // right ceiling, U+2309 ISOamsc -->
        result.put("lfloor", "8970"); // left floor = apl downstile,U+230A ISOamsc -->
        result.put("rfloor", "8971"); // right floor, U+230B ISOamsc -->
        result.put("lang", "9001"); // left-pointing angle bracket = bra,U+2329 ISOtech -->
        // <!-- lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation
        // mark' -->
        result.put("rang", "9002"); // right-pointing angle bracket = ket,U+232A ISOtech -->
        // <!-- rang is NOT the same character as U+003E 'greater than' or U+203A
        // 'single right-pointing angle quotation mark' -->
        // <!-- Geometric Shapes -->
        result.put("loz", "9674"); // lozenge, U+25CA ISOpub -->
        // <!-- Miscellaneous Symbols -->
        result.put("spades", "9824"); // black spade suit, U+2660 ISOpub -->
        // <!-- black here seems to mean filled as opposed to hollow -->
        result.put("clubs", "9827"); // black club suit = shamrock,U+2663 ISOpub -->
        result.put("hearts", "9829"); // black heart suit = valentine,U+2665 ISOpub -->
        result.put("diams", "9830"); // black diamond suit, U+2666 ISOpub -->

        // <!-- Latin Extended-A -->
        result.put("OElig", "338"); // -- latin capital ligature OE,U+0152 ISOlat2 -->
        result.put("oelig", "339"); // -- latin small ligature oe, U+0153 ISOlat2 -->
        // <!-- ligature is a misnomer, this is a separate character in some languages -->
        result.put("Scaron", "352"); // -- latin capital letter S with caron,U+0160 ISOlat2 -->
        result.put("scaron", "353"); // -- latin small letter s with caron,U+0161 ISOlat2 -->
        result.put("Yuml", "376"); // -- latin capital letter Y with diaeresis,U+0178 ISOlat2 -->
        // <!-- Spacing Modifier Letters -->
        result.put("circ", "710"); // -- modifier letter circumflex accent,U+02C6 ISOpub -->
        result.put("tilde", "732"); // small tilde, U+02DC ISOdia -->
        // <!-- General Punctuation -->
        result.put("ensp", "8194"); // en space, U+2002 ISOpub -->
        result.put("emsp", "8195"); // em space, U+2003 ISOpub -->
        result.put("thinsp", "8201"); // thin space, U+2009 ISOpub -->
        result.put("zwnj", "8204"); // zero width non-joiner,U+200C NEW RFC 2070 -->
        result.put("zwj", "8205"); // zero width joiner, U+200D NEW RFC 2070 -->
        result.put("lrm", "8206"); // left-to-right mark, U+200E NEW RFC 2070 -->
        result.put("rlm", "8207"); // right-to-left mark, U+200F NEW RFC 2070 -->
        result.put("ndash", "8211"); // en dash, U+2013 ISOpub -->
        result.put("mdash", "8212"); // em dash, U+2014 ISOpub -->
        result.put("lsquo", "8216"); // left single quotation mark,U+2018 ISOnum -->
        result.put("rsquo", "8217"); // right single quotation mark,U+2019 ISOnum -->
        result.put("sbquo", "8218"); // single low-9 quotation mark, U+201A NEW -->
        result.put("ldquo", "8220"); // left double quotation mark,U+201C ISOnum -->
        result.put("rdquo", "8221"); // right double quotation mark,U+201D ISOnum -->
        result.put("bdquo", "8222"); // double low-9 quotation mark, U+201E NEW -->
        result.put("dagger", "8224"); // dagger, U+2020 ISOpub -->
        result.put("Dagger", "8225"); // double dagger, U+2021 ISOpub -->
        result.put("permil", "8240"); // per mille sign, U+2030 ISOtech -->
        result.put("lsaquo", "8249"); // single left-pointing angle quotation mark,U+2039 ISO proposed -->
        // <!-- lsaquo is proposed but not yet ISO standardized -->
        result.put("rsaquo", "8250"); // single right-pointing angle quotation mark,U+203A ISO proposed -->
        // <!-- rsaquo is proposed but not yet ISO standardized -->
        result.put("euro", "8364"); // -- euro sign, U+20AC NEW -->

        return Collections.unmodifiableMap(result);
    }

    public static String escapeHTMLToValueEntities(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int firstAmp = str.indexOf('&');
        if (firstAmp < 0) {
            return str;
        } else {
            StringWriter stringWriter = new StringWriter((int) (str.length() + (str.length() * 0.1)));
            stringWriter.write(str, 0, firstAmp);
            int len = str.length();
            for (int i = firstAmp; i < len; i++) {
                char c = str.charAt(i);
                if (c == '&') {
                    int nextIdx = i + 1;
                    int semiColonIdx = str.indexOf(';', nextIdx);
                    if (semiColonIdx == -1) {
                        stringWriter.write(c);
                        continue;
                    }
                    int amphersandIdx = str.indexOf('&', i + 1);
                    if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
                        // Then the text looks like &...&...;
                        stringWriter.write(c);
                        continue;
                    }
                    String entityContent = str.substring(nextIdx, semiColonIdx);
                    String entityValue = null;
                    int entityContentLen = entityContent.length();
                    if (entityContentLen > 0) {
                        if (entityContent.charAt(0) != '#') {
                            entityValue = HTML4_MAP.get(entityContent);
                        }
                    }

                    stringWriter.write('&');
                    if (entityValue == null) {
                        stringWriter.write(entityContent);
                    } else {
                        stringWriter.write('#');
                        stringWriter.write(entityValue);
                    }
                    stringWriter.write(';');
                    i = semiColonIdx; // move index up to the semi-colon
                } else {
                    stringWriter.write(c);
                }
            }
            return stringWriter.toString();
        }
    }

    public static Pattern createCaseInsensitivePrefixPattern(String prefix) {
        return Pattern.compile("^(" + Pattern.quote(prefix) + ")", Pattern.CASE_INSENSITIVE);
    }

    private static final ObjectMapper SIMPLE_JSON_PARSER = new ObjectMapper();

    static {
        // jw: we want to be sure that any extra data that may be included in messages (attachments, etc...) do not break the deserialization.
        SIMPLE_JSON_PARSER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T[] parseArrayFromJson(String json, Class<T[]> ofType) {
        try {
            return SIMPLE_JSON_PARSER.readValue(json, ofType);
        } catch (IOException e) {
            // jw: Let's include the json in the UnexpectedError, so that it's easier to track down the root cause of the
            //     problem... This is used for parsing data from APIs currently, but could be used for anything going forward.
            throw UnexpectedError.getRuntimeException("Failed parsing json: " + json, e);
        }
    }

    public static <T> T parseFromJson(String json, Class<T> ofType) {
        try {
            return SIMPLE_JSON_PARSER.readValue(json, ofType);
        } catch (IOException e) {
            // jw: Let's include the json in the UnexpectedError, so that it's easier to track down the root cause of the
            //     problem... This is used for parsing data from APIs currently, but could be used for anything going forward.
            throw UnexpectedError.getRuntimeException("Failed parsing json: " + json, e);
        }
    }
}
