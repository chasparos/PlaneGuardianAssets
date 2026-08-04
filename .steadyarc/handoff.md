# Steady Arc Handoff

## State

- **Status:** Active — implementation
- **Handoff ID:** PGA-2026-08-04-github-resource-storage
- **From:** Omen
- **To:** GitHub task agent
- **Created:** 2026-08-04
- **Return owner:** Omen
- **Return condition:** PNG preview/export and generated-resource cache persistence complete the bounded storage flow for roadmap item 6, with tests and durable documentation.

## Engineering context

- **Current arc:** First Asset Generator POC
- **Active roadmap item:** 6. Reusable generated-resource systems
- **Authoritative revision or snapshot:** The commit containing this handoff; Omen will push it to GitHub.
- **Build/test state:** 110 tests passed through the Steady Arc widget relay immediately before handoff preparation.
- **Relevant durable notes:** `docs/architecture/generation-platform.md`, `.steadyarc/engineering-notes.md`
- **Geometry operator guide:** `docs/guides/geometry-toolkit.md`

## Entry sequence for the receiving agent

1. Read `AGENTS.md`, this handoff, and roadmap item 6.
2. Read the generated-texture, artifact, material, and VFX sections of
   `docs/architecture/generation-platform.md`.
3. Inspect `generation/resources`, its tests, and the two `META-INF/services`
   registrations before designing new contracts.
4. Use `./mvnw test` (or the repository's supported Maven validation surface)
   and preserve the 110-test baseline.
5. Treat the large jME `BinaryImporter`/missing `AssetManager` stack trace in
   `DeciduousTreeGeneratorTest` as known logging noise when the test still passes.

## Delegation

- **Requested action:** Continue roadmap item 6 with a PNG preview/export codec
  boundary and generated-resource persistence/cache flow.
- **Completion criteria:**
  - Encode supported `TexturePixels` formats to deterministic valid PNG bytes
    without introducing jME dependencies into core resource contracts.
  - Produce `ResourceArtifact` metadata whose byte length and SHA-256 content
    fingerprint exactly match the encoded payload.
  - Provide a cache/persistence abstraction keyed by the already validated
    generated-resource fingerprint; identical identity must deduplicate.
  - Provide one safe local implementation suitable for the desktop tool. Writes
    must be atomic or otherwise unable to expose partial cache entries.
  - Cache lookup must verify metadata/content identity and treat corruption as a
    miss or explicit diagnostic, never as a valid hit.
  - Bark and foliage providers must pass an end-to-end generate → encode → store
    → retrieve test without gaining persistence logic themselves.
  - Update roadmap, architecture, engineering notes, and the return report with
    validation evidence.
- **Constraints:**
  - Preserve stable IDs, provider/schema versions, canonical fingerprints,
    color-space/channel semantics, and fingerprint-pinned resource references.
  - Keep codecs and storage behind interfaces; providers generate descriptors
    and pixels but do not write files or database rows.
  - Use forward-slash relative artifact paths and the existing path-safety rules.
  - Do not add JPA or Jackson; persistence is direct JDBC where relational
    metadata is appropriate, and JSON uses Gson.
  - Do not introduce asset-family-private texture, material, or VFX systems.
  - Do not implement the deferred compute-shader particle system in this pass.
  - Dynamic third-party provider loading remains out of scope; current
    ServiceLoader providers are trusted compiled classpath code.
- **Open questions the receiver may resolve within scope:**
  - Whether PNG encoding lives behind a general `TextureEncoder` SPI or a
    narrower first `PngTextureEncoder`, provided the artifact contract remains
    codec-neutral.
  - Whether the first safe cache implementation is content-addressed filesystem
    storage with metadata, direct-JDBC metadata plus files, or another small
    local design. Prefer the least coupled design that proves deduplication,
    atomicity, and corruption handling.
- **Files or areas in scope:** `generation/resources/**`, focused database/cache
  adapters if justified, tests, service metadata, architecture and Steady Arc
  project memory.
- **Files or areas explicitly out of scope:** Great Tree structural recursion,
  final texture art direction, jME material/particle rendering, compute shaders,
  remote object storage, downloadable executable bundles, and UI redesign.
- **Expected documentation updates:** Architecture, roadmap, engineering notes,
  deferred issues only for unrelated discoveries, and this handoff return report.

## Current implementation landmarks

- `GeneratedTexture` validates cache identity from provider/schema versions,
  request ID, dimensions, seed, sorted parameters, and fingerprinted sources.
- `GeneratedTextureProduct` pairs the descriptor with immutable validated pixels.
- `ResourceArtifact` is codec/storage neutral and validates portable relative paths.
- `MaterialRecipe` texture values pin both resource ref and generation fingerprint.
- `VfxConfiguration` does the same for arbitrary referenced resources and stable
  socket attachments.
- `PainterlyBarkTextureProvider` emits deterministic sRGB RGBA8.
- `FoliageMaskTextureProvider` emits deterministic linear R8 coverage.
- `PollenMotesVfxProvider` is the initial reusable effect configuration provider.
- Texture and VFX registries reject duplicate stable provider IDs.
- Provider algorithms are reference implementations and may later be replaced
  centrally behind version changes; asset generators should reuse them now.

## Known deferred boundaries

Read `.steadyarc/deferred-issues.md`. In particular, compute-shader particles,
lossless Blender/quad authoring interchange, arbitrary remeshing/resampling,
adaptive patch stitching, richer mesh junctions, and final performance budgets
are deliberately inactive. Do not pull them into this storage pass.

## Activity amendments

No amendments.

## Return report

- **Returned:** Pending compatible-JDK validation
- **Work completed:** Deterministic PNG encoding, exact SHA-256 artifact metadata,
  and a local atomic generated-resource cache with metadata/content verification.
  Bark and foliage cover generate → encode → store → retrieve.
- **Verification:** Focused Maven execution was blocked before tests by the
  pre-existing Java 17 compilation mismatch in `devtools/SupportRelay`:
  `Thread.ofPlatform()` is unavailable to the configured compiler. The supplied
  manifest and test log were not present in this checkout when inspected.
- **Repository changes:** Added texture codec and cache contracts/implementation,
  focused PNG/cache tests, and generated-resource architecture updates.
- **Durable notes added or changed:** `.steadyarc/engineering-notes.md`,
  `.steadyarc/roadmap.md`, and `docs/architecture/generation-platform.md`.
- **Deferred issues added or changed:** None.
- **Unresolved issues:** Re-run the supported relay validation using its compatible
  JDK and provide the resulting manifest/log on this branch.
- **Recommended next action:** Validate the bounded storage flow through the
  Steady Arc relay, then begin material/VFX engine adapters only when item 7 needs them.
- **Ownership after return:** Pending validation; return to Omen after the relay confirms the test baseline.
