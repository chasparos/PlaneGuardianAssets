package com.planeguardian.assets.generation.resources.vfx;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.ReproducibilityFingerprint;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.api.Transform;
import com.planeguardian.assets.generation.determinism.FingerprintBuilder;

public final class VfxFingerprints {
    private VfxFingerprints() {
    }

    public static ReproducibilityFingerprint identity(
            StableId providerId, ContractVersion providerVersion,
            ContractVersion parameterSchemaVersion, VfxGenerationRequest request) {
        FingerprintBuilder fingerprint = new FingerprintBuilder().addString("vfx-configuration/1")
                .addId(providerId).addLong(providerVersion.major()).addLong(providerVersion.minor())
                .addLong(parameterSchemaVersion.major()).addLong(parameterSchemaVersion.minor())
                .addId(request.configurationId()).addId(request.pluginId()).addLong(request.seed());
        request.parameters().forEach((id, value) -> addValue(fingerprint.addId(id), value));
        request.attachments().forEach(attachment -> addTransform(
                fingerprint.addId(attachment.socketId()), attachment.localTransform()));
        return fingerprint.build();
    }

    private static void addValue(FingerprintBuilder fingerprint, VfxValue value) {
        if (value instanceof VfxValue.Numeric numeric) {
            fingerprint.addString("numeric").addLong(numeric.components().size());
            numeric.components().forEach(component -> fingerprint.addLong(Double.doubleToLongBits(component)));
        } else if (value instanceof VfxValue.Flag flag) {
            fingerprint.addString("flag").addLong(flag.value() ? 1 : 0);
        } else if (value instanceof VfxValue.Choice choice) {
            fingerprint.addString("choice").addId(choice.value());
        } else if (value instanceof VfxValue.Resource resource) {
            fingerprint.addString("resource").addId(resource.resource().resourceId())
                    .addString(resource.resource().kind().name())
                    .addLong(resource.resource().version().major()).addLong(resource.resource().version().minor())
                    .addString(resource.fingerprint().hex());
        } else {
            throw new IllegalStateException("Unknown VFX value: " + value.getClass());
        }
    }

    private static void addTransform(FingerprintBuilder fingerprint, Transform transform) {
        fingerprint.addLong(Double.doubleToLongBits(transform.translation().x()))
                .addLong(Double.doubleToLongBits(transform.translation().y()))
                .addLong(Double.doubleToLongBits(transform.translation().z()))
                .addLong(Double.doubleToLongBits(transform.rotation().x()))
                .addLong(Double.doubleToLongBits(transform.rotation().y()))
                .addLong(Double.doubleToLongBits(transform.rotation().z()))
                .addLong(Double.doubleToLongBits(transform.rotation().w()))
                .addLong(Double.doubleToLongBits(transform.scale().x()))
                .addLong(Double.doubleToLongBits(transform.scale().y()))
                .addLong(Double.doubleToLongBits(transform.scale().z()));
    }
}
