package org.narrative.common.web;

import org.narrative.common.core.services.interceptors.SubPropertySettable;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 * The DateAndTime object is used for marshalling/demarshalling date & time fields from the UI.
 * Anywhere that DateAndTime is accessible in the UI, it should have SubPropertySettable
 * specified with a depth of 2.  Without the depth of 2, the internal TimeOfDay field
 * in DateAndTime won't be able to be set.
 * <p>
 * Date: Aug 31, 2006
 * Time: 7:40:06 AM
 *
 * @author Brian
 */
public class DateTimeInput implements Validatable {
    private final TimeInput timeInput;
    private final DateInput dateInput;

    public static String TIME_INPUT_PARAM = "timeInput";
    public static String DATE_INPUT_PARAM = "dateInput";

    public DateTimeInput() {
        dateInput = new DateInput();
        timeInput = new TimeInput(dateInput.cal, false);
    }

    public DateTimeInput(Timestamp timestamp, TimeZone timeZone) {
        if (timestamp != null) {
            dateInput = new DateInput(timestamp, timeZone);
            timeInput = new TimeInput(dateInput.cal, true);
        } else {
            dateInput = new DateInput(timeZone);
            timeInput = new TimeInput(dateInput.cal, false);
        }
    }

    public DateTimeInput(Timestamp timestamp) {
        this(timestamp, null);
    }

    public DateTimeInput(Calendar cal) {
        if (cal != null) {
            dateInput = new DateInput(cal);
            timeInput = new TimeInput(dateInput.cal, true);
        } else {
            dateInput = new DateInput();
            timeInput = new TimeInput(dateInput.cal, false);
        }
    }

    public void setup(long millis) {
        dateInput.setup(millis);
        timeInput.setup(millis);
    }

    public Timestamp getTimestamp() {
        // bl: this timestamp is used for display on the page, even in the event of errors, so we need to return the
        // timestamp representing the time anytime that the date is set.  otherwise, error pages won't render with the
        // date pre-filled.  note that any time isValid() returns false, getTimestamp shouldn't be fully trusted.
        if (!isValid()) {
            return null;
        }
        return new Timestamp(dateInput.cal.getTimeInMillis());
    }

    @SubPropertySettable
    public DateInput getDateInput() {
        return dateInput;
    }

    @SubPropertySettable
    public TimeInput getTimeInput() {
        return timeInput;
    }

    public boolean isValid() {
        // in order for the DateAndTime to be valid, the date must
        // be set and the TimeOfDay must be valid.
        return dateInput.isValid() && timeInput.isValid();
    }

    public void addParameters(String prefix, Map<String, Object> parameters) {
        if (!isValid()) {
            return;
        }
        dateInput.addParameter(prefix + "." + DATE_INPUT_PARAM, parameters);
        timeInput.addParameter(prefix + "." + TIME_INPUT_PARAM, parameters);
    }

}
