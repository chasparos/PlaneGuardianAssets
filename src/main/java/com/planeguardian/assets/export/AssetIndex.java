package com.planeguardian.assets.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Root object written to {@code asset_index.json}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetIndex {
    private String version;
    private LocalDateTime exportDate;
    private int totalAssets;
    private List<AssetIndexEntry> assets;
}
