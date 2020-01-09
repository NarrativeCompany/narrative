package org.narrative.common.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Vector;

/**
 * Alias complies and expands input strings against an alias.
 * <p>
 * /aliasname text [$index[="variable name"]] text
 * <p>
 * variable name:refers to a named variable you supply.  If no
 * variable is found, you'll be prompted for one.
 * <p>
 * $index :the index refers to the n'th token on the command line supplied
 * when expanding the alis
 * <p>
 * $2- :indicates that everything following and including parameter
 * 2 should be appended to the command line
 * <p>
 * $2-4 : indicated user token 2 through four are substituted
 * <p>
 * $-4 : indicated user token 0 (the alias name) through four are substituted
 * <p>
 * $$1 : indicates this parameter is mandatory and the user will be prompted
 * for a value if they don't enter one.
 *
 * <listing>
 * llr:
 * ALIASNAME = (~whitespace)+
 * alias = ( input)*
 * INPUT = (text | parameter | null)*
 * PARAMETER = $[$](number | from | to | range)[="paramter_name"]
 * FROM = number-
 * TO = -number
 * RANGE = number-number
 *
 * </listing>
 * I know its a funny place for it, but Alias also has a bunch of useful parsing methods
 * that other code can make use of.
 *
 * @author Peter Bryant(pbryant@bigfoot.com)
 */
public final class Alias {
    private static final NarrativeLogger logger = new NarrativeLogger(Alias.class);
    public static final BitSet commaAndSpaceDelimiters = new BitSet();
    public static final BitSet hexDigits = new BitSet();
    /**
     * an array containing true at the index where the ascii value
     * is a digit.  Speeds up parsing no end.
     */
    static public final boolean DIGIT[] = new boolean[0x100];
    static public final boolean ALPHA[] = new boolean[0x100];

    static {
        commaAndSpaceDelimiters.set(',');
        commaAndSpaceDelimiters.set(' ');
        {
            for (int i = '0'; i <= '9'; i++) {
                hexDigits.set(i);
            }
            for (int i = 'a'; i <= 'f'; i++) {
                hexDigits.set(i);
            }
            for (int i = 'A'; i <= 'F'; i++) {
                hexDigits.set(i);
            }
        }
        markRange(DIGIT, '0', '9', true);
        markRange(ALPHA, 'a', 'z', true);
        markRange(ALPHA, 'A', 'Z', true);
    }

    static void markRange(boolean[] chars, char from, char to, boolean valid) {
        for (int i = (int) from; i <= (int) to; i++) {
            chars[i] = valid;
        }
    }

    /**
     * IFetchInput specifies the interface for getting values to fill
     * in from a set of arguments.
     *
     * @return values for arguments or null if user cancelled.
     */
    public static interface IFetchInput {
        String[] getInput(String aliasName, String rawAlias, String missingArguments[], String help);
    }

