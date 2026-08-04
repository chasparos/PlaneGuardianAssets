package com.planeguardian.assets.generation.tree;

import com.planeguardian.assets.generation.api.Contribution;

import java.util.ArrayList;
import java.util.List;

/** Resolves bounded semantic profile effects without allowing host semantics to overwrite native crown identity. */
public final class TreeSemanticAdapter {
    private static final double CROWN_BASE = .72;
    private static final double CROWN_LIFE = .24;
    private static final double CROWN_DEATH = -.48;
    private static final double CROWN_CREATION = .12;
    private static final double CROWN_ANNIHILATION = -.24;
    private static final double CROWN_WATER = .10;
    private static final double CROWN_FIRE = -.12;

    private TreeSemanticAdapter() {
    }

    public static ResolvedTreeFeatures resolve(TreeSemanticInputs inputs, TreeCrownSettings baseline) {
        if (inputs == null || baseline == null) {
            throw new IllegalArgumentException("inputs and baseline must not be null");
        }
        TreeSemanticProfile profile = inputs.intrinsic();
        List<Contribution> contributions = new ArrayList<>();
        double coverage = resolveCoverage(profile, contributions);
        double moss = clamp(.10 + .26 * blendedWater(inputs) + .18 * positive(profile.vitality())
                + .12 * positive(-profile.transformation()) - .20 * profile.fire());
        double vines = clamp(-.04 + .24 * blendedWater(inputs) + .18 * positive(profile.vitality())
                + .16 * positive(profile.transformation()) - .20 * profile.fire());
        double flowers = clamp(-.05 + .28 * positive(profile.vitality()) + .38 * positive(profile.genesis())
                - .24 * positive(-profile.vitality()) - .30 * positive(-profile.genesis()));
        double fruit = clamp(-.02 + .22 * positive(profile.vitality()) + .28 * positive(profile.genesis())
                + .12 * positive(-profile.transformation()) - .22 * positive(-profile.genesis()));
        double fungi = clamp(-.06 + .34 * positive(-profile.vitality()) + .22 * positive(-profile.transformation())
                + .16 * blendedWater(inputs) - .12 * positive(profile.genesis()));
        contributions.add(new Contribution("tree.moss.coverage", "resolved formula", moss,
                "bounded water, vitality, preservation, and fire response"));
        contributions.add(new Contribution("tree.feature.vine-coverage", "resolved formula", vines,
                "bounded water, vitality, transformation, and fire response"));
        contributions.add(new Contribution("tree.feature.flower-density", "resolved formula", flowers,
                "bounded vitality, creation, death, and annihilation response"));
        contributions.add(new Contribution("tree.feature.fruit-density", "resolved formula", fruit,
                "bounded vitality, creation, preservation, and annihilation response"));
        contributions.add(new Contribution("tree.feature.fungal-coverage", "resolved formula", fungi,
                "bounded death, preservation, water, and creation response"));
        return new ResolvedTreeFeatures(new TreeCrownSettings(coverage, baseline.maximumClusters(),
                baseline.widthRatio(), baseline.heightRatio(), baseline.verticalOffsetRatio(),
                baseline.latitudeBands(), baseline.radialSegments()), moss, vines, flowers, fruit, fungi, contributions);
    }

    private static double resolveCoverage(TreeSemanticProfile profile, List<Contribution> contributions) {
        double life = positive(profile.vitality());
        double death = positive(-profile.vitality());
        double creation = positive(profile.genesis());
        double annihilation = positive(-profile.genesis());
        add(contributions, "baseline", CROWN_BASE);
        add(contributions, "life", CROWN_LIFE * life);
        add(contributions, "death", CROWN_DEATH * death);
        add(contributions, "creation", CROWN_CREATION * creation);
        add(contributions, "annihilation", CROWN_ANNIHILATION * annihilation);
        add(contributions, "water", CROWN_WATER * profile.water());
        add(contributions, "fire", CROWN_FIRE * profile.fire());
        return clamp(CROWN_BASE + CROWN_LIFE * life + CROWN_DEATH * death + CROWN_CREATION * creation
                + CROWN_ANNIHILATION * annihilation + CROWN_WATER * profile.water() + CROWN_FIRE * profile.fire());
    }

    private static void add(List<Contribution> contributions, String source, double amount) {
        contributions.add(new Contribution("tree.crown.coverage", source, amount,
                "version-one bounded semantic crown contribution"));
    }

    private static double blendedWater(TreeSemanticInputs inputs) {
        return inputs.intrinsic().water() * (1 - inputs.hostInfluence()) + inputs.host().water() * inputs.hostInfluence();
    }

    private static double positive(double value) {
        return Math.max(0, value);
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
