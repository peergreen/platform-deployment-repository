package com.peergreen.deployment.repository.search;

import com.peergreen.deployment.repository.filter.AndFilter;
import com.peergreen.deployment.repository.filter.Filter;
import com.peergreen.deployment.repository.filter.NotFilter;
import com.peergreen.deployment.repository.filter.OrFilter;
import com.peergreen.deployment.repository.filter.StringFilter;
import com.peergreen.deployment.repository.filter.VersionFilter;
import com.peergreen.deployment.repository.maven.ArtifactIdQuery;
import com.peergreen.deployment.repository.maven.ClassifierQuery;
import com.peergreen.deployment.repository.maven.GroupIdQuery;
import com.peergreen.deployment.repository.maven.VersionQuery;

/**
 * @author Mohammed Boukada
 */
public class Queries {

    public static Filter and(Filter... args) {
        return new AndFilter(args);
    }

    public static Filter or(Filter... args) {
        return new OrFilter(args);
    }

    public static Filter not(Filter... args) {
        return new NotFilter(args);
    }

    public static Filter eq(String s) {
        return new StringFilter(s);
    }

    public static Filter startsWith(String s) {
        return new StringFilter(s + "*");
    }

    public static Filter endsWith(String s) {
        return new StringFilter("*" + s);
    }

    public static Filter contains(String s) {
        return new StringFilter("*" + s + "*");
    }

    public static VersionFilter greaterThan(String s, boolean include) {
        return null;
    }

    public static VersionFilter lowerThan(String s, boolean include) {
        return null;
    }

    public static VersionFilter between(String inf, String sup, boolean includeInf, boolean includeSup) {
        return null;
    }

    public static Query from(String... repositories) {
        return new RepositoryQuery(repositories);
    }

    public static Query groupId(String groupId) {
        GroupIdQuery gid = new GroupIdQuery(groupId);
        return gid.getQuery();
    }

    public static Query groupId(Filter filter) {
        GroupIdQuery gid = new GroupIdQuery(filter);
        return gid.getQuery();
    }

    public static Query artifactId(String artifactId) {
        ArtifactIdQuery aid = new ArtifactIdQuery(artifactId);
        return aid.getQuery();
    }

    public static Query artifactId(Filter filter) {
        ArtifactIdQuery aid = new ArtifactIdQuery(filter);
        return aid.getQuery();
    }

    public static Query classifier(String classifier) {
        ClassifierQuery cla = new ClassifierQuery(classifier);
        return cla.getQuery();
    }

    public static Query classifier(Filter filter) {
        ClassifierQuery cla = new ClassifierQuery(filter);
        return cla.getQuery();
    }

    public static Query version(String version) {
        VersionQuery ver = new VersionQuery(version);
        return ver.getQuery();
    }

    public static Query version(Filter filter) {
        VersionQuery ver = new VersionQuery(filter);
        return ver.getQuery();
    }

    public static Query version(VersionFilter versionFilter) {
        VersionQuery ver = new VersionQuery(versionFilter);
        return ver.getQuery();
    }
}
