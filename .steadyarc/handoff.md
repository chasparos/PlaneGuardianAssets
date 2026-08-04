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

## Corrective follow-ups

- **2026-08-04 — Item-7 hollow correction:** Review established that the current
  `tree.hollow` is only a disconnected interior tube. It does not yet deform or
  omit the trunk aperture, connect an irregular rim to the recessed interior, or
  expose deterministic configurable presence. Consequently, item 7 is not fully
  closed. A follow-up must add a shared wall-recess operation, hollow
  settings/schema, and revised deterministic tests before the hollow claim may
  be restored. This is recorded separately from routine return reports so a
  later continuation cannot mistake a superseded completion statement for the
  current boundary.

## Return report

- **Returned:** 2026-08-04 — validation confirmed
- **Work completed:** Deterministic PNG encoding, exact SHA-256 artifact metadata,
  and a local atomic generated-resource cache with metadata/content verification.
  Bark and foliage cover generate → encode → store → retrieve.
- **Verification:** The published compatible-JDK validation manifest identifies
  source revision `62fe6e2bb170c319ff287a36a7cd721a81549eda` and records
  `./mvnw test` success: 116 tests run, with zero failures, errors, or skips.
- **Repository changes:** Added texture codec and cache contracts/implementation,
  focused PNG/cache tests, and generated-resource architecture updates.
- **Durable notes added or changed:** `.steadyarc/engineering-notes.md`,
  `.steadyarc/roadmap.md`, and `docs/architecture/generation-platform.md`.
- **Deferred issues added or changed:** None.
- **Unresolved issues:** None.
- **Recommended next action:** Validate the bounded storage flow through the
  Steady Arc relay, then continue the item-7 structural composition proof.
- **Ownership after return:** Returned to Omen.

## Follow-on implementation report

- **Work completed:** Added bounded version-one structural composition controls,
  independent named branch/root spline-tube components, stable roles/sockets,
  root contact metadata, aggregate fingerprints, and focused invariants.
- **Verification:** Local Maven validation requires a newer JDK: the available
  Java runtime cannot compile the pre-existing `Thread.ofPlatform()` use in
  `devtools/SupportRelay`. The structural sources compiled before that unrelated
  failure; rerun `./mvnw test` through the compatible-JDK relay.
- **Follow-on update:** Branch levels now recurse through stable parent paths
  rather than being independently attached to the trunk. Path-scoped streams,
  stable component IDs, and deterministic traversal preserve admitted geometry
  when the declared component budget truncates later children.
- **Verification update:** `./mvnw test` passed locally after the recursive
  composition change: 121 tests run with zero failures, errors, or skips.

## Item-7 return report

- **Returned:** 2026-08-04 — structural composition complete.
- **Work completed:** Added a bounded budget-suffix hollow surface and canonical
  engine-neutral render meshes for every structural part. Render products are
  derived through shared triangulation and surface processing; no renderer type
  entered the tree generator.
- **Verification:** `./mvnw test` passed: 122 tests run with zero failures,
  errors, or skips. Focused invariants pin the default aggregate fingerprint,
  validate UV/tangent render products, preserve root-contact and socket metadata,
  and prove the hollow cannot displace admitted components under a budget.
- **Unresolved item-7 scope:** Boolean hollow subtraction, watertight fusion,
  and engine adapters remain deliberately deferred or belong to later roadmap
  items.
- **Recommended next action:** Begin roadmap item 8 with shared crown/foliage
  composition and semantic golden cases.

## Item-8 feature-suitability return report

- **Returned:** 2026-08-04 — semantic feature suitability complete.
- **Work completed:** Extended the bounded tree semantic adapter with independent
  vine and fungal suitability alongside moss, flower, and fruit. Each feature
  surface has a stable contribution target and remains bounded to `[0, 1]`.
- **Verification:** `./mvnw -Dtest=TreeSemanticAdapterTest test` passed: 3 tests
  run with zero failures, errors, or skips.
- **Unresolved item-8 scope:** Feature geometry, placement budgets, and renderer
  adapters remain separate consumers of resolved semantic surfaces.

## Item-8 feature-admission return report

- **Returned:** 2026-08-04 — bounded feature-anchor admission complete.
- **Work completed:** Added version-one per-group and total feature budgets plus
  deterministic, stable-ID engine-neutral moss, vine, flower, fruit, and fungal
  placement anchors derived from resolved suitability.
- **Verification:** `./mvnw -Dtest=DeciduousTreeFeatureGeneratorTest,TreeParameterSchemaTest test`
  passed: 4 tests run with zero failures, errors, or skips.
- **Unresolved item-8 scope:** Feature geometry and renderer adapters remain
  separate consumers of stable semantic admissions.

## Item-8 feature-geometry return report

- **Returned:** 2026-08-04 — bounded feature geometry complete.
- **Work completed:** Added deterministic, stable-ID engine-neutral mesh products
  for every admitted moss, vine, flower, fruit, and fungal anchor. The consumer
  composes the shared UV-mapped foliage shell primitive with role-specific bounded
  proportions and does not redefine semantic suitability or admission budgets.
- **Verification:** `./mvnw -Dtest=DeciduousTreeFeatureGeneratorTest,DeciduousTreeFeatureGeometryGeneratorTest test`
  passed: 4 tests run with zero failures, errors, or skips.
- **Unresolved item-8 scope:** Renderer-specific feature adapters remain deferred
  to item 9.

## Item-8 closure report

- **Returned:** 2026-08-04 — item 8 complete.
- **Work completed:** The shared UV-mapped foliage-shell primitive, deterministic
  crown composition, bounded semantic suitability and contribution traces, semantic
  golden cases, per-group and aggregate feature admission, and deterministic
  engine-neutral geometry products now cover the full item-8 scope.
- **Verification:** Focused semantic, feature-admission, and feature-geometry
  Maven tests passed with zero failures; the preceding full Maven validation
  recorded 122 tests with zero failures, errors, or skips.
- **Remaining before item 9:** Nothing from item 8. Renderer-specific material,
  VFX, motion, and preview adapters are explicitly item-9 work; item 7's
  independently tracked hollow correction remains open.
- **Recommended next action:** Begin roadmap item 9 without reopening item-8
  semantic, admission, or engine-neutral geometry contracts.

## Item-9 foundation report

- **Work completed:** Added version-two direct presentation controls, an
  AI-readable schema manifest and guarded proposal envelope, deterministic
  renderer-independent wind response, a fixed gameplay preview fixture, and
  jME-boundary PBR/pollen adapters.
- **Verification:** Focused Maven contract tests passed: 5 tests, zero failures,
  errors, or skips.
- **Remaining scope:** Texture/mask binding, shader wind application, rendered
  fixture/shadow review, and the semantic-wheel desktop editor are still open
  before item 9 can close.
