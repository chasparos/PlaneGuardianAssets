package com.planeguardian.assets.tools;

import com.planeguardian.assets.generation.api.Contribution;
import com.planeguardian.assets.generation.api.RenderTier;
import com.planeguardian.assets.generation.tree.ResolvedTreeFeatures;
import com.planeguardian.assets.generation.tree.TreeSemanticProfile;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/** Desktop visual-validation editor for source semantic wheels and derived tree diagnostics. */
public final class SemanticWheelDesktopEditor extends JFrame {
    private final SemanticWheelEditorModel model = new SemanticWheelEditorModel();
    private final JSlider vitality = signedSlider();
    private final JSlider regularity = signedSlider();
    private final JSlider transformation = signedSlider();
    private final JSlider genesis = signedSlider();
    private final JSlider water = unitSlider();
    private final JSlider fire = unitSlider();
    private final JTextField seed = new JTextField("42", 14);
    private final JComboBox<RenderTier> tier = new JComboBox<>(new DefaultComboBoxModel<>(RenderTier.values()));
    private final JLabel status = new JLabel("  Adjust a source wheel to regenerate.");
    private final JTextArea resolved = new JTextArea();
    private final DefaultTableModel contributions = new DefaultTableModel(
            new String[]{"Target", "Source", "Amount", "Explanation"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private long generation;

    public SemanticWheelDesktopEditor() {
        super("Great Tree Semantic Wheel");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1080, 650);
        setLocationRelativeTo(null);
        buildUi();
        regenerate();
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        JLabel header = new JLabel("Great Tree Semantic Wheel — derived values are read-only", SwingConstants.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        add(header, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourcePanel(), inspectionPanel());
        split.setResizeWeight(.38);
        split.setDividerLocation(410);
        add(split, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        add(status, BorderLayout.SOUTH);
    }

    private JPanel sourcePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Editable source inputs"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 8, 5, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        int row = 0;
        row = addWheel(panel, constraints, row, "Vitality", vitality, "-1 death  ·  +1 life");
        row = addWheel(panel, constraints, row, "Regularity", regularity, "-1 chaos  ·  +1 order");
        row = addWheel(panel, constraints, row, "Transformation", transformation, "-1 preservation  ·  +1 change");
        row = addWheel(panel, constraints, row, "Genesis", genesis, "-1 annihilation  ·  +1 creation");
        row = addWheel(panel, constraints, row, "Water", water, "0 dry  ·  1 saturated");
        row = addWheel(panel, constraints, row, "Fire", fire, "0 cool  ·  1 burning");
        constraints.gridy = row++;
        constraints.gridx = 0;
        panel.add(new JLabel("Seed"), constraints);
        constraints.gridx = 1;
        panel.add(seed, constraints);
        constraints.gridy = row++;
        constraints.gridx = 0;
        panel.add(new JLabel("Render tier"), constraints);
        constraints.gridx = 1;
        panel.add(tier, constraints);
        JButton regenerate = new JButton("Regenerate now");
        regenerate.addActionListener(event -> regenerate());
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        panel.add(regenerate, constraints);
        return panel;
    }

    private int addWheel(JPanel panel, GridBagConstraints constraints, int row, String name, JSlider slider, String hint) {
        constraints.gridy = row;
        constraints.gridx = 0;
        panel.add(new JLabel(name), constraints);
        constraints.gridx = 1;
        panel.add(slider, constraints);
        constraints.gridy = ++row;
        constraints.gridx = 1;
        JLabel label = new JLabel(hint);
        label.setForeground(Color.DARK_GRAY);
        panel.add(label, constraints);
        slider.addChangeListener(event -> regenerate());
        return row + 1;
    }

    private JPanel inspectionPanel() {
        resolved.setEditable(false);
        resolved.setLineWrap(true);
        resolved.setWrapStyleWord(true);
        JTable table = new JTable(contributions);
        JPanel panel = new JPanel(new GridLayout(2, 1, 4, 4));
        JScrollPane profile = new JScrollPane(resolved);
        profile.setBorder(BorderFactory.createTitledBorder("Resolved profile and regenerated products"));
        JScrollPane trace = new JScrollPane(table);
        trace.setBorder(BorderFactory.createTitledBorder("Contribution trace (read-only)"));
        panel.add(profile);
        panel.add(trace);
        seed.getDocument().addDocumentListener(new RegenerationDocumentListener());
        tier.addActionListener(event -> regenerate());
        return panel;
    }

    private void regenerate() {
        long request = ++generation;
        TreeSemanticProfile profile = new TreeSemanticProfile(signed(vitality), signed(regularity),
                signed(transformation), signed(genesis), unit(water), unit(fire));
        long parsedSeed;
        try {
            parsedSeed = Long.parseLong(seed.getText().trim());
        } catch (NumberFormatException exception) {
            status.setText("  Seed must be a signed 64-bit integer.");
            return;
        }
        RenderTier selectedTier = (RenderTier) tier.getSelectedItem();
        status.setText("  Regenerating " + selectedTier + " profile…");
        new SwingWorker<SemanticWheelEditorModel.Regeneration, Void>() {
            @Override protected SemanticWheelEditorModel.Regeneration doInBackground() {
                return model.regenerate(profile, parsedSeed, selectedTier);
            }

            @Override protected void done() {
                if (request != generation || !isDisplayable()) return;
                try {
                    show(get());
                } catch (Exception exception) {
                    status.setText("  Regeneration failed: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private void show(SemanticWheelEditorModel.Regeneration result) {
        ResolvedTreeFeatures features = result.resolved();
        resolved.setText("""
                Crown coverage: %.3f
                Moss coverage: %.3f
                Vine coverage: %.3f
                Flower density: %.3f
                Fruit density: %.3f
                Fungal coverage: %.3f

                Seed: %d    Tier: %s
                Structural parts: %d    Crown parts: %d
                Structural fingerprint: %s
                Crown fingerprint: %s
                """.formatted(features.crown().coverage(), features.mossCoverage(), features.vineCoverage(),
                features.flowerDensity(), features.fruitDensity(), features.fungalCoverage(), result.seed(), result.tier(),
                result.structuralPartCount(), result.crownPartCount(), result.structuralFingerprint(), result.crownFingerprint()));
        contributions.setRowCount(0);
        for (Contribution contribution : features.contributions()) {
            contributions.addRow(new Object[]{contribution.targetParameter(), contribution.source(),
                    "%.3f".formatted(contribution.amount()), contribution.explanation()});
        }
        status.setText("  Regenerated " + result.tier() + " tree for seed " + result.seed() + ".");
    }

    private static JSlider signedSlider() {
        JSlider slider = new JSlider(-100, 100, 0);
        slider.setMajorTickSpacing(100);
        slider.setPaintTicks(true);
        return slider;
    }

    private static JSlider unitSlider() {
        JSlider slider = new JSlider(0, 100, 0);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        return slider;
    }

    private static double signed(JSlider slider) { return slider.getValue() / 100.0; }
    private static double unit(JSlider slider) { return slider.getValue() / 100.0; }

    private final class RegenerationDocumentListener implements DocumentListener {
        @Override public void insertUpdate(DocumentEvent event) { regenerate(); }
        @Override public void removeUpdate(DocumentEvent event) { regenerate(); }
        @Override public void changedUpdate(DocumentEvent event) { regenerate(); }
    }
}
