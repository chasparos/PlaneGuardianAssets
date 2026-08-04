package com.planeguardian.assets.generation.preview;

import com.planeguardian.assets.generation.api.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuntimeWeatherInputTest {
    @Test
    void normalizesRuntimeDirectionAndRejectsInvalidMovingWeather() {
        RuntimeWeatherInput weather = new RuntimeWeatherInput(new Vector3(0, 3, 4), .5, 2);

        assertEquals(.6, weather.windDirection().y(), 1e-12);
        assertEquals(.8, weather.windDirection().z(), 1e-12);
        assertThrows(IllegalArgumentException.class, () -> new RuntimeWeatherInput(Vector3.ZERO, .1, 0));
    }
}
