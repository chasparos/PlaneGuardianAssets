package com.planeguardian.assets.tools;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder Swing tool for procedural asset generation.
 *
 * <p>Shape buttons log a notice; actual JME3 geometry generation will be
 * implemented in a future iteration.</p>
 */
@Slf4j
public class ProceduralGenTool extends JFrame {

    public ProceduralGenTool() {
        super("Procedural Asset Generator");
        setSize(580, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Procedural Asset Generator", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(new Color(40, 80, 160));
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // Shape preset buttons
        JPanel shapesPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        shapesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 20, 8, 20),
                BorderFactory.createTitledBorder("Primitive Shapes")));

        String[] shapes = {"Box", "Sphere", "Cylinder", "Cone", "Torus", "Plane",
                           "Capsule", "Quad", "Arrow"};
        for (String shape : shapes) {
            JButton btn = new JButton(shape);
            btn.addActionListener(e -> onGenerateShape(shape));
            shapesPanel.add(btn);
        }
        add(shapesPanel, BorderLayout.CENTER);

        // Status bar
        JLabel status = new JLabel(
                "  Select a primitive to generate a JME3 geometry asset (coming soon).",
                SwingConstants.LEFT);
        status.setFont(new Font("SansSerif", Font.ITALIC, 12));
        status.setForeground(Color.GRAY);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        add(status, BorderLayout.SOUTH);
    }

    private void onGenerateShape(String shapeName) {
        log.info("Procedural generation requested: {}", shapeName);
        JOptionPane.showMessageDialog(this,
                "Procedural " + shapeName + " generation is not yet implemented.\n\n" +
                "This will create a JME3 Mesh, wrap it in a Geometry/Node,\n" +
                "export it as a j3o file, and register it in the asset library.",
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
}
