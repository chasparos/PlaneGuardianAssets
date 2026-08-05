package com.planeguardian.assets.tools.semantics;

import com.planeguardian.assets.generation.api.ContractVersion;
import com.planeguardian.assets.generation.api.StableId;
import com.planeguardian.assets.generation.semantics.SemanticProfile;
import com.planeguardian.assets.generation.semantics.SemanticWheelDefinition;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Reusable source-profile editor; asset-family derived values remain outside it. */
public final class SemanticProfileEditor extends JPanel {
    private final Map<StableId, SemanticWheelComponent> wheels = new LinkedHashMap<>();
    private final java.util.List<Runnable> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public SemanticProfileEditor(List<SemanticWheelDefinition> definitions) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (int index = 0; index < definitions.size(); index += 2) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(wheelPanel(definitions.get(index)));
            if (index + 1 < definitions.size()) row.add(wheelPanel(definitions.get(index + 1)));
            add(row);
        }
    }

    public SemanticProfile profile() {
        TreeMap<StableId, com.planeguardian.assets.generation.semantics.SemanticWheelValue> values = new TreeMap<>();
        wheels.forEach((id, wheel) -> values.put(id, wheel.value()));
        return new SemanticProfile(new ContractVersion(1, 0), values);
    }

    public SemanticWheelComponent wheel(StableId id) {
        SemanticWheelComponent result = wheels.get(id);
        if (result == null) throw new IllegalArgumentException("Unknown wheel: " + id);
        return result;
    }
    public void addChangeListener(Runnable listener) { listeners.add(java.util.Objects.requireNonNull(listener)); }

    private JPanel wheelPanel(SemanticWheelDefinition definition) {
        SemanticWheelComponent wheel = new SemanticWheelComponent(definition); wheels.put(definition.id(), wheel);
        wheel.addValueListener(value -> listeners.forEach(Runnable::run));
        JSlider salience = new JSlider(0, 1000, 0); salience.setName(definition.id().value() + ".salience");
        salience.addChangeListener(event -> wheel.setSalience(salience.getValue() / 1000.0));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(320, 315));
        panel.setBorder(BorderFactory.createTitledBorder(definition.displayName())); panel.add(wheel, BorderLayout.CENTER);
        JPanel importance = new JPanel(new BorderLayout(6, 0)); importance.add(new JLabel(definition.salienceLabel()), BorderLayout.WEST);
        importance.add(salience, BorderLayout.CENTER); panel.add(importance, BorderLayout.SOUTH); return panel;
    }
}
