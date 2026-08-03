package com.planeguardian.assets.tools;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.planeguardian.assets.model.Asset;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Standalone JME3 {@link SimpleApplication} that runs in its own LWJGL3 window
 * and displays any asset whose file path can be resolved by the JME3 asset
 * manager.
 *
 * <p>Usage: {@code AssetViewerApp.openForAsset(asset)} — call from EDT or any
 * thread.</p>
 *
 * <p>The viewer is a singleton per JVM session; closing the LWJGL3 window
 * destroys the app. The next call to {@link #openForAsset} will create a fresh
 * instance.</p>
 */
@Slf4j
public class AssetViewerApp extends SimpleApplication {

    // ---- Singleton lifecycle ------------------------------------------------

    private static volatile AssetViewerApp instance;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Opens (or re-uses) the 3-D viewer and loads the given asset.
     * Safe to call from any thread.
     */
    public static void openForAsset(Asset asset) {
        if (asset == null || asset.getFilePath() == null || asset.getFilePath().isBlank()) {
            log.warn("openForAsset called with null/empty asset path");
            return;
        }

        if (!running.get()) {
            AppSettings settings = new AppSettings(true);
            settings.setTitle("Asset Viewer – " + asset.getName());
            settings.setWidth(960);
            settings.setHeight(720);
            settings.setVSync(true);
            settings.setFrameRate(60);

            AssetViewerApp app = new AssetViewerApp();
            app.setSettings(settings);
            app.setShowSettings(false);
            app.setPauseOnLostFocus(false);
            instance = app;

            running.set(true);
            Thread appThread = new Thread(() -> {
                try {
                    app.start();
                } finally {
                    running.set(false);
                    instance = null;
                    log.debug("AssetViewerApp stopped");
                }
            }, "JME3-AssetViewer");
            appThread.setDaemon(true);
            appThread.start();

            // Give the engine time to initialise before enqueuing work
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        AssetViewerApp current = instance;
        if (current != null) {
            current.requestLoadAsset(asset.getFilePath(), asset.getName());
        }
    }

    // ---- JME3 state --------------------------------------------------------

    /** Scene pivot; the ChaseCamera orbits this node. */
    private Node pivotNode;
    private AnimComposer currentAnimComposer;
    private AnimControlPanel controlPanel;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        setDisplayFps(true);
        setDisplayStatView(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.18f, 0.18f, 0.22f, 1f));

        // Allow loading arbitrary files by absolute path
        assetManager.registerLocator("/", FileLocator.class);

        // Lighting
        AmbientLight ambient = new AmbientLight(new ColorRGBA(0.45f, 0.45f, 0.45f, 1f));
        rootNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(1.4f));
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sun);

        // Pivot for orbit camera
        pivotNode = new Node("pivot");
        rootNode.attachChild(pivotNode);

        ChaseCamera chaseCam = new ChaseCamera(cam, pivotNode, inputManager);
        chaseCam.setDefaultDistance(6f);
        chaseCam.setMinDistance(0.5f);
        chaseCam.setMaxDistance(100f);
        chaseCam.setDefaultVerticalRotation(FastMath.PI / 8f);
        chaseCam.setDragToRotate(true);

        // Open animation control panel on EDT
        SwingUtilities.invokeLater(() -> {
            controlPanel = new AnimControlPanel(this);
            controlPanel.setVisible(true);
        });
    }

    /**
     * Schedules a model load on the JME3 render thread via {@link #enqueue}.
     */
    public void requestLoadAsset(String filePath, String assetName) {
        enqueue(() -> loadModelOnRenderThread(filePath, assetName));
    }

    // ---- Render-thread helpers --------------------------------------------

    private void loadModelOnRenderThread(String filePath, String assetName) {
        // Clear previous model
        pivotNode.detachAllChildren();
        currentAnimComposer = null;

        try {
            // Register the asset's parent directory so relative references resolve
            File assetFile = new File(filePath);
            String dir = assetFile.getParent();
            if (dir != null) {
                assetManager.registerLocator(dir, FileLocator.class);
            }

            Spatial spatial = assetManager.loadModel(assetFile.getName());
            spatial.center();
            pivotNode.attachChild(spatial);

            // Discover new-style animations
            currentAnimComposer = findControl(spatial, AnimComposer.class);
            List<String> clips;
            if (currentAnimComposer != null) {
                clips = new ArrayList<>(currentAnimComposer.getAnimClipsNames());
                log.info("Loaded '{}' – {} animation clip(s)", assetName, clips.size());
            } else {
                clips = Collections.emptyList();
                log.info("Loaded '{}' – no AnimComposer found", assetName);
            }

            List<String> finalClips = clips;
            if (controlPanel != null) {
                SwingUtilities.invokeLater(() -> controlPanel.updateAnimClips(finalClips));
            }
        } catch (Exception e) {
            log.error("Failed to load asset '{}' from path: {}", assetName, filePath, e);
        }
    }

    /** Starts the named animation clip. Call from any thread. */
    public void playAnimation(String clipName) {
        enqueue(() -> {
            if (currentAnimComposer != null) {
                try {
                    currentAnimComposer.setCurrentAction(clipName);
                    log.debug("Playing animation: {}", clipName);
                } catch (Exception e) {
                    log.error("Failed to play animation '{}'", clipName, e);
                }
            }
        });
    }

    /** Stops the current animation clip. Call from any thread. */
    public void stopAnimation() {
        enqueue(() -> {
            if (currentAnimComposer != null) {
                currentAnimComposer.reset();
                log.debug("Animation stopped");
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        running.set(false);
        instance = null;
        AnimControlPanel panel = controlPanel;
        if (panel != null) {
            SwingUtilities.invokeLater(panel::dispose);
            controlPanel = null;
        }
    }

    // ---- Utility ----------------------------------------------------------

    /** Depth-first search for a {@link com.jme3.scene.control.Control} in the scene graph. */
    @SuppressWarnings("unchecked")
    private <T> T findControl(Spatial spatial, Class<T> controlClass) {
        var ctrl = spatial.getControl((Class<com.jme3.scene.control.Control>) (Class<?>) controlClass);
        if (ctrl != null) {
            return (T) ctrl;
        }
        if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                T found = findControl(child, controlClass);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
