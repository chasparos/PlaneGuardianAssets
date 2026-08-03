package com.planeguardian.assets.tools.generator;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Hashtable;

/**
 * Stub generator for a parametric deciduous tree.
 *
 * <p>The parameter panel is fully wired; {@link #generate} is a placeholder
 * that logs the chosen parameters and returns a {@link GenerationResult#failure}
 * until the JME3 mesh-generation algorithm is implemented.</p>
 *
 * <p>Planned implementation will use JME3 to construct trunk, branch, and
 * canopy geometry, write a {@code .j3o} file to the output directory, and
 * return its path in a {@link GenerationResult#success} result.</p>
 */
@Slf4j
public class DeciduousTreeGenerator implements AssetGenerator {

    // ---- parameter state (read on the background thread via getter methods)

    private JSlider heightSlider;
    private JSlider canopyRadiusSlider;
    private JSlider trunkRadiusSlider;
    private JSlider branchDensitySlider;
    private JTextField seedField;
    private JTextField nameField;

    @Override
    public String getName() {
        return "Deciduous Tree";
    }

    @Override
    public JPanel buildParameterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Asset name
        nameField = new JTextField("DeciduousTree_01", 22);
        row = addRow(panel, c, row, "Asset Name:", nameField);

        // Height  1 – 20 m
        heightSlider = labelledSlider(1, 20, 8, 1);
        row = addRow(panel, c, row, "Height (m):", heightSlider);

        // Canopy radius  1 – 10 m
        canopyRadiusSlider = labelledSlider(1, 10, 4, 1);
        row = addRow(panel, c, row, "Canopy Radius (m):", canopyRadiusSlider);

        // Trunk radius  5 – 50 cm (stored as cm to keep ints)
        trunkRadiusSlider = labelledSlider(5, 50, 15, 5);
        row = addRow(panel, c, row, "Trunk Radius (cm):", trunkRadiusSlider);

        // Branch density  1 – 10
        branchDensitySlider = labelledSlider(1, 10, 5, 1);
        row = addRow(panel, c, row, "Branch Density:", branchDensitySlider);

        // Random seed
        seedField = new JTextField("42", 10);
        row = addRow(panel, c, row, "Random Seed:", seedField);

        // Stub notice
        JLabel notice = new JLabel(
                "<html><i>Generation not yet implemented – parameters are defined<br>" +
                "and will drive the JME3 tree-mesh algorithm in a future iteration.</i></html>");
        notice.setForeground(new Color(140, 90, 0));
        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        panel.add(notice, c);

        return panel;
    }

    @Override
    public GenerationResult generate(Path outputDirectory) {
        // Read parameter values (safe to access final fields set during buildParameterPanel)
        int height        = heightSlider.getValue();
        int canopyRadius  = canopyRadiusSlider.getValue();
        int trunkRadius   = trunkRadiusSlider.getValue();
        int branchDensity = branchDensitySlider.getValue();
        String seed       = seedField.getText().trim();
        String name       = nameField.getText().trim().isEmpty() ? "DeciduousTree" : nameField.getText().trim();

        log.info("DeciduousTreeGenerator.generate() called – height={}m canopyRadius={}m " +
                 "trunkRadius={}cm branchDensity={} seed={} outputDir={}",
                 height, canopyRadius, trunkRadius, branchDensity, seed, outputDirectory);

        // TODO: implement JME3 mesh generation:
        //   1. Build trunk Cylinder geometry
        //   2. Recursively add Branch nodes using L-system / random branching
        //   3. Attach Billboard leaf quads or Sphere canopy to branch tips
        //   4. Wrap everything in a Node, export to outputDirectory/<name>.j3o
        //   5. Return GenerationResult.success(outputPath, name, "Generated successfully")

        return GenerationResult.failure(
                "Deciduous Tree generation is not yet implemented.\n\n" +
                "Parameters captured:\n" +
                "  Height: " + height + " m\n" +
                "  Canopy Radius: " + canopyRadius + " m\n" +
                "  Trunk Radius: " + trunkRadius + " cm\n" +
                "  Branch Density: " + branchDensity + "\n" +
                "  Seed: " + seed);
    }

    // ---- helpers ----------------------------------------------------------

    private JSlider labelledSlider(int min, int max, int value, int majorTick) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(Math.max(majorTick, (max - min) / 5));
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(min, new JLabel(String.valueOf(min)));
        labels.put(max, new JLabel(String.valueOf(max)));
        labels.put(value, new JLabel(String.valueOf(value)));
        slider.setLabelTable(labels);
        return slider;
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label, Component field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
        return row + 1;
    }
}
