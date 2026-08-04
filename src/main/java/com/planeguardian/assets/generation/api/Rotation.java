package com.planeguardian.assets.generation.api;

/** Engine-neutral unit quaternion in x,y,z,w order, matching glTF. */
public record Rotation(double x, double y, double z, double w) {
    public static final Rotation IDENTITY = new Rotation(0, 0, 0, 1);

    public Rotation {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(z) || !Double.isFinite(w)) {
            throw new IllegalArgumentException("Rotation components must be finite");
        }
        double lengthSquared = x * x + y * y + z * z + w * w;
        if (Math.abs(lengthSquared - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Rotation must be a unit quaternion");
        }
    }
}
