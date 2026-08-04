package com.planeguardian.assets.generation.determinism;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterminismTest {
    @Test
    void splitMix64SequenceIsPinned() {
        DeterministicRandom random = new DeterministicRandom(0L);
        assertEquals(0xE220A8397B1DCDAFL, random.nextLong());
        assertEquals(0x6E789E6AA1B965F4L, random.nextLong());
        assertEquals(0x06C45D188009454FL, random.nextLong());
    }

    @Test
    void namedStreamsAreRepeatableAndIndependentOfOpeningOrder() {
        long structureFirst = NamedRandomStreams.open(42L, "tree.structure").nextLong();
        long foliage = NamedRandomStreams.open(42L, "tree.foliage").nextLong();
        long structureAfterOtherStream = NamedRandomStreams.open(42L, "tree.structure").nextLong();

        assertEquals(structureFirst, structureAfterOtherStream);
        assertNotEquals(structureFirst, foliage);
        assertThrows(IllegalArgumentException.class, () -> NamedRandomStreams.open(42L, " "));
    }

    @Test
    void boundedIntegersStayWithinTheirContract() {
        DeterministicRandom random = NamedRandomStreams.open(7L, "bounds");
        for (int i = 0; i < 10_000; i++) {
            int value = random.nextInt(7);
            assertTrue(value >= 0 && value < 7);
        }
        assertThrows(IllegalArgumentException.class, () -> random.nextInt(0));
    }

    @Test
    void quantizationAndFingerprintsAreCanonical() {
        NumericQuantizer quantizer = new NumericQuantizer(0.01);
        assertEquals(1.23, quantizer.quantize(1.234), 0.0);
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(quantizer.quantize(-0.0)));

        String first = new FingerprintBuilder().addString("tree").addLong(9)
                .addQuantized(1.234, quantizer).build().hex();
        String repeated = new FingerprintBuilder().addString("tree").addLong(9)
                .addQuantized(1.234, quantizer).build().hex();
        String changed = new FingerprintBuilder().addString("tree").addLong(10)
                .addQuantized(1.234, quantizer).build().hex();

        assertEquals(first, repeated);
        assertNotEquals(first, changed);
    }
}
