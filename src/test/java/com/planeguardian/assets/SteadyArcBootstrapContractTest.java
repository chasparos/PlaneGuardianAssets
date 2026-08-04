package com.planeguardian.assets;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class SteadyArcBootstrapContractTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void continuityAndRepositoryLocalLaunchersAreInstalled() {
        for (String path : new String[]{
                ".steadyarc/roadmap.md", ".steadyarc/engineering-notes.md",
                ".steadyarc/deferred-issues.md", ".steadyarc/handoff.md",
                "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties",
                "PatchSequence.ps1", "PatchSequence.sh",
                "PublishValidationArtifacts.ps1", "PublishValidationArtifacts.sh",
                "NewPatch.ps1", "RunWidget.ps1",
                "InvokeSteadyArcRelay.ps1"}) {
            assertTrue(Files.isRegularFile(ROOT.resolve(path)), path + " should be installed");
        }
    }

    @Test
    void widgetUsesWrapperAndRelayHasNoFreeFormOperation() throws Exception {
        String widget = Files.readString(ROOT.resolve("RunWidget.ps1"));
        String relay = Files.readString(ROOT.resolve("InvokeSteadyArcRelay.ps1"));
        assertTrue(widget.contains("mvnw.cmd"));
        assertTrue(relay.contains("ValidateSet(\"git-status\", \"git-diff-check\", \"maven-test\")"));
    }
}
