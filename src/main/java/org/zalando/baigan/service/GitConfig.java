package org.zalando.baigan.service;

public class GitConfig {
    private String repoRefs;
    private String repoName;
    private String repoOwner;
    private String gitHost;
    private String oauthToken;
    private String sourceFile;

    public GitConfig(String gitHost, String repoOwner, String repoName,
            String repoRefs, String sourceFile, String oauthToken) {
        this.repoRefs = repoRefs;
        this.repoName = repoName;
        this.repoOwner = repoOwner;
        this.gitHost = gitHost;
        this.oauthToken = oauthToken;
        this.sourceFile = sourceFile;
    }

    public String getRepoRefs() {
        return repoRefs;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getGitHost() {
        return gitHost;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public String getSourceFile() {
        return sourceFile;
    }

}