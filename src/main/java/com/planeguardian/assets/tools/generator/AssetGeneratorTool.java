package com.planeguardian.assets.tools.generator;

import com.planeguardian.assets.db.AssetRepository;
import com.planeguardian.assets.db.AssetVersionRepository;
import com.google.gson.JsonObject;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.model.AssetType;
import com.planeguardian.assets.model.AssetVersion;
import com.planeguardian.assets.model.VersionSource;
import com.planeguardian.assets.tools.AssetViewerApp;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Swing tool for procedural / parametric asset generation.
 *
 * <p>Layout overview:</p>
 * <pre>
 * ┌─────────────────────────────────────────────────────────┐
 * │  [Header: "Asset Generator"]                            │
 * ├──────────────┬──────────────────────────────────────────┤
 * │  Generators  │  Parameters (scrollable, per-generator)  │
 * │  (JList)     │                                          │
 * │              │                                          │
 * ├──────────────┴──────────────────────────────────────────┤
 * │  [Generate]  [Preview in 3D]  [Save to Library]  status │
 * └─────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>After a successful generation the result file can be previewed in the
 * shared {@link AssetViewerApp} LWJGL3 window and then saved as a new
 * {@link Asset} (version 1) in the library database.</p>
 */
@Slf4j
public class AssetGeneratorTool extends JFrame {

    /** All registered generator implementations. */
    private static final List<AuthoringGeneratorProvider> GENERATORS =
            new AuthoringGeneratorRegistry().providers();

    static List<AuthoringGeneratorProvider> registeredGenerators() {
        return GENERATORS;
    }

    private final AssetRepository assetRepo = new AssetRepository();
    private final AssetVersionRepository versionRepo = new AssetVersionRepository();

    // ---- state
    private AuthoringGeneratorProvider activeGenerator;
    private final java.util.Map<AuthoringGeneratorProvider, GeneratorParameterEditor> editors = new java.util.LinkedHashMap<>();
    private GenerationResult lastResult;

    // ---- widgets
    private JList<AuthoringGeneratorProvider> generatorList;
    private JPanel paramCardPanel;   // CardLayout, one card per generator
    private CardLayout cardLayout;

    private JButton generateBtn;
    private JButton previewBtn;
    private JButton saveBtn;
    private JLabel statusLabel;
    private JCheckBox liveRegenerate;
    private JCheckBox autoPreview;
    private final Timer liveTimer = new Timer(550, event -> onGenerate());

