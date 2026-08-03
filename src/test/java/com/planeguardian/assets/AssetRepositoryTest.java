package com.planeguardian.assets;

import com.planeguardian.assets.db.AssetRepository;
import com.planeguardian.assets.db.DatabaseManager;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.model.AssetType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link AssetRepository} using an in-memory H2 database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetRepositoryTest {

    private static AssetRepository repo;

    @BeforeAll
    static void setUpDatabase() {
        // Use an in-memory H2 database so tests don't touch the real file DB
        System.setProperty("planeguardian.db.url",
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        DatabaseManager.initialize();
        repo = new AssetRepository();
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.shutdown();
    }

    @Test
    @Order(1)
    void savePersistsNewAsset() {
        Asset asset = Asset.builder()
                .name("Test Fighter Jet")
                .filePath("/tmp/fighter.glb")
                .assetType(AssetType.MODEL)
                .includeInExport(true)
                .build();

        repo.save(asset);

        assertNotNull(asset.getId(), "id should be assigned after persist");
        List<Asset> all = repo.findAll();
        assertTrue(all.stream().anyMatch(a -> "Test Fighter Jet".equals(a.getName())));
    }

    @Test
    @Order(2)
    void findByIdReturnsAsset() {
        Asset asset = Asset.builder()
                .name("Findable Asset")
                .assetType(AssetType.TEXTURE)
                .includeInExport(false)
                .build();
        repo.save(asset);

        Optional<Asset> found = repo.findById(asset.getId());
        assertTrue(found.isPresent());
        assertEquals("Findable Asset", found.get().getName());
    }

    @Test
    @Order(3)
    void findIncludedInExportFiltersCorrectly() {
        // Save one included and one excluded
        repo.save(Asset.builder().name("ExportedA").assetType(AssetType.MODEL)
                .includeInExport(true).build());
        repo.save(Asset.builder().name("ExcludedB").assetType(AssetType.MODEL)
                .includeInExport(false).build());

        List<Asset> included = repo.findIncludedInExport();
        assertNotNull(included);
        assertTrue(included.stream().allMatch(Asset::isIncludeInExport),
                "findIncludedInExport should only return flagged assets");
        assertFalse(included.stream().anyMatch(a -> "ExcludedB".equals(a.getName())));
    }

    @Test
    @Order(4)
    void updateChangesPersistedFields() {
        Asset asset = Asset.builder()
                .name("Before Update")
                .assetType(AssetType.MODEL)
                .includeInExport(true)
                .build();
        repo.save(asset);

        asset.setName("After Update");
        asset.setIncludeInExport(false);
        repo.save(asset);

        Optional<Asset> reloaded = repo.findById(asset.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("After Update", reloaded.get().getName());
        assertFalse(reloaded.get().isIncludeInExport());
    }

    @Test
    @Order(5)
    void deleteRemovesAsset() {
        Asset asset = Asset.builder()
                .name("To Delete")
                .assetType(AssetType.OTHER)
                .includeInExport(false)
                .build();
        repo.save(asset);
        Long id = asset.getId();

        repo.delete(id);

        Optional<Asset> deleted = repo.findById(id);
        assertFalse(deleted.isPresent(), "Asset should be gone after delete");
    }
}
