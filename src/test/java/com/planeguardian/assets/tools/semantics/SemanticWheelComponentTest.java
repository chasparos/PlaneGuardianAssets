package com.planeguardian.assets.tools.semantics;

import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.StandardSemanticWheels;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticWheelComponentTest {
    @Test
    void dragIsConstrainedAndSalienceRemainsIndependent() {
        SemanticWheelComponent wheel = new SemanticWheelComponent(StandardSemanticWheels.all().get(0));
        wheel.setSize(250, 250); wheel.setSalience(.75); wheel.movePoint(1000, 125);
        assertEquals(1, wheel.value().extremity(), 1e-9);
        assertEquals(.75, wheel.value().salience(), 1e-9);
        wheel.paint(new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB).getGraphics());
    }

    @Test
    void reusableProfileEditorPublishesAllStandardWheels() {
        SemanticProfileEditor editor = new SemanticProfileEditor(StandardSemanticWheels.all());
        assertEquals(6, editor.profile().wheels().size());
        editor.wheel(new StableId("lore.ethos")).setSalience(1);
        assertEquals(1, editor.profile().wheels().get(new StableId("lore.ethos")).salience());
        assertEquals(6, countWheels(editor));
    }

    private static int countWheels(java.awt.Container container) {
        int count = 0;
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof SemanticWheelComponent) count++;
            if (component instanceof java.awt.Container child) count += countWheels(child);
        }
        return count;
    }
}
