package com.peergreen.deployment.repository.maven;

/**
 * @author Mohammed Boukada
 */
public class MavenArtifactInfo {

    public enum Type {
        REPOSITORY, GROUP_ID, ARTIFACT_ID, CLASSIFIER, VERSION, ARCHIVE
    }

    public String repository;
    public String groupId;
    public String artifactId;
    public String version;
    public String classifier;
    public Type type;

    public MavenArtifactInfo(String repository, String groupId, String artifactId, String version, String classifier, Type type) {
        this.repository = repository;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }
}
