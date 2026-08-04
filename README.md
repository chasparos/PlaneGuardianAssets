# PlaneGuardian Asset Library

A Java/Maven tool-suite for managing, browsing, procedurally generating, and exporting game assets for the **PlaneGuardian** game.

## Developer Guides

- [Geometry Construction Toolkit](docs/guides/geometry-toolkit.md) — compose the
  shared ProtoMesh, curve, tube, patch, and constructive operations before
  introducing asset-specific geometry.
- [Generation Platform Architecture](docs/architecture/generation-platform.md)
  — durable contracts and package boundaries for procedural generators.

## Technology Stack

| Concern           | Library / Version                         |
|-------------------|-------------------------------------------|
| 3-D Engine        | JMonkeyEngine 3 `3.7.0-stable` (LWJGL 3) |
| Database          | H2 (embedded, `~/.planeguardian/assets`)  |
| Persistence       | Direct JDBC repositories over H2         |
| Boilerplate       | Lombok                                    |
| Logging           | SLF4J 2 + Logback                         |
| JSON export       | Gson 2.10                                 |
| Build             | Maven 3, Java 17, fat-JAR via Shade plugin |

## Running

```bash
.\mvnw.cmd package -q
java -jar target/planeguardian-assets-1.0.0-SNAPSHOT.jar
```

Or directly during development:

```bash
.\mvnw.cmd compile exec:java "-Dexec.mainClass=com.planeguardian.assets.Main"
```

## Human validation publication

When this environment cannot run tests with the required JDK, run the appropriate
publisher from a local session configured with the compatible JDK:

```powershell
.\PublishValidationArtifacts.ps1
```

```bash
./PublishValidationArtifacts.sh
```

Each publisher invokes its platform-specific patch sequence with an empty patch
argument, then force-adds and commits `latest test results.log` and `latest snapshot
manifest.json` as a separate artifact-only commit before pushing the active branch. The manifest's
`repository.commit` therefore identifies the source commit that was tested, while the
branch `HEAD` includes the later artifact commit. This difference is expected; use the
manifest commit as the validation baseline. The snapshot ZIP remains untracked.

To apply an agent-provided patch directly from WSL, use:

```bash
./PatchSequence.sh /path/to/change.patch "Describe the applied change"
```

To run the sequence without a patch, retain the empty first argument:

```bash
./PatchSequence.sh "" "Validate current branch"
```

## Application Layout

```
Main (Swing launcher)
 ├── Asset Browser   ← manage & view assets stored in H2
 │    └── 3-D Viewer (JME3 LWJGL3 window + AnimControlPanel)
 ├── Procedural Generator (placeholder)
 └── Export Library  ← writes binary assets + asset_index.json
```

## Asset Browser

* Add a blank asset record or **Import glTF / glb** from disk.
* Edit name, type, and file path; toggle **Include in Export**.
* Click **View in 3-D Viewer** to open a live JME3 window.
* The **Animation Controls** Swing panel lists all `AnimComposer` clips
  from the loaded model; click **▶ Play** to trigger playback.

## Export

All assets with *Include in Export* checked are copied to a chosen
directory alongside an `asset_index.json` manifest consumed by the game
runtime.

```json
{
  "version": "1.0",
  "exportDate": "2025-01-15T12:00:00",
  "totalAssets": 3,
  "assets": [
    { "id": 1, "name": "FighterJet", "assetType": "MODEL",
      "exportedPath": "assets/FighterJet_1.glb", ... }
  ]
}
```

## Project Structure

```
src/main/java/com/planeguardian/assets/
  Main.java                   – tool-launcher Swing JFrame
  model/
    Asset.java                – plain Lombok-backed domain model
    AssetType.java            – enum
  db/
    DatabaseManager.java      – H2 JDBC DataSource and schema owner
    AssetRepository.java      – direct JDBC CRUD
  tools/
    AssetBrowserTool.java     – Swing asset list + details panel
    AssetViewerApp.java       – JME3 SimpleApplication (3-D viewer)
    AnimControlPanel.java     – Swing animation playback controls
    generator/
      AssetGeneratorTool.java      – procedural generator UI and preview
      DeciduousTreeGenerator.java  – current reference generator
  export/
    ExportManager.java        – copies assets + writes asset_index.json
    AssetIndex.java           – JSON root object
    AssetIndexEntry.java      – per-asset entry

src/main/resources/
  logback.xml                 – Logback configuration
  assets/                     – JME3 classpath assets (models, textures …)
```

## JME3 Asset Directory Convention

Place classpath assets (shaders, embedded textures, etc.) under
`src/main/resources/assets/`. JME3's default `ClasspathLocator` picks them
up automatically. External asset files are referenced by their absolute path
stored in H2 and loaded via JME3's `FileLocator`.

## Design documentation

The procedural architecture is organized under
[`docs/procedural-assets`](docs/procedural-assets/README.md). Game-facing
semantic and visual authority remains in the sibling `PlaneGuardianGDD`
repository; consumed references are listed in
[`docs/gdd-references.md`](docs/gdd-references.md).
