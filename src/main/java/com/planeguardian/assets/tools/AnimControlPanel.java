package com.planeguardian.assets.tools;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Swing panel that controls animation playback in a running
 * {@link AssetViewerApp}.  Shown automatically whenever the 3-D viewer loads
 * a model; stays on top of the viewer window.
 */
@Slf4j
public class AnimControlPanel extends JFrame {

    private final AssetViewerApp viewerApp;

    private JComboBox<String> clipCombo;
    private JButton playButton;
    private JButton stopButton;
    private JLabel statusLabel;

    public AnimControlPanel(AssetViewerApp viewerApp) {
        super("Animation Controls");
        this.viewerApp = viewerApp;
        buildUI();
        setSize(380, 160);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void buildUI() {
        setLayout(new BorderLayout(5, 5));

        // ---- Clip selector ----
        JPanel clipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        clipPanel.add(new JLabel("Clip:"));
        clipCombo = new JComboBox<>();
        clipCombo.setPreferredSize(new Dimension(220, 26));
        clipPanel.add(clipCombo);

        // ---- Playback buttons ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        playButton = new JButton("▶  Play");
        stopButton = new JButton("■  Stop");
        playButton.setEnabled(false);
        stopButton.setEnabled(false);

        playButton.addActionListener(e -> {
            String clip = (String) clipCombo.getSelectedItem();
            if (clip != null) {
                viewerApp.playAnimation(clip);
                statusLabel.setText("Playing: " + clip);
                stopButton.setEnabled(true);
            }
        });
        stopButton.addActionListener(e -> {
            viewerApp.stopAnimation();
            statusLabel.setText("Stopped");
            stopButton.setEnabled(false);
        });

        btnPanel.add(playButton);
        btnPanel.add(stopButton);

        // ---- Status ----
        statusLabel = new JLabel("No model loaded", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));

        add(clipPanel, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Called from the JME3 thread (via {@code SwingUtilities.invokeLater}) after
     * a model is loaded to populate the clip list.
     */
    public void updateAnimClips(List<String> clips) {
        clipCombo.removeAllItems();
        if (clips == null || clips.isEmpty()) {
            statusLabel.setText("No animations found in this model");
            playButton.setEnabled(false);
        } else {
            clips.forEach(clipCombo::addItem);
            statusLabel.setText(clips.size() + " animation clip(s) available");
            playButton.setEnabled(true);
        }
        stopButton.setEnabled(false);
    }
}
