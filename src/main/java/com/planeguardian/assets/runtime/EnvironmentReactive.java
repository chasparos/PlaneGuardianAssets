package com.planeguardian.assets.runtime;

/** Optional per-frame behavior; static loaded assets simply omit it. */
public interface EnvironmentReactive extends RuntimeCapability {
    void update(double deltaSeconds, EnvironmentState environment);
}