    public static List<String> getTokensIncludingEmptyOnes(String s, char delimiter) throws ParseException {
        BitSet delimiters = new BitSet();
        delimiters.set(delimiter);
        char input[] = s.toCharArray();
        int current[] = new int[]{0};
        ArrayList<String> tokens = new ArrayList<String>();
        boolean addAnEmptyIfTheNextTokenIsADelimiter = true;
        while (true) {
            //eol
            if (current[0] >= input.length) {
                break;
            }
            // eat a delimiter and add an empty token
            if (delimiters.get(input[current[0]])) {
                current[0]++;
                if (addAnEmptyIfTheNextTokenIsADelimiter) {
                    tokens.add("");
                }
                addAnEmptyIfTheNextTokenIsADelimiter = true;
                continue;
            }
            String token = delimitedWord(input, current, delimiters);
            addAnEmptyIfTheNextTokenIsADelimiter = false;
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * return a array of tokens.  tokens delimited by space or "..." pair
     */
    public static List<String> tokenizeWithDelimiters(char input[], int current[], boolean isQuotedTextOneToken, BitSet delimiters) throws ParseException {
        ArrayList<String> tokens = new ArrayList<String>();
        while (true) {
            //eol
            if (current[0] >= input.length) {
                break;
            }
            // ignore commans
            if (delimiters.get(input[current[0]])) {
                current[0]++;
                continue;
            }
            // eat spaces
            if (delimiters.get(input[current[0]])) {
                delimiters(input, current, delimiters);
                continue;
            }
            // do quoted parameter
            if (isQuotedTextOneToken && input[current[0]] == '"') {
                String token = quotedString(input, current, '\\');
                tokens.add(token);
                continue;
            }
            String token = delimitedWord(input, current, delimiters);
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * <SPACE: (' ')+ >
     */
    public static void space(char input[], int current[]) throws ParseException {
        if (current[0] >= input.length || ' ' != input[current[0]]) {
            throw new ParseException("Expecting ' '", current[0]);
        }
        current[0]++;
        while (current[0] < input.length && ' ' == input[current[0]]) {
            current[0]++;
        }
    }

    /**
     * <DELIMITERS: ('<delimited char>')+ >
     */
    public static void delimiters(char input[], int current[], BitSet delimiters) throws ParseException {
        if (current[0] >= input.length || !delimiters.get(input[current[0]])) {
            throw new ParseException("Expecting ' '", current[0]);
        }
        current[0]++;
        while (current[0] < input.length && delimiters.get(input[current[0]])) {
            current[0]++;
        }
    }

    /**
     * word: ~' '(char ~[ ])*
     * e.g. String foo = "a foo bar 1 ";
     * char fooChars[] = foo.toCharArray();
     * BitSet spTab = new BitSet();
     * {
     * spTab.set(' ');
     * spTab.set('\t');
     * }
     * int current[] = new int[]{0};
     * String a = Alias.word(fooChars, current);
     * Alias.delimiters(fooChars, current, spTab);
     * String foo = Alias.word(fooChars, current);
     * Alias.delimiters(fooChars, current, spTab);
     * String bar = Alias.word(fooChars, current);
     * Alias.delimiters(fooChars, current, spTab);
     * int one = Alias.number(fooChars, current);
     */
    public static String word(char input[], int current[]) throws ParseException {
        if (current[0] >= input.length || input[current[0]] == ' ') {
            throw new ParseException("Expecting a word", current[0]);
        }
        int from = current[0]++;
        while (current[0] < input.length && input[current[0]] != ' ') {
            current[0]++;
        }
        return new String(input, from, current[0] - from);
    }

    /**
     * word: ~' '(char ~[ ])*
     */
    public static String delimitedWord(char input[], int current[], BitSet delimiters) throws ParseException {
        if (current[0] >= input.length && !delimiters.get(input[current[0]])) {
            throw new ParseException("Expecting a word", current[0]);
        }
        int from = current[0]++;
        while (current[0] < input.length && !delimiters.get(input[current[0]])) {
            current[0]++;
        }
        return new String(input, from, current[0] - from);
    }

    /**
     * return everthing from the current position on as a string
     * null if nothing
     */
    public static String remainder(char input[], int current[]) {
        if (current[0] >= input.length) {
            return null;
        }
        return new String(input, current[0], input.length - current[0]);
    }

    /**
     * null | anything but $ (\$ ok)
     */
    public static String dollarDelimitedText(char input[], int current[]) throws ParseException {
        return specialDelimitedToken(input, current, '\\', '$', false, false);
    }

    /**
     * $[$](numberParamter())[=quotedString()]
     *
     * @return Object[] {paramName, lowerParamName, int[] positions}
     */
    public static Object[] parameter(char input[], int current[]) throws ParseException {
        if (current[0] >= input.length || input[current[0]] != '$') {
            throw new ParseException("Expecting a '$'", current[0]);
        }
        current[0]++;
        Boolean isMandatory = Boolean.FALSE;
        //$$ means mandatory
        if (current[0] < input.length && input[current[0]] == '$') {
            isMandatory = Boolean.TRUE;
            current[0]++;
        }
        // check for correct input length
        if (current[0] >= input.length) {
            throw new ParseException("Expecting a number parameter (e.g. 1, 1-, -1, 1-2)", current[0]);
        }
        int positions[] = numberParameter(input, current);
        Debug.assertMsg(logger, positions.length == 3, new Throwable("wrong length"));
        String parameterName = null;
        // $1-="blah"  - look for ="
        if (current[0] + "='x'".length() < input.length // space left for arg?
                && input[current[0]] == '=' && input[current[0] + 1] == '"') {
            current[0]++; // move to "
            parameterName = quotedString(input, current, '\\');
        }
        //IPARAM_NAME = 0;
        //IPARAM_LOWERNAME = 1;
        //IPARAM_IS_MANDATORY = 2;
        //IPARAM_INDICIES = 3;
        return new Object[]{parameterName, parameterName == null ? null : parameterName.toLowerCase(), isMandatory, positions};
    }

    /**
     * n-n
     * -n
     * n-
     */
    public static int[] numberParameter(char input[], int current[]) throws ParseException {
        int fromNum = -1;
        int toNum = -1;
        if (current[0] >= input.length) {
            throw new ParseException("Unexpected end of line", input.length - 1);
        }
        if (input[current[0]] == '-') {
            current[0]++;
            fromNum = 0;
            toNum = number(input, current);
        } else {
            fromNum = number(input, current);
            toNum = fromNum;
            if (current[0] < input.length && input[current[0]] == '-') {
                current[0]++;
                if (current[0] < input.length && DIGIT[input[current[0]]]) {
                    toNum = number(input, current);
                } else {
                    toNum = Integer.MAX_VALUE;
                }
            }
        }
        int positions[] = {0, fromNum, toNum};
        return positions;
    }

    /**
     * "quoted\"String"
     */
    public static String quotedString(char input[], int current[], char escape) throws ParseException {
        if (current[0] >= input.length || input[current[0]] != '"') {
            throw new ParseException("Expecting a '\"'", current[0]);
        }
        current[0]++;
        String token = specialDelimitedToken(input, current, escape, '"', true, true);
        return token;
    }

    /**
     * (digit)*
     */
    public static int number(char input[], int current[]) throws ParseException {
        int from = current[0];
        if (current[0] >= input.length) {
            throw new ParseException("Unexpected end of line", input.length - 1);
        }
        if (!DIGIT[input[current[0]]]) {
            throw new ParseException("Expecting a DIGIT or LETTER in Command", current[0]);
        }
        current[0]++;
        while (current[0] < input.length && DIGIT[input[current[0]]]) {
            current[0]++;
        }
        return Integer.parseInt(new String(input, from, current[0] - from));
    }

    /**
     * return the next line of text.  null if at end of stream
     */
    public static String readLine(char input[], int current[]) {
        int from = current[0];
        char c = '\0';
        if (current[0] >= input.length) {
            return null;
        }
        while (true) {
            if (current[0] >= input.length) {
                break;
            }
            c = input[current[0]++];
            if ((c == '\n') || (c == '\r')) {
                break;
            }
        }
        int to = current[0];
        // step over next if: space exists, next char is \n, last was \r
        if (c == '\r') {
            to -= 1;
            if (current[0] < input.length && input[current[0]] == '\n') {
                current[0]++;
            }
        }
        if (c == '\n') {
            to -= 1;
        }
        return new String(input, from, to - from);
    }

    /**
     * returns the next special delimited token.  ignore if escape then special
     * e.g. test1\$test2 =>test1$test2
     * e.g. test$test2 => test
     * e.g. test\xtest2 => test\xtest2
     */
    public static String specialDelimitedToken(char input[], int current[], char escape, char special, boolean throwIfDelimiterNotFound, boolean eatDelimiter) throws ParseException {
        int from = current[0];
        StringBuffer text = null;
        while (true) {
            // end
            if (current[0] >= input.length) {
                if (throwIfDelimiterNotFound) {
                    throw new ParseException("Expecting a " + special, current[0]);
                }
                break;
            }
            // escape char?
            if (input[current[0]] == escape) {
                current[0]++;
                // end, not escape
                if (current[0] >= input.length) {
                    break;
                }
                // not escape
                if (input[current[0]] != special) {
                    continue;
                }
                if (text == null) {
                    text = new StringBuffer().append(input, from, current[0] - 1 - from).append(input[current[0]]);
                } else {
                    text = text.append(input, from, current[0] - 1 - from).append(input[current[0]]);
                }
                from = ++current[0];
                continue;
            }
            // parameter
            if (input[current[0]] == special) {
                if (text == null) {
                    text = new StringBuffer().append(input, from, current[0] - from);
                } else {
                    text = text.append(input, from, current[0] - from);
                }
                if (eatDelimiter) {
                    current[0]++;
                }
                return text.toString();
            }
            current[0]++;
        }
        if (text == null) {
            text = new StringBuffer().append(input, from, current[0] - from);
        } else {
            text = text.append(input, from, current[0] - from);
        }
        return text.toString();
    }

    /**
     * ParsedAlias is the 'compiled' form of an alias (faster to expand this way)
     */
    public static final class ParsedAlias {
        /**
         * the alias before it was parsed
         */
        public String rawAlias;
        /**
         * the short name of the alias.  e.g.  /alias /j /join xxx is 'j'
         */
        public String shortAliasName;
        /**
         * the long name of the alias.  (from the menu item name)
         */
        public String longAliasName;
        /**
         * from: /j /join $roomname
         * text will be '/join '
         */
        public String text;
        /**
         * argments store where each argument is inserted and the
         * user token that is used to replace the parameter
         * Object[] {String name, String lowerName, Boolean isMandatory
         * , int[text insertion point, token from, token to]}
         */
        public Vector arguments = new Vector();
        /**
         * name of the parameter
         */
        public static final int IPARAM_NAME = 0;
        /**
         * lowercase name of the parameter
         */
        public static final int IPARAM_LOWERNAME = 1;
        /**
         * is the parameter mandatory?
         */
        public static final int IPARAM_IS_MANDATORY = 2;
        /**
         * location information about the parameter, see IINDICIES*
         */
        public static final int IPARAM_INDICIES = 3;
        /**
         * where in the raw text to insert the param
         */
        public static final int IINDICIES_INSERTION_POINT = 0;
        /**
         * the index of the first user token to use for the parameter
         */
        public static final int IINDICIES_USER_TOKEN_FROM = 1;
        /**
         * the index of the last user token to use for the parameter
         */
        public static final int IINDICIES_USER_TOKEN_TO = 2;
        /**
         * a constant for a paramter name that will automatically expand to
         * the irc users current nickname
         */
        public static final String ME = "me";
        /**
         * a constant for a paramter name that will automatically expand to
         * the room the message is directed to
         */
        public static final String ROOM = "room";
        /**
         * a constant for a paramter name that will automatically expand to
         * the nick the message is directed to
         */
        public static final String NICK = "nick";
    }

    public static class MatchResult {
        /**
         * the match we found (index into the match array the user passed in)
         */
        public int match = -1;

        /**
         * the match was on a char per the bitset
         public boolean wasSingleCharMatch;
         */
        /**
         * the stop match we found
         */
        public int stop = -1;
        /**
         * here is where it was
         */
        public int foundIndex;

        /**
         * returns the index after the match
         */
        public int afterMatchIndex;

        public static void main(String args[]) {
            char chInput[] = "abcd".toCharArray();
            Object testInput[][] = {{new char[][]{"a".toCharArray()}, new char[][]{"b".toCharArray()}}, {new char[][]{"c".toCharArray()}, new char[][]{"b".toCharArray()}}, {new char[][]{"abc".toCharArray()}, new char[][]{"b".toCharArray()}}, {new char[][]{"x".toCharArray()}, new char[][]{"a".toCharArray()}}, {new char[][]{"x".toCharArray()}, new char[][]{"x".toCharArray()}}, {new char[][]{"abc".toCharArray(), "ab".toCharArray()}, new char[][]{"b".toCharArray()}}, {new char[][]{"abc".toCharArray(), "ab".toCharArray()}, new char[][]{"a".toCharArray()}}, {new char[][]{"bc".toCharArray()}, new char[][]{"ab".toCharArray()}}};
            StringBuffer output = new StringBuffer();
            for (int i = 0; i < testInput.length; i++) {
                char match[][] = (char[][]) testInput[i][0];
                char stopMatch[][] = (char[][]) testInput[i][1];
                MatchResult mr = getMatchResult("abc".toCharArray(), 0, 4, match, stopMatch, -1);
                if (mr == null) {
                    output.append("\nNo match result on match='");
                } else {
                    output.append("\nMatch result: " + (mr.match == -1 ? "" : new String(match[mr.match])) + (mr.stop == -1 ? "" : new String(stopMatch[mr.stop]) + "(stopmatch)") + "@" + mr.foundIndex + " on match='");
                }
                for (int j = 0; j < match.length; j++) {
                    output.append(new String(match[j]) + ",");
                }
                output.append("', nomatch='");
                for (int j = 0; j < stopMatch.length; j++) {
                    output.append(new String(stopMatch[j]) + ",");
                }
            }
            String expected = "\nMatch result: a@0 on match='a,', nomatch='b," + "\nMatch result: b(stopmatch)@1 on match='c,', nomatch='b," + "\nMatch result: b(stopmatch)@1 on match='abc,', nomatch='b," + "\nMatch result: a(stopmatch)@0 on match='x,', nomatch='a," + "\nNo match result on match='x,', nomatch='x," + "\nMatch result: ab@0 on match='abc,ab,', nomatch='b," + "\nMatch result: a(stopmatch)@0 on match='abc,ab,', nomatch='a," + "\nMatch result: ab(stopmatch)@0 on match='bc,', nomatch='ab,";
            if (logger.isInfoEnabled()) {
                logger.info(output.toString());
            }
            if (expected.equals(output.toString())) {
                if (logger.isInfoEnabled()) {
                    logger.info("as expected");
                }
            } else {
                Debug.assertMsg(logger, false, "Expected: '" + expected + "', not '" + output.toString());
            }
        }
    }

    private static char s_EmptyMatches[][] = new char[0][0];

    /**
     * @param expectTagsToStartHere -1 if you don't care where it starts, else the index where you
     *                              expect the match to be (else you get a null return)
     * @return null if we full off the end of the string.  MatchResult of the first
     * match or stopMatch we find
     */
    public static MatchResult getMatchResult(char ch[], int startIndex, int stopLookingIndex, char matches[][]
                                             /*
                                             , BitSet singleCharMatches
                                             , boolean matchAsManySingleCharsAsPoss
                                             */, char stopMatches[][], int expectTagsToStartHere) {
        if (matches == null) {
            matches = s_EmptyMatches;
        }
        if (stopMatches == null) {
            stopMatches = s_EmptyMatches;
        }
        int currentMatches[] = new int[matches.length];
        int currentStopMatches[] = new int[stopMatches.length];
        int currentMatchCount = 0;
        // for each character
        int chLen = ch.length;
        int matchesLen = matches.length;
        int stopMatchesLen = stopMatches.length;
        for (int i = startIndex; i < chLen && (stopLookingIndex < 0 || i < stopLookingIndex); i++) {
            // match not starting where we thought it would
            if (expectTagsToStartHere > -1 && i > expectTagsToStartHere && currentMatchCount == 0) {
                return null;
            }
            char currentChar = ch[i];
            // for each thing we're matching
            for (int im = 0; im < matchesLen; im++) {
                // match at our current match point?
                if (currentChar == matches[im][currentMatches[im]]) {
                    if (currentMatches[im] == 0) {
                        currentMatchCount++;
                    }
                    currentMatches[im]++;
                    // have we found the whole thing?
                    if (currentMatches[im] == matches[im].length) {
                        // return found
                        MatchResult ret = new MatchResult();
                        ret.foundIndex = i - matches[im].length + 1;
                        ret.afterMatchIndex = i + 1;
                        ret.match = im;
                        return ret;
                    }
                } else {
                    // no match reset
                    if (currentMatches[im] > 0) {
                        currentMatchCount--;
                    }
                    currentMatches[im] = 0;
                }
            }
            /*
            if(singleCharMatches.get(ch[i])) {
                MatchResult ret = new MatchResult();
                ret.foundIndex = i;
                ret.wasSingleCharMatch = true;
                // get the biggest match possible
                if(matchAsManySingleCharsAsPoss) {
                    for(i<ch.length && (stopLookingIndex<0 || i<stopLookingIndex); i++) {
                        if(!singleCharMatches.get(ch[i]))
                            break;
                    }
                }
                // return found
                ret.afterMatchIndex = i+1;
                return ret;
            }
            */
            // for each thing we're stop matching on
            for (int im = 0; im < stopMatchesLen; im++) {
                // match at our current match point?
                if (ch[i] == stopMatches[im][currentStopMatches[im]]) {
                    currentStopMatches[im]++;
                    // have we found the whole thing?
                    if (currentStopMatches[im] == stopMatches[im].length) {
                        // return found
                        MatchResult ret = new MatchResult();
                        ret.foundIndex = i - stopMatches[im].length + 1;
                        ret.afterMatchIndex = i + 1;
                        ret.stop = im;
                        return ret;
                    }
                } else {
                    // no match reset
                    currentStopMatches[im] = 0;
                }
            }
        }
        // nomatch
        return null;
    }
}
