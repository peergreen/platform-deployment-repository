package com.peergreen.deployment.repository.filter;

/**
 * @author Mohammed Boukada
 */
public interface VersionFilter {
    String getMinValue();
    String getMaxValue();
    boolean isMinIncluded();
    boolean isMaxIncluded();
}
