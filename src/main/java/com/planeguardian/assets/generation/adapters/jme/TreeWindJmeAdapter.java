package com.planeguardian.assets.generation.adapters.jme;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.planeguardian.assets.generation.preview.RuntimeWeatherInput;
import com.planeguardian.assets.generation.tree.TreePresentationSettings;
import com.planeguardian.assets.generation.tree.TreeWindResponse;

import java.util.Objects;

/** Binds semantic per-part response and mutable scene weather to the tree wind shader. */
public final class TreeWindJmeAdapter {
    private TreeWindJmeAdapter() {
    }

    public static void bind(Material material, TreeWindResponse response, TreePresentationSettings settings,
                            RuntimeWeatherInput weather) {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(weather, "weather");
        material.setFloat("WindWeight", (float) response.weight());
        material.setFloat("WindPhase", (float) response.phaseOffset());
        material.setFloat("WindFrequency", (float) settings.windFrequency());
        material.setVector3("WindDirection", new Vector3f((float) weather.windDirection().x(),
                (float) weather.windDirection().y(), (float) weather.windDirection().z()));
        material.setFloat("WindIntensity", (float) weather.intensity());
        material.setFloat("WindTime", (float) weather.elapsedSeconds());
    }
}
