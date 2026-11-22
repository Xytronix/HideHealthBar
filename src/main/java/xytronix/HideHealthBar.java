package xytronix;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import xytronix.commands.HideHealthBarCommand;
import xytronix.config.HideHealthBarConfig;
import xytronix.systems.HideEntityUiSystem;

public class HideHealthBar extends JavaPlugin {
    private static final Logger LOGGER = Logger.getLogger(HideHealthBar.class.getName());
    private final HideHealthBarConfig config = new HideHealthBarConfig();

    public HideHealthBar(@Nonnull JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
        Path serverRoot = Paths.get(".", new String[0]).toAbsolutePath().normalize();
        this.config.initialize(serverRoot);
        this.getCommandRegistry().registerCommand(new HideHealthBarCommand(this.config));
        HideHealthBarConfig.TargetSettings players = this.config.getPlayerSettings();
        HideHealthBarConfig.TargetSettings npcs = this.config.getNpcSettings();
        boolean playersEnabled = players.hasAnyHide();
        boolean npcsEnabled = npcs.hasAnyHide();
        this.getEntityStoreRegistry().registerSystem(new HideEntityUiSystem(this.config));
        if (!playersEnabled && !npcsEnabled) {
            LOGGER.info("HideHealthBar: disabled (no hides enabled). Use /hidehealthbar toggle to configure.");
            return;
        }
        LOGGER.info("HideHealthBar: enabled (players: hideHealthBar=" + players.isHideHealthBar() + ", hideDamage="
                + players.isHideDamage() + "; npcs: hideHealthBar=" + npcs.isHideHealthBar() + ", hideDamage="
                + npcs.isHideDamage() + ").");
    }
}
