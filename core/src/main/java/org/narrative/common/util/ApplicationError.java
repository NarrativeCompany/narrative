package org.narrative.common.util;

import org.narrative.network.customizations.narrative.service.api.model.ErrorType;

public class ApplicationError extends NarrativeException {

    private String title;
    private final StringBuilder systemLogInfo = new StringBuilder();
    private Integer statusCodeOverride;

    public ApplicationError(String message) {
        super(message);
    }

    public ApplicationError(String title, String message) {
        super(message);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void appendSystemLogInfo(String logInfo) {
        if (systemLogInfo.length() == 0) {
            systemLogInfo.append("\n");
        }
        systemLogInfo.append(logInfo);
    }

    public String getSystemLogInfo() {
        return systemLogInfo.toString();
    }

    public Integer getStatusCodeOverride() {
        return statusCodeOverride;
    }

    public void setStatusCodeOverride(Integer statusCodeOverride) {
        this.statusCodeOverride = statusCodeOverride;
    }

    public ErrorType getErrorType() {
        return null;
    }

    public Object getErrorDetailObject() {
        return null;
    }
}
