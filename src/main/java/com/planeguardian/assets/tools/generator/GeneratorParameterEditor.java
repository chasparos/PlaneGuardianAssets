package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.generation.api.GeneratorDescriptor;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Generic schema-driven editor shared by every registered authoring provider. */
final class GeneratorParameterEditor {
    private final GeneratorDescriptor descriptor;
    private final AuthoringGeneratorProvider provider;
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JTextField name = new JTextField(22);
    private final JTextField seed = new JTextField("42", 14);
    private final JComboBox<GeneratorDescriptor.Preset> presets;
    private final Map<String, JComponent> controls = new LinkedHashMap<>();
    private final Map<String, Component[]> advancedRows = new LinkedHashMap<>();
    private final Map<String, JCheckBox> overrides = new LinkedHashMap<>();
    private final JTextArea resolved = new JTextArea(12, 36);
    private final java.util.List<Runnable> changeListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final com.planeguardian.assets.tools.semantics.SemanticProfileEditor semantics =
            new com.planeguardian.assets.tools.semantics.SemanticProfileEditor(
                    com.planeguardian.assets.generation.semantics.StandardSemanticWheels.all());

    GeneratorParameterEditor(AuthoringGeneratorProvider provider) {
        this.provider = java.util.Objects.requireNonNull(provider);
        this.descriptor = provider.descriptor();
        this.name.setText(descriptor.displayName().replaceAll("[^a-zA-Z0-9]+", "") + "_01");
        this.presets = new JComboBox<>(descriptor.presets().toArray(GeneratorDescriptor.Preset[]::new));
        build();
        resolved.setEditable(false); resolved.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        semantics.addChangeListener(() -> { refreshResolved(); fireChanged(); });
        seed.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { fireChanged(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { fireChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { fireChanged(); }
        });
        if (!descriptor.presets().isEmpty()) applyPreset(descriptor.presets().get(0));
        refreshResolved();
    }

    JComponent panel() {
        JTabbedPane tabs = new JTabbedPane(); tabs.addTab("Parameters", panel);
        JSplitPane semanticTab = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(semantics), new JScrollPane(resolved));
        semanticTab.setResizeWeight(.72); resolved.setBorder(BorderFactory.createTitledBorder("Resolved visual channels and contribution trace (read-only)"));
        tabs.addTab("Semantic profile", semanticTab); return tabs;
    }