    public AssetGeneratorTool() {
        super("Asset Generator");
        setSize(960, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        liveTimer.setRepeats(false);

        // Select the first generator by default
        if (!GENERATORS.isEmpty()) {
            generatorList.setSelectedIndex(0);
        }
    }

    // ---- UI construction --------------------------------------------------

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainSplit(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
    }

    private JLabel buildHeader() {
        JLabel header = new JLabel("Asset Generator", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(new Color(35, 75, 170));
        header.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        return header;
    }

    private JSplitPane buildMainSplit() {
        // ---- Left: generator list ----
        DefaultListModel<AuthoringGeneratorProvider> listModel = new DefaultListModel<>();
        GENERATORS.forEach(listModel::addElement);
        generatorList = new JList<>(listModel);
        generatorList.setCellRenderer(new GeneratorCellRenderer());
        generatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        generatorList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onGeneratorSelected(generatorList.getSelectedValue());
            }
        });
        JScrollPane listScroll = new JScrollPane(generatorList);
        listScroll.setPreferredSize(new Dimension(200, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder("Generators"));

        // ---- Right: per-generator parameter panels ----
        cardLayout = new CardLayout();
        paramCardPanel = new JPanel(cardLayout);
        for (AuthoringGeneratorProvider gen : GENERATORS) {
            GeneratorParameterEditor editor = new GeneratorParameterEditor(gen);
            editor.addChangeListener(() -> scheduleLiveGeneration(gen));
            editors.put(gen, editor);
            JScrollPane scroll = new JScrollPane(editor.panel());
            scroll.setBorder(BorderFactory.createTitledBorder("Parameters – " + gen.descriptor().displayName()));
            paramCardPanel.add(scroll, gen.descriptor().generatorId().value());
        }

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, paramCardPanel);
        split.setDividerLocation(210);
        split.setResizeWeight(0.0);
        return split;
    }

    private JPanel buildActionBar() {
        generateBtn = new JButton("⚙  Generate");
        previewBtn  = new JButton("👁  Preview in 3D");
        saveBtn     = new JButton("💾  Save to Library");
        liveRegenerate = new JCheckBox("Live regenerate", false);
        autoPreview = new JCheckBox("Auto-preview", false);
        statusLabel = new JLabel("  Select a generator and click Generate.");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(Color.DARK_GRAY);

        previewBtn.setEnabled(false);
        saveBtn.setEnabled(false);

        generateBtn.addActionListener(e -> onGenerate());
        previewBtn.addActionListener(e  -> onPreview());
        saveBtn.addActionListener(e     -> onSaveToLibrary());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        bar.add(generateBtn);
        bar.add(previewBtn);
        bar.add(saveBtn);
        bar.add(liveRegenerate);
        bar.add(autoPreview);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(statusLabel);
        return bar;
    }

    // ---- actions ----------------------------------------------------------

    private void onGeneratorSelected(AuthoringGeneratorProvider gen) {
        if (gen == null) return;
        activeGenerator = gen;
        cardLayout.show(paramCardPanel, gen.descriptor().generatorId().value());
        lastResult = null;
        previewBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        setStatus("Selected: " + gen.descriptor().displayName(), Color.DARK_GRAY);
    }

    private void onGenerate() {
        if (activeGenerator == null) return;

        generateBtn.setEnabled(false);
        previewBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        setStatus("Generating…", Color.DARK_GRAY);

        AuthoringGeneratorProvider gen = activeGenerator;
        AuthoringGenerationRequest request;
        try {
            request = editors.get(gen).snapshot();
            log.info("Generating {} seed={} parameters={}", gen.descriptor().generatorId(),
                    request.visualSeed(), request.directParameters());
        } catch (RuntimeException exception) {
            generateBtn.setEnabled(true);
            setStatus("Invalid parameters: " + exception.getMessage(), new Color(180, 0, 0));
            return;
        }
        Thread worker = new Thread(() -> {
            try {
                Path outDir = Files.createTempDirectory("pg_gen_");
                GenerationResult result = gen.generate(request, outDir);
                SwingUtilities.invokeLater(() -> onGenerationComplete(result));
            } catch (Exception ex) {
                log.error("Generation failed unexpectedly", ex);
                SwingUtilities.invokeLater(() ->
                        onGenerationComplete(GenerationResult.failure("Unexpected error: " + ex.getMessage())));
            }
        }, "AssetGenerator-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void onGenerationComplete(GenerationResult result) {
        generateBtn.setEnabled(true);
        lastResult = result;

        if (result.success()) {
            previewBtn.setEnabled(true);
            saveBtn.setEnabled(true);
            setStatus("✓ " + result.message(), new Color(0, 120, 0));
            String fingerprint = result.generationFingerprint() == null ? "" : result.generationFingerprint();
            String shortFingerprint = fingerprint.length() > 12 ? fingerprint.substring(0, 12) : fingerprint;
            setStatus("Generated " + result.assetName()
                            + (shortFingerprint.isBlank() ? "" : "  [" + shortFingerprint + "]"),
                    new Color(0, 120, 0));
            log.info("Generation succeeded: path={} fingerprint={}", result.outputPath(), fingerprint);
            // Once a preview context exists it tracks successful generations.
            // Auto-preview only controls opening the first preview window.
            if (autoPreview.isSelected() || AssetViewerApp.hasLivePreview()) onPreview();
        } else {
            setStatus("✗ Generation failed (see details)", new Color(180, 0, 0));
            log.warn("Generation failed for {}: {}", activeGenerator.descriptor().generatorId(), result.message());
            FailurePresentation failure = presentFailure(result.message());
            JPanel content = new JPanel(new BorderLayout(0, 10));
            JTextArea summary = new JTextArea(failure.summary());
            summary.setEditable(false); summary.setOpaque(false); summary.setLineWrap(true); summary.setWrapStyleWord(true);
            summary.setColumns(58); summary.setFont(UIManager.getFont("Label.font"));
            content.add(summary, BorderLayout.NORTH);
            JTextArea details = new JTextArea(failure.details(), 8, 72);
            details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true); details.setCaretPosition(0);
            JScrollPane detailScroll = new JScrollPane(details);
            detailScroll.setBorder(BorderFactory.createTitledBorder("Technical details"));
            content.add(detailScroll, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this,
                    content, "Generation failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    static FailurePresentation presentFailure(String rawMessage) {
        String details = rawMessage == null || rawMessage.isBlank() ? "No technical details were supplied." : rawMessage;
        if (details.contains("Cannot triangulate invalid topology")) {
            return new FailurePresentation(
                    "The generated tree folded across itself, so a valid mesh could not be created.\n\n"
                            + "Try reducing trunk, branch, or root gnarliness; lowering gnarliness frequency; "
                            + "or increasing the corresponding ring count.", details);
        }
        return new FailurePresentation("The asset could not be generated. Review the technical details below.", details);
    }

    record FailurePresentation(String summary, String details) { }

    private void scheduleLiveGeneration(AuthoringGeneratorProvider provider) {
        if (provider == activeGenerator && liveRegenerate != null && liveRegenerate.isSelected()
                && generateBtn != null && generateBtn.isEnabled()) liveTimer.restart();
    }

    private void onPreview() {
        if (lastResult == null || !lastResult.success() || lastResult.outputPath() == null) {
            return;
        }
        Asset temp = Asset.builder()
                .name(lastResult.assetName() != null ? lastResult.assetName() : "Preview")
                .filePath(lastResult.outputPath().toAbsolutePath().toString())
                .build();
        AssetViewerApp.openForAsset(temp);
    }

    private void onSaveToLibrary() {
        if (lastResult == null || !lastResult.success() || lastResult.outputPath() == null) {
            return;
        }

        // Ask the user to confirm/rename the asset
        JPanel dlgPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField nameField = new JTextField(
                lastResult.assetName() != null ? lastResult.assetName() : "GeneratedAsset");
        JComboBox<AssetType> typeCombo = new JComboBox<>(AssetType.values());
        typeCombo.setSelectedItem(AssetType.MODEL);
        JTextField notesField = new JTextField("Generated by " + activeGenerator.descriptor().displayName());
        dlgPanel.add(new JLabel("Asset Name:"));  dlgPanel.add(nameField);
        dlgPanel.add(new JLabel("Asset Type:"));  dlgPanel.add(typeCombo);
        dlgPanel.add(new JLabel("Notes:"));       dlgPanel.add(notesField);

        int choice = JOptionPane.showConfirmDialog(this, dlgPanel,
                "Save Generated Asset to Library", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Asset name cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Copy generated file to a stable library location (~/.planeguardian/generated/)
        Path stableDir = Path.of(System.getProperty("user.home"), ".planeguardian", "generated");
        try {
            Files.createDirectories(stableDir);
            String fileName = name.replaceAll("[^a-zA-Z0-9._-]", "_")
                    + "_" + System.currentTimeMillis()
                    + extension(lastResult.outputPath().getFileName().toString());
            Path stablePath = stableDir.resolve(fileName);
            Files.copy(lastResult.outputPath(), stablePath);

            // Persist Asset (v1 GENERATED)
            Asset asset = Asset.builder()
                    .name(name)
                    .filePath(stablePath.toAbsolutePath().toString())
                    .assetType((AssetType) typeCombo.getSelectedItem())
                    .includeInExport(true)
                    .metadata(generationMetadata(lastResult))
                    .build();
            assetRepo.save(asset);

            AssetVersion version = AssetVersion.builder()
                    .assetId(asset.getId())
                    .filePath(stablePath.toAbsolutePath().toString())
                    .source(VersionSource.GENERATED)
                    .notes(notesField.getText().trim())
                    .build();
            versionRepo.save(version);

            setStatus("✓ Saved '" + name + "' to library (v" + version.getVersionNumber() + ")",
                    new Color(0, 120, 0));
            log.info("Saved generated asset '{}' id={} v{} → {}",
                    name, asset.getId(), version.getVersionNumber(), stablePath);
            JOptionPane.showMessageDialog(this,
                    "Asset '" + name + "' saved to library (v" + version.getVersionNumber() + ").",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            log.error("Failed to save asset to library", ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to save asset:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- helpers ----------------------------------------------------------

    private void setStatus(String text, Color color) {
        statusLabel.setText("  " + text);
        statusLabel.setForeground(color);
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    private static String generationMetadata(GenerationResult result) {
        if (result.generatorId().isBlank() || result.generationFingerprint().isBlank()) return null;
        JsonObject metadata = new JsonObject();
        metadata.addProperty("generatorId", result.generatorId());
        metadata.addProperty("generationFingerprint", result.generationFingerprint());
        return metadata.toString();
    }

    // ---- cell renderer ----------------------------------------------------

    private static class GeneratorCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            if (value instanceof AuthoringGeneratorProvider gen) {
                setText(gen.descriptor().displayName());
            }
            return this;
        }
    }
}
