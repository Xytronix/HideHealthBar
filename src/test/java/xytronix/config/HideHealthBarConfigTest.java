package xytronix.config;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HideHealthBarConfigTest {
    @TempDir
    Path root;

    private Path writeConfig(String json) throws Exception {
        Path file = root.resolve("mods/HideHealthBar/config.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, json);
        return file;
    }

    @Test
    void defaultsKeepExistingLocationAndHideBothTargets() throws Exception {
        var config = new HideHealthBarConfig();
        config.initialize(root);
        assertTrue(Files.exists(root.resolve("mods/HideHealthBar/config.json")));
        assertTrue(config.getPlayerSettings().isHideHealthBar());
        assertTrue(config.getPlayerSettings().isHideDamage());
        assertTrue(config.getNpcSettings().isHideHealthBar());
        assertTrue(config.getNpcSettings().isHideDamage());
    }

    @Test
    void targetsRemainIndependentAcrossSaveAndReload() {
        var config = new HideHealthBarConfig();
        config.initialize(root);
        config.getPlayerSettings().setHideHealthBar(false);
        config.getNpcSettings().setHideDamage(false);
        config.save();
        var restored = new HideHealthBarConfig();
        restored.initialize(root);
        assertFalse(restored.getPlayerSettings().isHideHealthBar());
        assertTrue(restored.getPlayerSettings().isHideDamage());
        assertTrue(restored.getNpcSettings().isHideHealthBar());
        assertFalse(restored.getNpcSettings().isHideDamage());
    }

    @Test
    void legacyCombatTextConfigMigratesWithoutLosingFlags() throws Exception {
        Path file = writeConfig("""
{"hidePlayers":false,"hideNpcs":true,"hideHealthBar":false,"hideCombatText":true}
""");
        var config = new HideHealthBarConfig();
        config.initialize(root);
        assertFalse(config.getPlayerSettings().hasAnyHide());
        assertFalse(config.getNpcSettings().isHideHealthBar());
        assertTrue(config.getNpcSettings().isHideDamage());
        var saved = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertTrue(saved.has("targets"));
        assertFalse(saved.has("hideCombatText"));
    }

    @Test
    void missingTargetGetsDefaultsWithoutOverwritingOtherTarget() throws Exception {
        writeConfig("""
{"targets":[{"target":"PLAYERS","hideHealthBar":false,"hideDamage":false},null]}
""");
        var config = new HideHealthBarConfig();
        config.initialize(root);
        assertFalse(config.getPlayerSettings().hasAnyHide());
        assertTrue(config.getNpcSettings().isHideHealthBar());
        assertTrue(config.getNpcSettings().isHideDamage());
    }

    @Test
    void reloadReadsChangedSettingsFromDisk() throws Exception {
        var config = new HideHealthBarConfig();
        config.initialize(root);
        writeConfig("""
{"targets":[{"target":"npcs","hideHealthBar":false,"hideDamage":false}]}
""");
        config.load();
        assertFalse(config.getNpcSettings().hasAnyHide());
        assertTrue(config.getPlayerSettings().hasAnyHide());
    }
}
