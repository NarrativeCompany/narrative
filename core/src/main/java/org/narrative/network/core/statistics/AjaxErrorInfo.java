package org.narrative.network.core.statistics;

import org.narrative.common.persistence.OID;
import org.narrative.network.shared.security.PrimaryRole;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: Mar 28, 2007
 * Time: 9:09:00 AM
 *
 * @author Brian
 */
public class AjaxErrorInfo implements ErrorInfo<AjaxErrorInfo.ReferenceIdInfo> {
    private final String error;
    private final String stackTrace;
    private final int stackTraceHashcode;
    private final Queue<ReferenceIdInfo> referenceIdInfos = new ConcurrentLinkedQueue<ReferenceIdInfo>();
    private int count = 0;

    public AjaxErrorInfo(String error, String stackTrace) {
        this.error = error;
        this.stackTrace = stackTrace;
        this.stackTraceHashcode = stackTrace.hashCode();
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public int getStackTraceHashcode() {
        return stackTraceHashcode;
    }

    @Override
    public int getCount() {
        return count;
    }

    public void addReferenceIdInfo(PrimaryRole primaryRole, OID visitOid, Timestamp timestamp, String referenceId, String debugHtml) {
        count++;
        referenceIdInfos.add(new ReferenceIdInfo(primaryRole, visitOid, timestamp, referenceId, debugHtml));
        while (referenceIdInfos.size() > 20) {
            // bl: just do a poll, not a remove so that we won't get an exception in case there aren't any more items
            // in the queue due to multiple threads accessing this code at the same time.
            referenceIdInfos.poll();
        }
    }

    @Override
    public List<ReferenceIdInfo> getRequestInfos() {
        return new ArrayList<ReferenceIdInfo>(referenceIdInfos);
    }

    public static class ReferenceIdInfo {
        private final String displayName;
        private final OID primaryRoleOid;
        private final boolean isUser;
        private final OID visitOid;
        private final Timestamp timestamp;
        private final String referenceId;
        private final String debugHtml;

        public ReferenceIdInfo(PrimaryRole primaryRole, OID visitOid, Timestamp timestamp, String referenceId, String debugHtml) {
            if (primaryRole != null) {
                this.displayName = primaryRole.getDisplayNameResolved();
                this.primaryRoleOid = primaryRole.getOid();
                this.isUser = primaryRole.isRegisteredUser();

            } else {
                this.displayName = null;
                this.primaryRoleOid = null;
                this.isUser = false;
            }
            this.visitOid = visitOid;
            this.timestamp = timestamp;
            this.referenceId = referenceId;
            this.debugHtml = debugHtml;
        }

        public String getDisplayName() {
            return displayName;
        }

        public OID getPrimaryRoleOid() {
            return primaryRoleOid;
        }

        public boolean isUser() {
            return isUser;
        }

        public OID getVisitOid() {
            return visitOid;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public String getDebugHtml() {
            return debugHtml;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<strong>");
            sb.append(timestamp);
            sb.append("</strong>");
            sb.append(" ReferenceId/");
            sb.append(referenceId);
            sb.append(isUser ? " User/" : " Guest/");
            sb.append(displayName);
            sb.append("/");
            sb.append(primaryRoleOid);
            sb.append(" Visit/");
            sb.append(visitOid);
            sb.append(" Debug:<br>");
            sb.append(debugHtml);
            return sb.toString();
        }
    }
}
