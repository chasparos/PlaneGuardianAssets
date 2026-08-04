package com.planeguardian.assets.tools;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.material.Material;
import com.jme3.system.AppSettings;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.export.GltfPersistenceFormat;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
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
    private ChaseCamera chaseCam;
    private AnimComposer currentAnimComposer;
    private AnimControlPanel controlPanel;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        setDisplayFps(true);
        setDisplayStatView(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.025f, 0.028f, 0.035f, 1f));

        // Allow loading arbitrary files by absolute path
        assetManager.registerLocator("/", FileLocator.class);

        addThreePointLighting();

        // Pivot for orbit camera
        pivotNode = new Node("pivot");
        rootNode.attachChild(pivotNode);

        chaseCam = new ChaseCamera(cam, pivotNode, inputManager);
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

            Spatial spatial = loadThroughPersistence(assetFile);
            spatial.center();
            pivotNode.attachChild(spatial);
            spatial.updateGeometricState();
            addPreviewDecorations((BoundingBox) spatial.getWorldBound());

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

    private Spatial loadThroughPersistence(File assetFile) throws IOException {
        String name = assetFile.getName().toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith(".gltf") || name.endsWith(".glb")) {
            return GltfPersistenceFormat.loadAsset(assetManager, assetFile.toPath());
        }
        return assetManager.loadModel(assetFile.getName());
    }

    private void addThreePointLighting() {
        rootNode.addLight(directionalLight(new Vector3f(-0.6f, -1f, -0.4f), ColorRGBA.White.mult(1.25f)));
        rootNode.addLight(directionalLight(new Vector3f(0.7f, -0.55f, -0.25f), new ColorRGBA(.55f, .65f, 1f, 1f)));
        rootNode.addLight(directionalLight(new Vector3f(0.25f, -0.7f, 0.8f), new ColorRGBA(1f, .72f, .48f, 1f)));
    }

    private static DirectionalLight directionalLight(Vector3f direction, ColorRGBA color) {
        DirectionalLight light = new DirectionalLight();
        light.setDirection(direction.normalize());
        light.setColor(color);
        return light;
    }

    private void addPreviewDecorations(BoundingBox bounds) {
        rootNode.detachChildNamed("preview-bounds");
        rootNode.detachChildNamed("preview-floor");
        Vector3f center = bounds.getCenter();
        Geometry outline = new Geometry("preview-bounds",
                new WireBox(bounds.getXExtent(), bounds.getYExtent(), bounds.getZExtent()));
        outline.setLocalTranslation(center);
        outline.setMaterial(unshaded(new ColorRGBA(.92f, .78f, .28f, 1)));
        rootNode.attachChild(outline);

        float halfSize = Math.max(2f, Math.max(bounds.getXExtent(), bounds.getZExtent()) * 1.5f);
        Geometry floor = new Geometry("preview-floor", new Box(halfSize, .025f, halfSize));
        floor.setLocalTranslation(center.x, bounds.getMin(null).y - .025f, center.z);
        floor.setMaterial(unshaded(new ColorRGBA(.17f, .17f, .18f, 1)));
        rootNode.attachChild(floor);
        float radius = Math.max(bounds.getXExtent(), Math.max(bounds.getYExtent(), bounds.getZExtent()));
        chaseCam.setDefaultDistance(Math.max(3f, radius * 3.2f));
    }

    private Material unshaded(ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", color);
        return material;
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
