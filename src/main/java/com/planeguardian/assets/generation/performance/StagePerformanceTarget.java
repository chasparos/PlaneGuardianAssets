package com.planeguardian.assets.generation.performance;

import java.util.Objects;

/** A measurable aggregate stage target; numeric budgets are populated by profiling. */
public record StagePerformanceTarget(
        StageViewProfile viewProfile,
        int visibleIslands,
        boolean projectedSizeDrivenLod,
        boolean nonIslandContentPrimarilyBillboardsTexturesAndUi) {

    public StagePerformanceTarget {
        Objects.requireNonNull(viewProfile, "viewProfile");
        if (visibleIslands < 0 || visibleIslands > viewProfile.maximumVisibleIslands()) {
            throw new IllegalArgumentException("Visible island count exceeds the view profile");
        }
        if (!projectedSizeDrivenLod) throw new IllegalArgumentException("Stage targets require projected-size LOD");
    }

    public static StagePerformanceTarget upperLimit(StageViewProfile profile) {
        return new StagePerformanceTarget(profile, profile.maximumVisibleIslands(), true, true);
    }
}
