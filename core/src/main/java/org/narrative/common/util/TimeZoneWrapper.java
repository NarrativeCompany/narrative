package org.narrative.common.util;

import org.narrative.common.persistence.PersistenceUtil;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 7/27/11
 * Time: 11:30 AM
 *
 * @author brian
 */
public class TimeZoneWrapper implements NameForDisplayProvider {

    // bl: we used to use the server default time zone in places. now, we'll just be consistent and always
    // use America/Los_Angeles, which also ensures that the default time zone will match one of the detected
    // time zones from our time zone detection library.
    // could consider making this a setting eventually.
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(PersistenceUtil.MySQLUtils.DEFAULT_TIMEZONE);

    private final TimeZone timeZone;
    private final Locale locale;

    public TimeZoneWrapper(TimeZone timeZone, Locale locale) {
        this.timeZone = timeZone;
        this.locale = locale;
    }

    public String getNameForDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTimeZoneIDFormatted(timeZone));
        sb.append(" - ");
        sb.append(timeZone.getDisplayName(locale));
        sb.append(" (GMT");
        int offset = timeZone.getOffset(System.currentTimeMillis());
        if (offset != 0) {
            int hours = offset / IPDateUtil.HOUR_IN_MS;
            int minutes = (offset % IPDateUtil.HOUR_IN_MS) / IPDateUtil.MINUTE_IN_MS;
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(0);
            nf.setMaximumIntegerDigits(2);
            nf.setMinimumIntegerDigits(2);
            sb.append(hours > 0 ? "+" : "-").append(Math.abs(hours)).append(":").append(nf.format(Math.abs(minutes)));
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getTimeZoneIDFormatted(TimeZone timeZone) {
        return timeZone.getID().replaceAll("\\_", " ");
    }

    private static final List<TimeZone> ALL_TIME_ZONES;

    static {
        Pattern pattern = Pattern.compile("^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific|Antarctica)/.*");
        Map<String, TimeZone> timeZones = newTreeMap();
        // bl: the javascript timezone detection library that we're using may use one of these values.
        // bl: but there's no reason to include them in our TimeZoneWrapper list, so not going to include them here.
        //Set<String> forcedTimeZoneIds = newHashSet(Arrays.asList("Etc/GMT+12", "Etc/GMT+2", "Etc/UTC"));
        for (String id : TimeZone.getAvailableIDs()) {
            // bl: allow selection of UTC as the time zone
            if (!pattern.matcher(id).matches() && !IPDateUtil.UTC_TIMEZONE.getID().equals(id)) {
                continue;
            }

            TimeZone timeZone = TimeZone.getTimeZone(id);

            // bl: ignore short IDs and those that are Etc.
            // "3 Character time zone codes are deprecated in JRE 1.5 because there are ambiguities:
            // "CST" could be U.S. "Central Standard Time" or "China Standard Time"
            // refer: http://www.sencha.com/forum/showthread.php?63307-Anyone-come-up-with-a-better-UI-for-timezone-selection&s=74fe7419d9dbef232fdebc8b25107749&p=306141&viewfull=1#post306141
            /*if(id.length() <= 3 || id.startsWith("Etc") || id.startsWith("SystemV") || id.startsWith("EST") || id.startsWith("CST") || id.startsWith("MST") || id.startsWith("PST")) {
                throw UnexpectedError.getRuntimeException("Found an unsupported TimeZone! timeZone id/" + id);
            }
            String name = timeZone.getDisplayName();

            // Don't include zones with generic "GMT+n" descriptions.
            if(name.startsWith("GMT-") || name.startsWith("GMT+")) {
                throw UnexpectedError.getRuntimeException("Found an unsupported TimeZone by name! timeZone id/" + id + " name/" + timeZone.getDisplayName());
            }*/
            timeZones.put(id, timeZone);
        }
        ALL_TIME_ZONES = Collections.unmodifiableList(newArrayList(timeZones.values()));
    }

    public static List<TimeZoneWrapper> getAllTimeZones(Locale locale) {
        List<TimeZoneWrapper> ret = newArrayList(ALL_TIME_ZONES.size());
        for (TimeZone timeZone : ALL_TIME_ZONES) {
            ret.add(new TimeZoneWrapper(timeZone, locale));
        }
        return ret;
    }

    public static TimeZoneWrapper getWrappedTimeZoneForCurrentContext(TimeZone timeZone) {
        if (timeZone == null) {
            return null;
        }
        return new TimeZoneWrapper(timeZone, networkContext().getLocale());
    }

    public static void main(String[] args) throws Exception {
        Map<Integer, Set<TimeZone>> map = new TreeMap<Integer, Set<TimeZone>>();

        for (String id : TimeZone.getAvailableIDs()) {
            TimeZone timeZone = TimeZone.getTimeZone(id);
            Set<TimeZone> timeZones = map.get(timeZone.getRawOffset());
            if (timeZones == null) {
                map.put(timeZone.getRawOffset(), timeZones = newLinkedHashSet());
            }
            timeZones.add(timeZone);
        }

        System.out.println("Time Zone Count: " + map.size());

        System.out.println("Default Time Zone: " + TimeZone.getDefault());

        for (Map.Entry<Integer, Set<TimeZone>> entry : map.entrySet()) {
            int rawOffset = entry.getKey();
            Set<TimeZone> timeZones = entry.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append("Offset:\t");
            sb.append((double) rawOffset / IPDateUtil.HOUR_IN_MS);
            sb.append("\n");
            for (TimeZone timeZone : timeZones) {
                sb.append("\t\t\t");
                sb.append(timeZone.getDisplayName());
                sb.append("---");
                sb.append(timeZone.getID());
                sb.append("\n");
            }
            System.out.println(sb);
        }

        System.out.println("\n\n\n\n\n\n");

        for (TimeZone timeZone : ALL_TIME_ZONES) {
            System.out.println(timeZone.getID() + " -> " + timeZone.getDisplayName());
        }

        System.out.println("\n\n\n\n\n\n");

        for (TimeZone timeZone : ALL_TIME_ZONES) {
            System.out.println(new TimeZoneWrapper(timeZone, Locale.getDefault()).getNameForDisplay());
        }

        System.out.println("Time Zone Count: " + ALL_TIME_ZONES.size());

        // bl: this is the list of time zones supported by the javascript time zone detection library.
        List<String> javascriptDetectedTimeZones = Arrays.asList("Etc/GMT+12", "Pacific/Pago_Pago", "America/Adak", "Pacific/Apia", "Pacific/Honolulu", "Pacific/Marquesas", "Pacific/Gambier", "America/Anchorage", "America/Los_Angeles", "Pacific/Pitcairn", "America/Phoenix", "America/Denver", "America/Guatemala", "America/Chicago", "Pacific/Easter", "America/Bogota", "America/New_York", "America/Caracas", "America/Halifax", "America/Santo_Domingo", "America/Asuncion", "America/St_Johns", "America/Godthab", "America/Argentina/Buenos_Aires", "America/Montevideo", "America/Noronha", "Etc/GMT+2", "Atlantic/Azores", "Atlantic/Cape_Verde", "Etc/UTC", "Europe/London", "Europe/Berlin", "Africa/Lagos", "Africa/Windhoek", "Asia/Beirut", "Africa/Johannesburg", "Europe/Moscow", "Asia/Baghdad", "Asia/Tehran", "Asia/Dubai", "Asia/Yerevan", "Asia/Kabul", "Asia/Yekaterinburg", "Asia/Karachi", "Asia/Kolkata", "Asia/Kathmandu", "Asia/Dhaka", "Asia/Omsk", "Asia/Rangoon", "Asia/Krasnoyarsk", "Asia/Jakarta", "Asia/Shanghai", "Asia/Irkutsk", "Australia/Eucla", "Australia/Eucla", "Asia/Yakutsk", "Asia/Tokyo", "Australia/Darwin", "Australia/Adelaide", "Australia/Brisbane", "Asia/Vladivostok", "Australia/Sydney", "Australia/Lord_Howe", "Asia/Kamchatka", "Pacific/Noumea", "Pacific/Norfolk", "Pacific/Auckland", "Pacific/Tarawa", "Pacific/Chatham", "Pacific/Tongatapu", "Pacific/Kiritimati");

        System.out.println("\n\n\n\n\n\n");

        outer:
        for (String timeZoneId : javascriptDetectedTimeZones) {
            for (TimeZone timeZone : ALL_TIME_ZONES) {
                if (timeZone.getID().equals(timeZoneId)) {
                    continue outer;
                }
            }
            System.out.println("Missing time zone for: " + timeZoneId);
        }
    }

}
