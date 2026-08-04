package com.planeguardian.assets.generation.performance;

/** Main-stage population endpoints used for aggregate performance validation. */
public enum StageViewProfile {
    MAXIMUM_DETAIL(10),
    OVERVIEW(100);

    private final int maximumVisibleIslands;

    StageViewProfile(int maximumVisibleIslands) {
        this.maximumVisibleIslands = maximumVisibleIslands;
    }

    public int maximumVisibleIslands() { return maximumVisibleIslands; }
}
