package org.narrative.network.core.system;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 9, 2006
 * Time: 12:18:50 AM
 *
 * This class is solely for the seeing current repository number/build number
 * from application.
 */

public enum NetworkVersion {
    INSTANCE;

    private long jenkinsBuild;
    private String branch;
    private String gitSha;
    private String version;

    public void init(String version, long jenkinsBuild, String branch, String gitSha) {
        this.jenkinsBuild = jenkinsBuild;
        this.branch = branch;
        this.gitSha = gitSha;

        // bl: git commit IDs are 40-character SHA-1 checksums. a short, 7-char version is used universally
        // as well, so let's use that in our basic versioning for URLs, etc.
        if (gitSha != null && gitSha.length() > 7) {
            gitSha = gitSha.substring(0, 7);
        }

        // bl: the version now includes the build number, if applicable
        this.version = version + "-" + gitSha;
    }

    /**
     * Version, incorporating the version, Jenkins build number, and git revision
     *
     * @return version of the form: 1.0.0{-LOCAL|SNAPSHOT|RC|HOTFIX-123}-abcdefg
     */
    public String getVersion() {
        return version;
    }

    public long getJenkinsBuild() {
        return jenkinsBuild;
    }

    public String getBranch() {
        return branch;
    }

    public String getGitSha() {
        return gitSha;
    }
}
