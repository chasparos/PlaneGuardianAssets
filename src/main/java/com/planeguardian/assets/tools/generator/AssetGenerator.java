package com.planeguardian.assets.tools.generator;

import javax.swing.JPanel;
import java.nio.file.Path;

/**
 * Contract for all procedural / parametric asset generators.
 *
 * <p>Each implementation provides a Swing parameter panel that the
 * {@link AssetGeneratorTool} embeds, and a {@link #generate} method that
 * runs the actual generation (may be slow – the tool calls it from a
 * background thread).</p>
 */
public interface AssetGenerator {

    /** Short display name shown in the generator list (e.g. "Deciduous Tree"). */
    String getName();

    /**
     * Builds the Swing panel containing all tunable parameters.
     * Called once on the EDT; the returned panel is cached for the lifetime
     * of the tool window.
     */
    JPanel buildParameterPanel();

    /**
     * Runs the generation algorithm and writes output file(s) to
     * {@code outputDirectory}.
     *
     * <p>This method is invoked on a background thread — implementations
     * must <em>not</em> touch Swing components directly.</p>
     *
     * @param outputDirectory directory where the generator should write files
     * @return result describing success/failure and the path of the primary output
     */
    GenerationResult generate(Path outputDirectory);
}
