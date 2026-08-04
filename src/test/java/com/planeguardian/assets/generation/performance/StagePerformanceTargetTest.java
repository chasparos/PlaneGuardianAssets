package com.planeguardian.assets.generation.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StagePerformanceTargetTest {
    @Test
    void stageEndpointsArePinnedAsExecutableContracts() {
        StagePerformanceTarget near = StagePerformanceTarget.upperLimit(StageViewProfile.MAXIMUM_DETAIL);
        StagePerformanceTarget overview = StagePerformanceTarget.upperLimit(StageViewProfile.OVERVIEW);

        assertEquals(10, near.visibleIslands());
        assertEquals(100, overview.visibleIslands());
        assertTrue(near.projectedSizeDrivenLod());
        assertTrue(overview.nonIslandContentPrimarilyBillboardsTexturesAndUi());
    }

    @Test
    void targetCannotExceedItsPopulationEndpoint() {
        assertThrows(IllegalArgumentException.class,
                () -> new StagePerformanceTarget(StageViewProfile.MAXIMUM_DETAIL, 11, true, true));
        assertThrows(IllegalArgumentException.class,
                () -> new StagePerformanceTarget(StageViewProfile.OVERVIEW, 100, false, true));
    }
}
