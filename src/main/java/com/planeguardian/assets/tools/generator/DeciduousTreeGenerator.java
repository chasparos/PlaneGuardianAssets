package com.planeguardian.assets.tools.generator;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Random;

/**
 * Generates a deterministic, medium-detail deciduous tree as a JME3 {@code .j3o} model.
 */
@Slf4j
public class DeciduousTreeGenerator implements AssetGenerator {

    private static final String LIGHTING_SHADER = "Common/MatDefs/Light/Lighting.j3md";
    private volatile TreeParameters parameters = new TreeParameters(8, 4, 15, 5, "42", "DeciduousTree_01");

    @Override
    public String generatorId() {
        return "legacy.tree.deciduous/0";
    }

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

        JTextField nameField = new JTextField(parameters.name(), 22);
        row = addRow(panel, c, row, "Asset Name:", nameField);
        JSlider heightSlider = labelledSlider(1, 20, parameters.height(), 1);
        row = addRow(panel, c, row, "Height (m):", heightSlider);
        JSlider canopyRadiusSlider = labelledSlider(1, 10, parameters.canopyRadius(), 1);
        row = addRow(panel, c, row, "Canopy Radius (m):", canopyRadiusSlider);
        JSlider trunkRadiusSlider = labelledSlider(5, 50, parameters.trunkRadiusCm(), 5);
        row = addRow(panel, c, row, "Trunk Radius (cm):", trunkRadiusSlider);
        JSlider branchDensitySlider = labelledSlider(1, 10, parameters.branchDensity(), 1);
        row = addRow(panel, c, row, "Branch Density:", branchDensitySlider);
        JTextField seedField = new JTextField(parameters.seed(), 10);
        row = addRow(panel, c, row, "Random Seed:", seedField);

        Runnable updateParameters = () -> parameters = new TreeParameters(
                heightSlider.getValue(), canopyRadiusSlider.getValue(), trunkRadiusSlider.getValue(),
                branchDensitySlider.getValue(), seedField.getText().trim(), nameField.getText().trim());
        heightSlider.addChangeListener(e -> updateParameters.run());
        canopyRadiusSlider.addChangeListener(e -> updateParameters.run());
        trunkRadiusSlider.addChangeListener(e -> updateParameters.run());
        branchDensitySlider.addChangeListener(e -> updateParameters.run());
        nameField.getDocument().addDocumentListener(new DocumentChangeListener(updateParameters));
        seedField.getDocument().addDocumentListener(new DocumentChangeListener(updateParameters));

