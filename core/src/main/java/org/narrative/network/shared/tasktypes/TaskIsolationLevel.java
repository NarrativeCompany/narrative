package org.narrative.network.shared.tasktypes;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Apr 19, 2007
 * Time: 9:57:11 AM
 */
public enum TaskIsolationLevel {

    //Task is totally isolated in a new partition group, and all current sessions are cleared
    ISOLATED(true, false),

    //Task is isolated in a new partition group and the specific PartitionType for this task gets a new session, but
    //any current sessions from the other partition types are left open from the previous partition group
    ISOLATED_KEEP_CURRENT_SESSIONS(true, true),

    //Task is not isolated, and is still a member of the current partition group. All sessoins are left open and the
    //session for the current partition type is reused if possible
    NOT_ISOLATED(false, true);

    final boolean isolated;
    final boolean keepingCurrentSessions;

    TaskIsolationLevel(boolean isolated, boolean keepCurrentSessions) {
        this.isolated = isolated;
        this.keepingCurrentSessions = keepCurrentSessions;
    }

    public boolean isIsolated() {
        return isolated;
    }

    public boolean isKeepingCurrentSessions() {
        return keepingCurrentSessions;
    }
}
