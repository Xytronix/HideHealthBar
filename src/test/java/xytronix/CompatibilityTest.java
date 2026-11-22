package xytronix;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.common.semver.SemverRange;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;
import xytronix.commands.HideHealthBarCommand;
import xytronix.config.HideHealthBarConfig;

class CompatibilityTest {
    @Test
    void manifestMatchesBuildAndAcceptsPreReleasesFrom080Pre6() throws Exception {
        try (var stream = getClass().getResourceAsStream("/manifest.json")) {
            assertNotNull(stream);
            var manifest = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals(
                    System.getProperty("project.version"),
                    manifest.get("Version").getAsString());
            assertEquals("xytronix", manifest.get("Group").getAsString());
            assertEquals("Hide HealthBar", manifest.get("Name").getAsString());
            assertEquals(HideHealthBar.class.getName(), manifest.get("Main").getAsString());
            var range = SemverRange.fromString(manifest.get("ServerVersion").getAsString());
            assertTrue(range.satisfies(Semver.fromString("0.8.0-pre.6")));
            assertTrue(range.satisfies(Semver.fromString("0.8.0")));
            assertFalse(range.satisfies(Semver.fromString("0.8.0-pre.5")));
            assertFalse(range.satisfies(Semver.fromString("0.7.7")));
        }
    }

    @Test
    void originalCommandNamesAndAliasAreRetained() {
        var command = new HideHealthBarCommand(new HideHealthBarConfig());
        assertEquals("hidehealthbar", command.getName());
        assertTrue(command.getAliases().contains("hhb"));
        assertEquals(
                Set.of("status", "toggle", "reload"), command.getSubCommands().keySet());
    }

    @Test
    void dependenciesMatchActualCoreModuleNames() throws Exception {
        var ui = com.hypixel.hytale.server.core.modules.entityui.EntityUIModule.MANIFEST;
        var entity = com.hypixel.hytale.server.core.modules.entity.EntityModule.MANIFEST;
        try (var builtins = getClass().getResourceAsStream("/manifests.json");
                var own = getClass().getResourceAsStream("/manifest.json")) {
            assertNotNull(builtins);
            assertNotNull(own);
            String npcId = null;
            for (var element : JsonParser.parseReader(new InputStreamReader(builtins, StandardCharsets.UTF_8))
                    .getAsJsonArray()) {
                var entry = element.getAsJsonObject();
                if (entry.has("Main")
                        && entry.get("Main").getAsString().equals("com.hypixel.hytale.server.npc.NPCPlugin")) {
                    npcId = entry.get("Group").getAsString() + ":"
                            + entry.get("Name").getAsString();
                }
            }
            assertNotNull(npcId);
            var dependencies = JsonParser.parseReader(new InputStreamReader(own, StandardCharsets.UTF_8))
                    .getAsJsonObject()
                    .getAsJsonObject("Dependencies");
            assertEquals(
                    Set.of(ui.getGroup() + ":" + ui.getName(), entity.getGroup() + ":" + entity.getName(), npcId),
                    dependencies.keySet());
        }
    }
}