        JLabel notice = new JLabel("<html><i>Generates a deterministic medium-detail tree with named bark and foliage materials.</i></html>");
        notice.setForeground(new Color(0, 100, 50));
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        panel.add(notice, c);
        return panel;
    }

    @Override
    public GenerationResult generate(Path outputDirectory) {
        TreeParameters snapshot = parameters;
        String name = snapshot.name().isBlank() ? "DeciduousTree" : snapshot.name();
        try {
            Files.createDirectories(outputDirectory);
            Node tree = createTree(snapshot);
            Path output = outputDirectory.resolve(sanitiseFileName(name) + ".j3o");
            BinaryExporter.getInstance().save(tree, output.toFile());
            return GenerationResult.success(output, name,
                    "Generated deterministic deciduous tree (" + snapshot.branchDensity() + " branch density).");
        } catch (IOException | RuntimeException e) {
            log.error("Failed to generate deciduous tree", e);
            return GenerationResult.failure("Failed to generate deciduous tree: " + e.getMessage());
        }
    }

    private Node createTree(TreeParameters p) {
        Random random = new Random(seedValue(p.seed()));
        float trunkHeight = p.height() * 0.58f;
        float trunkRadius = p.trunkRadiusCm() / 100f;
        Material bark = createMaterial("Tree_Bark", new ColorRGBA(0.25f, 0.11f, 0.035f, 1f), 6f);
        Material foliage = createMaterial("Tree_Foliage", new ColorRGBA(0.12f, 0.38f, 0.06f, 1f), 2f);

        Node tree = new Node("DeciduousTree");
        tree.attachChild(cylinder("Trunk", trunkRadius * 0.72f, trunkHeight, Vector3f.ZERO, Vector3f.UNIT_Y, bark));

        int branchCount = 8 + p.branchDensity() * 5;
        for (int i = 0; i < branchCount; i++) {
            float level = 0.28f + random.nextFloat() * 0.64f;
            float elevation = 0.24f + random.nextFloat() * 0.36f;
            float angle = FastMath.TWO_PI * i / branchCount + random.nextFloat() * 0.55f;
            float length = p.canopyRadius() * (0.55f + random.nextFloat() * 0.35f);
            Vector3f direction = new Vector3f(FastMath.cos(angle), elevation, FastMath.sin(angle)).normalizeLocal();
            Vector3f base = new Vector3f(0, trunkHeight * level, 0);
            tree.attachChild(cylinder("Branch_" + i, trunkRadius * (0.32f + (1f - level) * 0.22f),
                    length, base, direction, bark));

            Vector3f tip = base.add(direction.mult(length));
            attachFoliage(tree, foliage, tip, p.canopyRadius(), random, i);
        }
        return tree;
    }

    private void attachFoliage(Node tree, Material foliage, Vector3f tip, float canopyRadius, Random random, int branch) {
        float clusterRadius = canopyRadius * (0.18f + random.nextFloat() * 0.07f);
        for (int i = 0; i < 2; i++) {
            Vector3f offset = new Vector3f(
                    (random.nextFloat() - 0.5f) * clusterRadius,
                    (random.nextFloat() - 0.35f) * clusterRadius,
                    (random.nextFloat() - 0.5f) * clusterRadius);
            Geometry leaves = new Geometry("Foliage_" + branch + "_" + i, new Sphere(8, 12, clusterRadius));
            leaves.setMaterial(foliage);
            leaves.setLocalTranslation(tip.add(offset));
            leaves.setLocalScale(1.15f, 0.85f, 1.15f);
            tree.attachChild(leaves);
        }
    }

    private Geometry cylinder(String name, float radius, float length, Vector3f base, Vector3f direction, Material material) {
        Geometry geometry = new Geometry(name, new Cylinder(8, 12, radius, length, true));
        geometry.setMaterial(material);
        geometry.setLocalTranslation(base.add(direction.mult(length * 0.5f)));
        geometry.setLocalRotation(rotationFromYAxis(direction));
        return geometry;
    }

    private Quaternion rotationFromYAxis(Vector3f direction) {
        Vector3f axis = Vector3f.UNIT_Y.cross(direction);
        if (axis.lengthSquared() < FastMath.ZERO_TOLERANCE) {
            return new Quaternion();
        }
        return new Quaternion().fromAngleAxis(Vector3f.UNIT_Y.angleBetween(direction), axis.normalizeLocal());
    }

    private Material createMaterial(String name, ColorRGBA color, float shininess) {
        Material material = new Material(new DesktopAssetManager(true), LIGHTING_SHADER);
        material.setName(name);
        material.setColor("Diffuse", color);
        material.setColor("Ambient", color.mult(0.55f));
        material.setColor("Specular", ColorRGBA.White.mult(0.08f));
        material.setFloat("Shininess", shininess);
        material.setBoolean("UseMaterialColors", true);
        return material;
    }

    private long seedValue(String seed) {
        try {
            return Long.parseLong(seed);
        } catch (NumberFormatException ignored) {
            return seed.hashCode();
        }
    }

    private String sanitiseFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

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

    private record TreeParameters(int height, int canopyRadius, int trunkRadiusCm, int branchDensity,
                                  String seed, String name) { }

    private static final class DocumentChangeListener implements javax.swing.event.DocumentListener {
        private final Runnable action;

        private DocumentChangeListener(Runnable action) {
            this.action = action;
        }

        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }
}
