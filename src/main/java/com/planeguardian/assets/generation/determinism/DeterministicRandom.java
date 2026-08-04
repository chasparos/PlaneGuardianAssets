package com.planeguardian.assets.generation.determinism;

/** Specified SplitMix64 stream; results do not depend on JDK random implementations. */
public final class DeterministicRandom {
    private long state;

    DeterministicRandom(long seed) {
        state = seed;
    }

    public long nextLong() {
        long value = (state += 0x9E3779B97F4A7C15L);
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        long mask = bound - 1L;
        long candidate = nextLong() >>> 1;
        if ((bound & mask) == 0) {
            return (int) (candidate & mask);
        }
        long result = candidate % bound;
        while (candidate + mask - result < 0L) {
            candidate = nextLong() >>> 1;
            result = candidate % bound;
        }
        return (int) result;
    }
}
