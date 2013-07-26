package com.peergreen.deployment.repository.maven;

/**
 * @author Mohammed Boukada
 */
public class MavenArtifactInfo {

    public enum Type {
        REPOSITORY, GROUP_ID, ARTIFACT_ID, CLASSIFIER, VERSION
    }

    public String groupId;
    public String artifactId;
    public String version;
    public String classifier;

    public MavenArtifactInfo(String groupId, String artifactId, String version, String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
    }
}