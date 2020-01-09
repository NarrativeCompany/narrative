package org.narrative.network.shared.util;

import org.narrative.common.util.NarrativeConstants;

/**
 * Date: Jul 27, 2006
 * Time: 3:42:07 PM
 *
 * @author Brian
 */
public class NetworkConstants extends NarrativeConstants {

    public static final int MIN_URL_LENGTH = 4;
    public static final int MAX_URL_LENGTH = 255;

    public static final int MIN_SUBJECT_LENGTH = 1;
    public static final int MAX_SUBJECT_LENGTH = 255;

    public static final int MIN_PRETTY_URL_STRING_LENGTH = 0;
    public static final int MAX_PRETTY_URL_STRING_LENGTH = 255;

    // bl: the MEDIUMTEXT field supports up to 16777215 characters.  that's a lot ;)  let's scale
    // back the limit to 500,000 characters.
    public static final int MAX_BODY_LENGTH = 500000;

    public static final int MIN_FQDN_LENGTH = 4;
    public static final int MAX_FQDN_LENGTH = 255;

}