    AuthoringGenerationRequest snapshot() {
        Map<String, String> values = new java.util.TreeMap<>();
        descriptor.parameters().forEach(parameter -> {
            JComponent control = controls.get(parameter.id().value());
            values.put(parameter.id().value(), value(control, parameter));
        });
        java.util.Set<String> explicit = overrides.entrySet().stream().filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey).collect(java.util.stream.Collectors.toSet());
        return new AuthoringGenerationRequest(name.getText().trim(), Long.parseLong(seed.getText().trim()),
                new java.util.TreeMap<>(values), semantics.profile(), explicit);
    }
    void addChangeListener(Runnable listener) { changeListeners.add(java.util.Objects.requireNonNull(listener)); }

    private void build() {
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = constraints();
        int row = add(row(c, 0), "Asset Name", name, false);
        row = add(row(c, row), "Seed", seed, false);
        JButton randomSeed = new JButton("Randomize seed");
        randomSeed.addActionListener(event -> seed.setText(Long.toString(ThreadLocalRandom.current().nextLong())));
        row = add(row(c, row), "", randomSeed, false);
        if (presets.getItemCount() > 0) {
            presets.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    if (value instanceof GeneratorDescriptor.Preset preset) setText(preset.displayName());
                    return this;
                }
            });
            presets.addActionListener(event -> applyPreset((GeneratorDescriptor.Preset) presets.getSelectedItem()));
            row = add(row(c, row), "Preset", presets, false);
        }
        JButton reset = new JButton("Reset defaults");
        reset.addActionListener(event -> resetDefaults());
        row = add(row(c, row), "", reset, false);
        JCheckBox advanced = new JCheckBox("Show advanced parameters");
        advanced.addActionListener(event -> setAdvancedVisible(advanced.isSelected()));
        row = add(row(c, row), "", advanced, false);
        for (GeneratorDescriptor.Parameter parameter : descriptor.parameters()) {
            JComponent control = control(parameter); control.setName(parameter.id().value());
            if (control instanceof JSpinner spinner) {
                spinner.addChangeListener(event -> fireChanged());
            } else if (control instanceof JComboBox<?> combo) {
                combo.addActionListener(event -> fireChanged());
            } else if (control instanceof JTextField text) {
                text.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { fireChanged(); }
                    @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { fireChanged(); }
                    @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { fireChanged(); }
                });
            }
            controls.put(parameter.id().value(), control);
            Component field = control;
            if (descriptor.semanticDerivedParameters().contains(parameter.id())) {
                JCheckBox override = new JCheckBox("Override resolved value"); overrides.put(parameter.id().value(), override);
                override.addActionListener(event -> fireChanged());
                JPanel group = new JPanel(new BorderLayout(6, 0)); group.setName(parameter.id().value());
                group.add(control, BorderLayout.CENTER); group.add(override, BorderLayout.EAST); field = group;
            }
            row = add(row(c, row), parameter.displayName(), field, parameter.advanced());
        }
        c.gridx = 2; c.gridy = 0; c.gridheight = Math.max(1, row); c.weightx = 1;
        c.weighty = 0; c.fill = GridBagConstraints.HORIZONTAL; panel.add(Box.createHorizontalGlue(), c);
        c.gridx = 0; c.gridy = row; c.gridwidth = 3; c.gridheight = 1; c.weightx = 0;
        c.weighty = 1; c.fill = GridBagConstraints.VERTICAL; panel.add(Box.createVerticalGlue(), c);
        setAdvancedVisible(false);
    }

    private int add(GridBagConstraints c, String text, Component field, boolean advanced) {
        JLabel label = new JLabel(text); c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE; panel.add(label, c);
        c.gridx = 1; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL; panel.add(field, c);
        if (advanced) advancedRows.put(field.getName(), new Component[]{label, field});
        return c.gridy + 1;
    }

    private void resetDefaults() {
        descriptor.parameters().forEach(parameter -> setControlValue(controls.get(parameter.id().value()),
                parameter.defaultValue(), parameter));
    }
    private void applyPreset(GeneratorDescriptor.Preset preset) {
        if (preset == null) return;
        resetDefaults();
        preset.parameterValues().forEach((id, value) -> {
            JComponent control = controls.get(id);
            if (control != null) {
                GeneratorDescriptor.Parameter parameter = descriptor.parameters().stream()
                        .filter(p -> p.id().value().equals(id)).findFirst().orElseThrow();
                setControlValue(control, value, parameter);
            }
        });
    }
    private void setAdvancedVisible(boolean visible) {
        advancedRows.values().forEach(row -> { row[0].setVisible(visible); row[1].setVisible(visible); });
        panel.revalidate(); panel.repaint();
    }
    private void refreshResolved() {
        provider.semanticAdapter().ifPresentOrElse(adapter -> {
            var profile = adapter.resolve(semantics.profile(),
                    com.planeguardian.assets.generation.semantics.AssetSemanticAdapter.ResolutionContext.intrinsicOnly());
            StringBuilder text = new StringBuilder(); profile.channels().forEach((id, value) ->
                    text.append(id).append(" = ").append("%.4f".formatted(value)).append('\n'));
            text.append("\nContributions:\n"); profile.contributionTrace().forEach(contribution -> text.append("• ")
                    .append(contribution.targetParameter()).append(" ← ").append(contribution.source())
                    .append(" (").append("%.4f".formatted(contribution.amount())).append(")\n"));
            resolved.setText(text.toString());
        }, () -> resolved.setText("This provider declares no semantic adapter."));
    }
    private void fireChanged() { changeListeners.forEach(Runnable::run); }
    private static JComponent control(GeneratorDescriptor.Parameter parameter) {
        if ("enum".equals(parameter.valueType())) {
            JComboBox<String> combo = new JComboBox<>(parameter.allowedValues().split("\\|", -1));
            combo.setSelectedItem(parameter.defaultValue());
            return combo;
        }
        if ("string".equals(parameter.valueType())) {
            return new JTextField(parameter.defaultValue(), 22);
        }
        JSpinner spinner = spinner(parameter);
        return spinner;
    }
    private static JSpinner spinner(GeneratorDescriptor.Parameter parameter) {
        double min = bound(parameter.allowedValues(), true); double max = bound(parameter.allowedValues(), false);
        Number value = number(parameter.defaultValue(), parameter.valueType());
        JSpinner spinner = "integer".equals(parameter.valueType())
                ? new JSpinner(new SpinnerNumberModel(value.intValue(), (int) min, (int) max, 1))
                : new JSpinner(new SpinnerNumberModel(value.doubleValue(), min, max, .01));
        if (!"integer".equals(parameter.valueType())) {
            spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00##"));
        }
        Dimension preferred = spinner.getPreferredSize();
        spinner.setPreferredSize(new Dimension(180, preferred.height));
        spinner.setMinimumSize(new Dimension(120, preferred.height));
        spinner.setMaximumSize(new Dimension(220, preferred.height));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setColumns(12);
            editor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
        return spinner;
    }
    private static String value(JComponent control, GeneratorDescriptor.Parameter parameter) {
        if (control instanceof JSpinner spinner) {
            try {
                spinner.commitEdit();
            } catch (java.text.ParseException exception) {
                throw new IllegalArgumentException("Invalid value for " + parameter.displayName() + ": "
                        + ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().getText(), exception);
            }
            return spinner.getValue().toString();
        }
        if (control instanceof JComboBox<?> combo) return String.valueOf(combo.getSelectedItem());
        if (control instanceof JTextField text) return text.getText();
        throw new IllegalArgumentException("Unsupported control for " + parameter.displayName());
    }
    private static Object controlValue(String value, GeneratorDescriptor.Parameter parameter) {
        return switch (parameter.valueType()) {
            case "integer" -> Integer.parseInt(value);
            case "enum", "string" -> value;
            default -> Double.parseDouble(value);
        };
    }
    private static void setControlValue(JComponent control, String value, GeneratorDescriptor.Parameter parameter) {
        if (control instanceof JComboBox<?> combo) {
            combo.setSelectedItem(value);
        } else if (control instanceof JTextField text) {
            text.setText(value);
        } else {
            ((JSpinner) control).setValue(controlValue(value, parameter));
        }
    }
    private static Number number(String value, String type) { return "integer".equals(type) ? Integer.parseInt(value) : Double.parseDouble(value); }
    private static double bound(String range, boolean lower) {
        if (range.startsWith("(0") && lower) return .01;
        String token = range.substring(1, range.length() - 1).split(",")[lower ? 0 : 1].trim();
        if ("attachment-start".equals(token)) return 0;
        if ("infinity".equals(token)) return Double.MAX_VALUE;
        if ("-2pi".equals(token)) return -2 * StrictMath.PI;
        if ("2pi".equals(token)) return 2 * StrictMath.PI;
        return Double.parseDouble(token);
    }
    private static GridBagConstraints constraints() { GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4, 6, 4, 6); c.anchor = GridBagConstraints.WEST; return c; }
    private static GridBagConstraints row(GridBagConstraints c, int row) { c.gridy = row; return c; }
}
