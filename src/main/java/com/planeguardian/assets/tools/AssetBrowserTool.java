package com.planeguardian.assets.tools;

import com.planeguardian.assets.db.AssetRepository;
import com.planeguardian.assets.export.ExportManager;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.model.AssetType;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;

/**
 * Main asset-management UI.
 *
 * <p>Left panel – scrollable list of all assets in H2.
 * Right panel – editable details for the selected asset including the
 * <em>Include in Export</em> toggle.  Toolbar provides Import glTF/glb,
 * 3-D viewer shortcut, and Export Library.</p>
 */
@Slf4j
public class AssetBrowserTool extends JFrame {

    private final AssetRepository assetRepository = new AssetRepository();

    // ---- List ----
    private DefaultListModel<Asset> listModel;
    private JList<Asset> assetList;

    // ---- Detail fields ----
    private JLabel idLabel;
    private JTextField nameField;
    private JTextField pathField;
    private JComboBox<AssetType> typeCombo;
    private JCheckBox includeInExportCheck;
    private JButton saveBtn;
    private JButton viewBtn;

    public AssetBrowserTool() {
        super("Asset Browser");
        setSize(1000, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        refreshAssetList();
    }

    // ---- UI construction --------------------------------------------------

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildToolBar(), BorderLayout.NORTH);
        add(buildListPanel(), BorderLayout.WEST);
        add(buildDetailsPanel(), BorderLayout.CENTER);
    }

    private JToolBar buildToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        JButton newBtn = new JButton("New Asset");
        JButton importBtn = new JButton("Import glTF / glb…");
        JButton removeBtn = new JButton("Remove");
        JButton refreshBtn = new JButton("⟳  Refresh");
        JButton exportBtn = new JButton("⬆  Export Library");

        newBtn.addActionListener(e -> onNewAsset());
        importBtn.addActionListener(e -> onImportGltf());
        removeBtn.addActionListener(e -> onRemoveAsset());
        refreshBtn.addActionListener(e -> refreshAssetList());
        exportBtn.addActionListener(e -> ExportManager.exportLibrary(this));

        bar.add(newBtn);
        bar.add(importBtn);
        bar.add(removeBtn);
        bar.addSeparator();
        bar.add(refreshBtn);
        bar.add(Box.createHorizontalGlue());
        bar.add(exportBtn);
        return bar;
    }

    private JScrollPane buildListPanel() {
        listModel = new DefaultListModel<>();
        assetList = new JList<>(listModel);
        assetList.setCellRenderer(new AssetCellRenderer());
        assetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assetList.addListSelectionListener(this::onSelectionChanged);

        JScrollPane scroll = new JScrollPane(assetList);
        scroll.setPreferredSize(new Dimension(290, 0));
        scroll.setBorder(BorderFactory.createTitledBorder("Assets"));
        return scroll;
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Asset Details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;

        // ID (read-only)
        idLabel = new JLabel("—");
        addRow(form, c, 0, "ID:", idLabel);

        // Name
        nameField = new JTextField(32);
        addRow(form, c, 1, "Name:", nameField);

        // File path + browse button
        pathField = new JTextField(32);
        pathField.setEditable(false);
        JButton browseBtn = new JButton("Browse…");
        browseBtn.addActionListener(e -> onBrowseFile());
        JPanel pathRow = new JPanel(new BorderLayout(4, 0));
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.add(browseBtn, BorderLayout.EAST);
        addRow(form, c, 2, "File Path:", pathRow);

        // Asset type
        typeCombo = new JComboBox<>(AssetType.values());
        addRow(form, c, 3, "Type:", typeCombo);

        // Include-in-export toggle
        includeInExportCheck = new JCheckBox("Include in Export");
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        form.add(includeInExportCheck, c);
        c.gridwidth = 1;

        // Action buttons
        saveBtn = new JButton("Save Changes");
        viewBtn = new JButton("View in 3-D Viewer");
        saveBtn.setEnabled(false);
        viewBtn.setEnabled(false);
        saveBtn.addActionListener(e -> onSaveAsset());
        viewBtn.addActionListener(e -> onViewAsset());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.add(saveBtn);
        btns.add(viewBtn);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        form.add(btns, c);

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    /** Helper to add a label + component row in the GridBag form. */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), c);

        c.gridx = 1; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    // ---- Asset list -------------------------------------------------------

    public void refreshAssetList() {
        Asset selected = assetList.getSelectedValue();
        listModel.clear();

        List<Asset> assets = assetRepository.findAll();
        assets.forEach(listModel::addElement);

        // Re-select the previously selected asset
        if (selected != null) {
            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.getElementAt(i).getId().equals(selected.getId())) {
                    assetList.setSelectedIndex(i);
                    return;
                }
            }
        }
        populateDetails(null);
    }

    private void onSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            populateDetails(assetList.getSelectedValue());
        }
    }

    private void populateDetails(Asset asset) {
        if (asset == null) {
            idLabel.setText("—");
            nameField.setText("");
            pathField.setText("");
            typeCombo.setSelectedItem(AssetType.MODEL);
            includeInExportCheck.setSelected(true);
            saveBtn.setEnabled(false);
            viewBtn.setEnabled(false);
            return;
        }
        idLabel.setText(String.valueOf(asset.getId()));
        nameField.setText(asset.getName() != null ? asset.getName() : "");
        pathField.setText(asset.getFilePath() != null ? asset.getFilePath() : "");
        typeCombo.setSelectedItem(asset.getAssetType() != null ? asset.getAssetType() : AssetType.MODEL);
        includeInExportCheck.setSelected(asset.isIncludeInExport());
        saveBtn.setEnabled(true);
        viewBtn.setEnabled(hasFilePath(asset));
    }

    // ---- Actions ----------------------------------------------------------

    private void onNewAsset() {
        Asset asset = Asset.builder()
                .name("New Asset")
                .assetType(AssetType.MODEL)
                .includeInExport(true)
                .build();
        assetRepository.save(asset);
        refreshAssetList();
        selectById(asset.getId());
    }

    private void onImportGltf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import glTF / glb File");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "glTF Files (*.gltf, *.glb)", "gltf", "glb"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            // Use filename without extension as default name
            String name = file.getName().replaceFirst("\\.[^.]+$", "");
            Asset asset = Asset.builder()
                    .name(name)
                    .filePath(file.getAbsolutePath())
                    .assetType(AssetType.MODEL)
                    .includeInExport(true)
                    .build();
            assetRepository.save(asset);
            refreshAssetList();
            selectById(asset.getId());
            log.info("Imported glTF asset: {} → {}", name, file.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Imported: " + name, "Import Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onBrowseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Asset File");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "3-D Assets (*.gltf, *.glb, *.j3o, *.obj)", "gltf", "glb", "j3o", "obj"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            viewBtn.setEnabled(true);
        }
    }

    private void onSaveAsset() {
        Asset asset = assetList.getSelectedValue();
        if (asset == null) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Asset name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        asset.setName(name);
        asset.setFilePath(pathField.getText().trim());
        asset.setAssetType((AssetType) typeCombo.getSelectedItem());
        asset.setIncludeInExport(includeInExportCheck.isSelected());
        assetRepository.save(asset);
        refreshAssetList();
        log.info("Saved asset: {}", asset.getName());
    }

    private void onRemoveAsset() {
        Asset asset = assetList.getSelectedValue();
        if (asset == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Remove '" + asset.getName() + "' from the library?\n(The file on disk will NOT be deleted.)",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            assetRepository.delete(asset.getId());
            refreshAssetList();
        }
    }

    private void onViewAsset() {
        Asset asset = assetList.getSelectedValue();
        if (asset == null || !hasFilePath(asset)) {
            JOptionPane.showMessageDialog(this,
                    "Please assign a file path to this asset first.",
                    "No File Path", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AssetViewerApp.openForAsset(asset);
    }

    // ---- Helpers ----------------------------------------------------------

    private static boolean hasFilePath(Asset asset) {
        return asset.getFilePath() != null && !asset.getFilePath().isBlank();
    }

    private void selectById(Long id) {
        for (int i = 0; i < listModel.getSize(); i++) {
            if (listModel.getElementAt(i).getId().equals(id)) {
                assetList.setSelectedIndex(i);
                return;
            }
        }
    }

    // ---- Cell renderer ----------------------------------------------------

    private static class AssetCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            if (value instanceof Asset asset) {
                String type = asset.getAssetType() != null
                        ? "[" + asset.getAssetType() + "] " : "";
                String include = asset.isIncludeInExport() ? "✓ " : "  ";
                setText(include + type + asset.getName());
                setToolTipText(asset.getFilePath());
            }
            return this;
        }
    }
}
