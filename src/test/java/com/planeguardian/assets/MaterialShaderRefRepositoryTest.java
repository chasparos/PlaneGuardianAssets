package com.planeguardian.assets;

import com.planeguardian.assets.db.AssetRepository;
import com.planeguardian.assets.db.DatabaseManager;
import com.planeguardian.assets.db.MaterialShaderRefRepository;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.model.AssetType;
import com.planeguardian.assets.model.MaterialShaderRef;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link MaterialShaderRefRepository} using an in-memory H2 database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MaterialShaderRefRepositoryTest {

    private static AssetRepository assetRepo;
    private static MaterialShaderRefRepository refRepo;
    private static Long testAssetId;

    @BeforeAll
    static void setUpDatabase() {
        System.setProperty("planeguardian.db.url",
                "jdbc:h2:mem:testdb_materialrefs;DB_CLOSE_DELAY=-1;MODE=MySQL");
        DatabaseManager.initialize();
        assetRepo = new AssetRepository();
        refRepo   = new MaterialShaderRefRepository();

        // Create a parent asset for tests that need one
        Asset asset = Asset.builder()
                .name("Test Tree Asset")
                .filePath("/tmp/tree.glb")
                .assetType(AssetType.MODEL)
                .includeInExport(true)
                .build();
        assetRepo.save(asset);
        testAssetId = asset.getId();
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void savePersistsNewRef() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .assetId(testAssetId)
                .materialName("Bark_Material")
                .shaderId("procedural_wind_bark")
                .shaderParameters("{\"wind_sway_amplitude\":0.15,\"wind_speed_multiplier\":1.2}")
                .build();

        refRepo.save(ref);

        assertNotNull(ref.getId(), "id should be assigned after persist");
    }

    @Test
    @Order(2)
    void findByAssetIdReturnsSavedRefs() {
        List<MaterialShaderRef> refs = refRepo.findByAssetId(testAssetId);
        assertFalse(refs.isEmpty());
        assertTrue(refs.stream().anyMatch(r -> "Bark_Material".equals(r.getMaterialName())));
    }

    @Test
    @Order(3)
    void findByIdReturnsRef() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .assetId(testAssetId)
                .materialName("Leaf_Material")
                .shaderId("procedural_wind_leaf")
                .build();
        refRepo.save(ref);

        Optional<MaterialShaderRef> found = refRepo.findById(ref.getId());
        assertTrue(found.isPresent());
        assertEquals("Leaf_Material", found.get().getMaterialName());
        assertEquals("procedural_wind_leaf", found.get().getShaderId());
    }

    @Test
    @Order(4)
    void updateChangesPersistedFields() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .assetId(testAssetId)
                .materialName("Trunk_Material")
                .shaderId("unshaded_trunk")
                .build();
        refRepo.save(ref);

        ref.setShaderId("lit_trunk");
        ref.setShaderParameters("{\"roughness\":0.8}");
        refRepo.save(ref);

        Optional<MaterialShaderRef> reloaded = refRepo.findById(ref.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("lit_trunk", reloaded.get().getShaderId());
        assertEquals("{\"roughness\":0.8}", reloaded.get().getShaderParameters());
    }

    @Test
    @Order(5)
    void deleteByAssetIdRemovesAllRefs() {
        Asset asset = Asset.builder()
                .name("Temp Asset")
                .assetType(AssetType.MODEL)
                .includeInExport(false)
                .build();
        assetRepo.save(asset);

        refRepo.save(MaterialShaderRef.builder()
                .assetId(asset.getId())
                .materialName("Mat_A")
                .shaderId("shader_a")
                .build());
        refRepo.save(MaterialShaderRef.builder()
                .assetId(asset.getId())
                .materialName("Mat_B")
                .shaderId("shader_b")
                .build());

        assertFalse(refRepo.findByAssetId(asset.getId()).isEmpty());

        refRepo.deleteByAssetId(asset.getId());

        assertTrue(refRepo.findByAssetId(asset.getId()).isEmpty(),
                "All refs for the asset should be deleted");
    }

    @Test
    @Order(6)
    void deleteByIdRemovesSingleRef() {
        MaterialShaderRef ref = MaterialShaderRef.builder()
                .assetId(testAssetId)
                .materialName("Transient_Mat")
                .shaderId("transient_shader")
                .build();
        refRepo.save(ref);
        Long id = ref.getId();

        refRepo.delete(id);

        assertFalse(refRepo.findById(id).isPresent(), "Ref should be gone after delete");
    }

    @Test
    @Order(7)
    void findAllReturnsAllRefs() {
        List<MaterialShaderRef> all = refRepo.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }
}
