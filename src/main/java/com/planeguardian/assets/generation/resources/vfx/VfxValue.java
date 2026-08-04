package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;

import java.util.List;
import java.util.Objects;

/** Closed engine-neutral value set for VFX plugin parameters. */
public sealed interface VfxValue permits VfxValue.Numeric, VfxValue.Flag, VfxValue.Choice, VfxValue.Resource {
    record Numeric(List<Double> components) implements VfxValue {
        public Numeric {
            components = List.copyOf(components);
            if (components.isEmpty() || components.size() > 4
                    || components.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
                throw new IllegalArgumentException("Numeric VFX values require one to four finite components");
            }
        }
    }

    record Flag(boolean value) implements VfxValue {
    }

    record Choice(StableId value) implements VfxValue {
        public Choice { Objects.requireNonNull(value, "value"); }
    }

    record Resource(GeneratedResourceRef resource, ReproducibilityFingerprint fingerprint) implements VfxValue {
        public Resource {
            Objects.requireNonNull(resource, "resource");
            Objects.requireNonNull(fingerprint, "fingerprint");
        }
    }
}
