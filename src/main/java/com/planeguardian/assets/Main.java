package com.planeguardian.assets;

import com.planeguardian.assets.db.DatabaseManager;
import com.planeguardian.assets.export.ExportManager;
import com.planeguardian.assets.tools.AssetBrowserTool;
import com.planeguardian.assets.tools.generator.AssetGeneratorTool;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point – a Swing tool-launcher JFrame.
 *
 * <p>Each button opens an independent tool window.  Tools that need a JME3
 * context (like the Asset Browser's 3-D viewer) spawn their own LWJGL3
 * window on a daemon thread; all other tools are plain Swing JFrames.</p>
 */
@Slf4j
public class Main extends JFrame {

    public Main() {
        super("PlaneGuardian Asset Tools");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 320);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("PlaneGuardian  ·  Asset Library", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(new Color(35, 75, 170));
        header.setBorder(BorderFactory.createEmptyBorder(22, 10, 8, 10));
        add(header, BorderLayout.NORTH);

        // Tool buttons
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 55, 10, 55));

        JButton assetBrowserBtn   = toolButton("Asset Browser",       "Browse, import and manage library assets");
        JButton assetGeneratorBtn = toolButton("Asset Generator",      "Parametric/procedural asset generation with 3-D preview");
        JButton exportLibraryBtn  = toolButton("Export Library",       "Export flagged assets + write asset_index.json");

        assetBrowserBtn.addActionListener(e   -> new AssetBrowserTool().setVisible(true));
        assetGeneratorBtn.addActionListener(e -> new AssetGeneratorTool().setVisible(true));
        exportLibraryBtn.addActionListener(e  -> ExportManager.exportLibrary(this));

        buttons.add(assetBrowserBtn);
        buttons.add(assetGeneratorBtn);
        buttons.add(exportLibraryBtn);
        add(buttons, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("v1.0.0-SNAPSHOT  ·  JME3 3.7.0-stable / LWJGL3", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);
        footer.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
        add(footer, BorderLayout.SOUTH);
    }

    private static JButton toolButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(0, 46));
        return btn;
    }

    // ---- Entry point ------------------------------------------------------

    public static void main(String[] args) {
        log.info("Starting PlaneGuardian Asset Tools…");

        // Use the OS native look & feel when available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.warn("Could not apply system look and feel: {}", e.getMessage());
        }

        DatabaseManager.initialize();
        Runtime.getRuntime().addShutdownHook(
                new Thread(DatabaseManager::shutdown, "DB-Shutdown"));

        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
