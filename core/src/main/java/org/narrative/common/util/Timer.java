package org.narrative.common.util;

import org.narrative.common.util.processes.GenericProcess;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Oct 15, 2004
 * Time: 12:07:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Timer {
    private final NarrativeLogger logger;
    private final GenericProcess process;
    private long startTime;
    private String description;

    /**
     * Creates an object that can be used to store timing information
     */
    public Timer(NarrativeLogger logger) {
        this(logger, null);
    }

    public Timer(NarrativeLogger logger, GenericProcess process) {
        this.logger = logger;
        this.process = process;
    }

    /**
     * Logs a timing message if the debug level is set to Debug.TIMING.  Message will prepend your description to the
     * number of ms that the operation took since the timer started or the last reset.
     *
     * @param description
     */
    public void start(String description) {
        startTime = System.currentTimeMillis();
        this.description = description;

        if (process != null) {
            process.updateStatusMessageAndLog(logger, "Starting: " + description);
        } else if (logger.isInfoEnabled()) {
            logger.info("Starting: " + description);
        }
    }

    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public void finish() {
        if (process != null) {
            process.updateStatusMessageAndLog(logger, "Finished: " + description + ": " + getDuration() + "ms");
        } else if (logger.isInfoEnabled()) {
            logger.info("Finished: " + description + ": " + getDuration() + "ms");
        }
    }
}
