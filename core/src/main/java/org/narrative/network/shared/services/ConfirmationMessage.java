package org.narrative.network.shared.services;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 2, 2006
 * Time: 10:19:15 AM
 */
public class ConfirmationMessage {
    private final String title;
    private final String message;
    private final Integer autoHideDelayMS;

    public static final int DEFAULT_AUTO_HIDE_DELAY_MS = 3000;

    public ConfirmationMessage(String message) {
        this(message, false);
    }

    public ConfirmationMessage(String message, boolean useDefaultAutoHideDelay) {
        this(message, useDefaultAutoHideDelay ? DEFAULT_AUTO_HIDE_DELAY_MS : null);
    }

    public ConfirmationMessage(String message, Integer autoHideDelayMS) {
        this(null, message, autoHideDelayMS);
    }

    public ConfirmationMessage(String title, String message) {
        this(title, message, false);
    }

    public ConfirmationMessage(String title, String message, boolean useDefaultAutoHideDelay) {
        this(title, message, useDefaultAutoHideDelay ? DEFAULT_AUTO_HIDE_DELAY_MS : null);
    }

    public ConfirmationMessage(String title, String message, Integer autoHideDelayMS) {
        this.title = title;
        this.message = message;
        this.autoHideDelayMS = autoHideDelayMS;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Integer getAutoHideDelayMS() {
        return autoHideDelayMS;
    }
}
