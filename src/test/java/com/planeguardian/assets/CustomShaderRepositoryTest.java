package com.planeguardian.assets;

import com.planeguardian.assets.db.CustomShaderRepository;
import com.planeguardian.assets.db.DatabaseManager;
import com.planeguardian.assets.model.CustomShader;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CustomShaderRepository} using an in-memory H2 database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomShaderRepositoryTest {

    private static CustomShaderRepository repo;

    @BeforeAll
    static void setUpDatabase() {
        System.setProperty("planeguardian.db.url",
                "jdbc:h2:mem:testdb_shaders;DB_CLOSE_DELAY=-1;MODE=MySQL");
        DatabaseManager.initialize();
        repo = new CustomShaderRepository();
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void savePersistsNewShader() {
        CustomShader shader = CustomShader.builder()
                .shaderId("procedural_wind_bark")
                .displayName("Procedural Wind Bark")
                .description("Animates bark geometry with wind")
                .parameterSchema("{\"wind_sway_amplitude\":{\"type\":\"float\",\"default\":0.1}}")
                .standardJme3(false)
                .build();

        repo.save(shader);

        assertNotNull(shader.getId(), "id should be assigned after persist");
        assertNotNull(shader.getCreatedAt());
    }

    @Test
    @Order(2)
    void findByShaderIdReturnsShader() {
        Optional<CustomShader> found = repo.findByShaderId("procedural_wind_bark");
        assertTrue(found.isPresent());
        assertEquals("Procedural Wind Bark", found.get().getDisplayName());
        assertFalse(found.get().isStandardJme3());
    }

    @Test
    @Order(3)
    void findAllReturnsAllShaders() {
        repo.save(CustomShader.builder()
                .shaderId("Common/MatDefs/Light/Lighting")
                .displayName("JME3 Lighting")
                .standardJme3(true)
                .build());

        List<CustomShader> all = repo.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    @Order(4)
    void findCustomOnlyExcludesJme3Shaders() {
        List<CustomShader> custom = repo.findCustomOnly();
        assertTrue(custom.stream().noneMatch(CustomShader::isStandardJme3),
                "findCustomOnly should not return JME3 standard shaders");
        assertTrue(custom.stream().anyMatch(s -> "procedural_wind_bark".equals(s.getShaderId())));
    }

    @Test
    @Order(5)
    void updateChangesPersistedFields() {
        CustomShader shader = CustomShader.builder()
                .shaderId("wind_leaves_v1")
                .displayName("Wind Leaves")
                .standardJme3(false)
                .build();
        repo.save(shader);

        shader.setDisplayName("Wind Leaves v2");
        shader.setDescription("Updated description");
        repo.save(shader);

        Optional<CustomShader> reloaded = repo.findById(shader.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("Wind Leaves v2", reloaded.get().getDisplayName());
        assertEquals("Updated description", reloaded.get().getDescription());
    }

    @Test
    @Order(6)
    void deleteRemovesShader() {
        CustomShader shader = CustomShader.builder()
                .shaderId("to_delete_shader")
                .displayName("Delete Me")
                .standardJme3(false)
                .build();
        repo.save(shader);
        Long id = shader.getId();

        repo.delete(id);

        assertFalse(repo.findById(id).isPresent(), "Shader should be gone after delete");
    }

    @Test
    @Order(7)
    void findByShaderIdReturnsEmptyForUnknown() {
        Optional<CustomShader> found = repo.findByShaderId("nonexistent_shader_xyz");
        assertFalse(found.isPresent());
    }
}
