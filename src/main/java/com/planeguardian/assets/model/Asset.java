package com.planeguardian.assets.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Persistent record representing a single asset in the library.
 * The actual binary file is stored on disk; only the path (and metadata)
 * are kept in H2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name shown in the browser. */
    @Column(nullable = false)
    private String name;

    /** Absolute path to the asset file on disk (glb, j3o, png, …). */
    @Column
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column
    private AssetType assetType;

    /** When true, this asset is included in the next Export Library run. */
    @Builder.Default
    @Column(nullable = false)
    private boolean includeInExport = true;

    /** Optional free-form JSON for tags, LOD hints, etc. */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Used by JList cell renderer. */
    @Override
    public String toString() {
        return name != null ? name : "(Unnamed)";
    }
}
