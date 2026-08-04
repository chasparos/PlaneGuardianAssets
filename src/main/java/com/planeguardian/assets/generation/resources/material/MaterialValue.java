package com.planeguardian.assets.generation.resources.material;

import com.planeguardian.assets.generation.api.GeneratedResourceRef;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.ResourceKind;

import java.util.List;
import java.util.Objects;

/** Closed engine-neutral value set for versioned material recipe inputs. */
public sealed interface MaterialValue permits MaterialValue.Numeric, MaterialValue.Flag, MaterialValue.Texture {
    record Numeric(List<Double> components) implements MaterialValue {
        public Numeric {
            components = List.copyOf(components);
            if (components.isEmpty() || components.size() > 4
                    || components.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
                throw new IllegalArgumentException("Numeric material values require one to four finite components");
            }
        }
    }

    record Flag(boolean value) implements MaterialValue {
    }

    record Texture(GeneratedResourceRef resource, ReproducibilityFingerprint fingerprint) implements MaterialValue {
        public Texture {
            Objects.requireNonNull(resource, "resource");
            Objects.requireNonNull(fingerprint, "fingerprint");
            if (resource.kind() != ResourceKind.TEXTURE) throw new IllegalArgumentException("Material texture value must reference a texture");
        }
    }
}
