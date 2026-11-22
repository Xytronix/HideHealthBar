package xytronix.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class HideHealthBarConfig {
    private static final Logger LOGGER = Logger.getLogger(HideHealthBarConfig.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private List<TargetSettings> targets =
            new ArrayList<TargetSettings>(Arrays.asList(TargetSettings.players(), TargetSettings.npcs()));
    private transient Path configDir;
    private transient Path configPath;

    public void initialize(Path rootPath) {
        this.configDir = rootPath.resolve("mods").resolve("HideHealthBar");
        this.configPath = this.configDir.resolve("config.json");
        try {
            if (!Files.exists(this.configDir, new LinkOption[0])) {
                Files.createDirectories(this.configDir, new FileAttribute[0]);
            }
            if (Files.exists(this.configPath, new LinkOption[0])) {
                this.load();
            } else {
                this.save();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize config: " + e.getMessage());
        }
    }

    public void load() {
        try (BufferedReader reader = Files.newBufferedReader(this.configPath); ) {
            JsonElement element = JsonParser.parseReader((Reader) reader);
            if (!element.isJsonObject()) {
                return;
            }
            JsonObject obj = element.getAsJsonObject();
            boolean migrated = false;
            if (obj.has("targets")) {
                HideHealthBarConfig loaded =
                        (HideHealthBarConfig) GSON.fromJson((JsonElement) obj, HideHealthBarConfig.class);
                if (loaded != null && loaded.targets != null) {
                    this.targets = loaded.targets;
                }
            } else {
                boolean hidePlayers = HideHealthBarConfig.getBool(obj, "hidePlayers", true);
                boolean hideNpcs = HideHealthBarConfig.getBool(obj, "hideNpcs", true);
                boolean hideHealthBar = HideHealthBarConfig.getBool(obj, "hideHealthBar", true);
                boolean hideDamage = obj.has("hideDamage")
                        ? HideHealthBarConfig.getBool(obj, "hideDamage", true)
                        : HideHealthBarConfig.getBool(obj, "hideCombatText", true);
                this.targets =
                        new ArrayList<TargetSettings>(Arrays.asList(TargetSettings.players(), TargetSettings.npcs()));
                TargetSettings players = this.getPlayerSettings();
                TargetSettings npcs = this.getNpcSettings();
                players.hideHealthBar = hidePlayers && hideHealthBar;
                players.hideDamage = hidePlayers && hideDamage;
                npcs.hideHealthBar = hideNpcs && hideHealthBar;
                npcs.hideDamage = hideNpcs && hideDamage;
                migrated = true;
            }
            this.ensureTargets();
            if (migrated) {
                this.save();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(this.configPath, new OpenOption[0]); ) {
            GSON.toJson((Object) this, (Appendable) writer);
        } catch (Exception e) {
            LOGGER.severe("Failed to save config: " + e.getMessage());
        }
    }

    public TargetSettings getPlayerSettings() {
        return this.getSettings("players");
    }

    public TargetSettings getNpcSettings() {
        return this.getSettings("npcs");
    }

    private TargetSettings getSettings(String target) {
        this.ensureTargets();
        for (TargetSettings settings : this.targets) {
            if (settings == null
                    || settings.target == null
                    || !settings.target.toLowerCase(Locale.ROOT).equals(target)) continue;
            return settings;
        }
        TargetSettings fallback = "players".equals(target) ? TargetSettings.players() : TargetSettings.npcs();
        this.targets.add(fallback);
        return fallback;
    }

    private void ensureTargets() {
        if (this.targets == null) {
            this.targets = new ArrayList<TargetSettings>();
        }
        boolean hasPlayers = false;
        boolean hasNpcs = false;
        for (TargetSettings settings : this.targets) {
            if (settings == null || settings.target == null) continue;
            String name = settings.target.toLowerCase(Locale.ROOT);
            if ("players".equals(name)) {
                hasPlayers = true;
                continue;
            }
            if (!"npcs".equals(name)) continue;
            hasNpcs = true;
        }
        if (!hasPlayers) {
            this.targets.add(TargetSettings.players());
        }
        if (!hasNpcs) {
            this.targets.add(TargetSettings.npcs());
        }
    }

    private static boolean getBool(JsonObject obj, String key, boolean fallback) {
        if (obj.has(key) && obj.get(key).isJsonPrimitive()) {
            try {
                return obj.get(key).getAsBoolean();
            } catch (Exception e) {
                return fallback;
            }
        }
        return fallback;
    }

    public static class TargetSettings {
        private String target;
        private boolean hideHealthBar = true;
        private boolean hideDamage = true;

        public TargetSettings() {}

        private TargetSettings(String target, boolean hideHealthBar, boolean hideDamage) {
            this.target = target;
            this.hideHealthBar = hideHealthBar;
            this.hideDamage = hideDamage;
        }

        public static TargetSettings players() {
            return new TargetSettings("players", true, true);
        }

        public static TargetSettings npcs() {
            return new TargetSettings("npcs", true, true);
        }

        public String getTarget() {
            return this.target;
        }

        public boolean isHideHealthBar() {
            return this.hideHealthBar;
        }

        public void setHideHealthBar(boolean hideHealthBar) {
            this.hideHealthBar = hideHealthBar;
        }

        public boolean isHideDamage() {
            return this.hideDamage;
        }

        public void setHideDamage(boolean hideDamage) {
            this.hideDamage = hideDamage;
        }

        public boolean hasAnyHide() {
            return this.hideHealthBar || this.hideDamage;
        }
    }
}
