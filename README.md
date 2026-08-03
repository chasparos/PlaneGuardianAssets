# PlaneGuardian Asset Library

A Java/Maven tool-suite for managing, browsing, procedurally generating, and exporting game assets for the **PlaneGuardian** game.

## Technology Stack

| Concern           | Library / Version                         |
|-------------------|-------------------------------------------|
| 3-D Engine        | JMonkeyEngine 3 `3.7.0-stable` (LWJGL 3) |
| Database          | H2 (embedded, `~/.planeguardian/assets`)  |
| ORM / JPA         | Hibernate 6.4 / Jakarta Persistence 3    |
| Boilerplate       | Lombok                                    |
| Logging           | SLF4J 2 + Logback                         |
| JSON export       | Jackson 2 (with JSR-310 module)           |
| Build             | Maven 3, Java 17, fat-JAR via Shade plugin |

## Running

```bash
mvn package -q
java -jar target/planeguardian-assets-1.0.0-SNAPSHOT.jar
```

Or directly during development:

```bash
mvn compile exec:java -Dexec.mainClass=com.planeguardian.assets.Main
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
    Asset.java                – JPA entity (@Entity, Lombok)
    AssetType.java            – enum
  db/
    DatabaseManager.java      – EntityManagerFactory singleton
    AssetRepository.java      – CRUD operations
  tools/
    AssetBrowserTool.java     – Swing asset list + details panel
    AssetViewerApp.java       – JME3 SimpleApplication (3-D viewer)
    AnimControlPanel.java     – Swing animation playback controls
    ProceduralGenTool.java    – placeholder procedural-gen tool
  export/
    ExportManager.java        – copies assets + writes asset_index.json
    AssetIndex.java           – JSON root object
    AssetIndexEntry.java      – per-asset entry

src/main/resources/
  META-INF/persistence.xml    – JPA / Hibernate config
  logback.xml                 – Logback configuration
  assets/                     – JME3 classpath assets (models, textures …)
```

## JME3 Asset Directory Convention

Place classpath assets (shaders, embedded textures, etc.) under
`src/main/resources/assets/`. JME3's default `ClasspathLocator` picks them
up automatically. External asset files are referenced by their absolute path
stored in H2 and loaded via JME3's `FileLocator`.
