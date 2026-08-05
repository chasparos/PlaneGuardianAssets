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
import com.jme3.system.lwjgl.LwjglWindow;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.planeguardian.assets.runtime.LoadedAsset;
import com.planeguardian.assets.runtime.EnvironmentState;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.export.GltfPersistenceFormat;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.lwjgl.glfw.GLFW;

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
    private enum ViewerState { IDLE, STARTING, RUNNING, STOPPING, TERMINATED }
    private static final AtomicReference<ViewerState> viewerState = new AtomicReference<>(ViewerState.IDLE);
    private static final AtomicReference<Asset> pendingAsset = new AtomicReference<>();

    /** True while the single preview context exists, whether visible or hidden. */
    public static boolean hasLivePreview() {
        return viewerState.get() == ViewerState.RUNNING && instance != null;
    }

    /**
     * Opens (or re-uses) the 3-D viewer and loads the given asset.
     * Safe to call from any thread.
     */
    public static synchronized void openForAsset(Asset asset) {
        if (asset == null || asset.getFilePath() == null || asset.getFilePath().isBlank()) {
            log.warn("openForAsset called with null/empty asset path");
            return;
        }

        ViewerState state = viewerState.get();
        log.info("Preview request '{}' path={} state={}", asset.getName(), asset.getFilePath(), state);
        if (state == ViewerState.TERMINATED || state == ViewerState.STOPPING) {
            JOptionPane.showMessageDialog(null,
                    "The 3D preview was closed and cannot be restarted safely in this JVM.\nRestart the Asset Tools application to open it again.",
                    "Preview Restart Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        pendingAsset.set(asset);
        if (viewerState.compareAndSet(ViewerState.IDLE, ViewerState.STARTING)) {
            AppSettings settings = new AppSettings(true);
            settings.setTitle("Asset Viewer – " + asset.getName());
            settings.setWidth(960);
            settings.setHeight(720);
            settings.setVSync(true);
            settings.setFrameRate(60);

            AssetViewerApp app = new AssetViewerApp(asset);
            app.setSettings(settings);
            app.setShowSettings(false);
            app.setPauseOnLostFocus(false);
            instance = app;

            // LegacyApplication.start() launches jME's render thread and then
            // returns; returning here does not mean the application stopped.
            // Lifecycle cleanup belongs in destroy(), which represents actual
            // context teardown.
            Thread appThread = new Thread(app::start, "JME3-AssetViewer-Starter");
            appThread.setDaemon(true);
            appThread.start();
            return;
        }

        AssetViewerApp current = instance;
        if (viewerState.get() == ViewerState.RUNNING && current != null) {
            Asset newest = pendingAsset.getAndSet(null);
            if (newest != null) current.requestLoadAsset(newest.getFilePath(), newest.getName());
            current.showPreviewWindow();
        }
    }

    // ---- JME3 state --------------------------------------------------------

    /** Scene pivot; the ChaseCamera orbits this node. */
    private Node pivotNode;
    private ChaseCamera chaseCam;
    private AnimComposer currentAnimComposer;
    private AnimControlPanel controlPanel;
    private LoadedAsset<Node> currentLoadedAsset;
    private double previewElapsedSeconds;
    private final Asset initialAsset;

    private AssetViewerApp(Asset initialAsset) {
        this.initialAsset = java.util.Objects.requireNonNull(initialAsset, "initialAsset");
    }

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
        viewerState.set(ViewerState.RUNNING);
        Asset first = pendingAsset.getAndSet(null);
        if (first == null) first = initialAsset;
        loadModelOnRenderThread(first.getFilePath(), first.getName());
    }

    @Override public void simpleUpdate(float tpf) {
        if (currentLoadedAsset == null) return;
        previewElapsedSeconds += tpf;
        currentLoadedAsset.update(tpf, new EnvironmentState(previewElapsedSeconds,
                new com.planeguardian.assets.generation.api.Vector3(1, 0, .25), .28,
                new java.util.TreeMap<>()));
    }

    /**
     * Schedules a model load on the JME3 render thread via {@link #enqueue}.
     */
    public void requestLoadAsset(String filePath, String assetName) {
        log.info("Queueing preview replacement '{}' from {}", assetName, filePath);
        enqueue(() -> loadModelOnRenderThread(filePath, assetName));
    }

    /** Re-shows the existing native preview window without rebuilding GLFW. */
    private void showPreviewWindow() {
        enqueue(() -> {
            if (getContext() instanceof LwjglWindow window) {
                long handle = window.getWindowHandle();
                GLFW.glfwSetWindowShouldClose(handle, false);
                GLFW.glfwShowWindow(handle);
                GLFW.glfwFocusWindow(handle);
            }
        });
    }

    /**
     * A native close request only hides the preview. Keeping its sole GLFW
     * context alive avoids unsafe teardown/reinitialisation inside the tools JVM.
     */
    @Override
    public void requestClose(boolean esc) {
        if (getContext() instanceof LwjglWindow window) {
            long handle = window.getWindowHandle();
            GLFW.glfwSetWindowShouldClose(handle, false);
            GLFW.glfwHideWindow(handle);
            log.debug("Asset preview hidden");
            return;
        }
        super.requestClose(esc);
    }

    // ---- Render-thread helpers --------------------------------------------

    private void loadModelOnRenderThread(String filePath, String assetName) {
        // Clear previous model
        pivotNode.detachAllChildren();
        currentAnimComposer = null;

        try {
            File previewFile = new File(filePath);
            log.info("Loading preview '{}' from {} ({} bytes, modified={})", assetName, filePath,
                    previewFile.length(), previewFile.lastModified());
            // Register the asset's parent directory so relative references resolve
            File assetFile = new File(filePath);
            String dir = assetFile.getParent();
            if (dir != null) {
                assetManager.registerLocator(dir, FileLocator.class);
            }

            Spatial spatial = AssetPersistenceLoader.load(assetManager, assetFile.toPath());
            currentLoadedAsset = spatial instanceof Node node
                    ? com.planeguardian.assets.generation.adapters.jme.JmeLoadedAssetFactory.wrap(node) : null;
            previewElapsedSeconds = 0;
            spatial.center();
            pivotNode.attachChild(spatial);
            spatial.updateGeometricState();
            if (!(spatial.getWorldBound() instanceof BoundingBox bounds)) {
                throw new IOException("Loaded scene has no finite bounding box");
            }
            log.info("Preview bounds center={} extents=({}, {}, {})", bounds.getCenter(),
                    bounds.getXExtent(), bounds.getYExtent(), bounds.getZExtent());
            addPreviewDecorations(bounds);

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
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Preview failed to load '" + assetName + "'.\n" + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    "Asset Preview Error", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void addThreePointLighting() {
        DirectionalLight key = directionalLight(new Vector3f(-0.6f, -1f, -0.4f), ColorRGBA.White.mult(1.25f));
        rootNode.addLight(key);
        rootNode.addLight(directionalLight(new Vector3f(0.7f, -0.55f, -0.25f), new ColorRGBA(.55f, .65f, 1f, 1f)));
        rootNode.addLight(directionalLight(new Vector3f(0.25f, -0.7f, 0.8f), new ColorRGBA(1f, .72f, .48f, 1f)));
        AmbientLight fill = new AmbientLight();
        fill.setColor(ColorRGBA.White.mult(.72f));
        rootNode.addLight(fill);
        DirectionalLightShadowRenderer shadows = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
        shadows.setLight(key); viewPort.addProcessor(shadows);
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
        float distance = Math.max(3f, radius * 3.2f);
        chaseCam.setDefaultDistance(distance);
        chaseCam.setMinDistance(Math.max(.25f, radius * .2f));
        chaseCam.setMaxDistance(Math.max(100f, radius * 12f));
        cam.setLocation(new Vector3f(0, radius * .35f, distance));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
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
        viewerState.compareAndSet(ViewerState.RUNNING, ViewerState.STOPPING);
        super.destroy();
        viewerState.set(ViewerState.TERMINATED);
        pendingAsset.set(null);
        instance = null;
        log.debug("AssetViewerApp destroyed");
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
