package xytronix.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.util.Locale;
import javax.annotation.Nonnull;
import xytronix.config.HideHealthBarConfig;

public class HideHealthBarCommand extends AbstractCommandCollection {
    public HideHealthBarCommand(@Nonnull HideHealthBarConfig config) {
        super("hidehealthbar", "Manage HideHealthBar plugin settings.");
        this.addAliases(new String[] {"hhb"});
        this.addSubCommand(new StatusCommand(config));
        this.addSubCommand(new ToggleCommand(config));
        this.addSubCommand(new ReloadCommand(config));
    }

    private static class StatusCommand extends CommandBase {
        private final HideHealthBarConfig config;

        public StatusCommand(@Nonnull HideHealthBarConfig config) {
            super("status", "Show current HideHealthBar settings.");
            this.config = config;
        }

        protected void executeSync(@Nonnull CommandContext context) {
            HideHealthBarConfig.TargetSettings players = this.config.getPlayerSettings();
            HideHealthBarConfig.TargetSettings npcs = this.config.getNpcSettings();
            context.sendMessage(Message.raw((String) "[HideHealthBar] Current settings:"));
            context.sendMessage(
                    Message.raw((String) ("  Players: healthBar=" + (players.isHideHealthBar() ? "hidden" : "visible")
                            + ", damage=" + (players.isHideDamage() ? "hidden" : "visible"))));
            context.sendMessage(
                    Message.raw((String) ("  NPCs: healthBar=" + (npcs.isHideHealthBar() ? "hidden" : "visible")
                            + ", damage=" + (npcs.isHideDamage() ? "hidden" : "visible"))));
        }
    }

    private static class ToggleCommand extends CommandBase {
        private final HideHealthBarConfig config;

        @Nonnull
        private final RequiredArg<String> targetArg;

        @Nonnull
        private final RequiredArg<String> typeArg;

        public ToggleCommand(@Nonnull HideHealthBarConfig config) {
            super("toggle", "Toggle a hide setting for a target.");
            this.config = config;
            this.targetArg = this.withRequiredArg("target", "Target: players or npcs", ArgTypes.STRING);
            this.typeArg = this.withRequiredArg("type", "Type: healthbar, damage, or all", ArgTypes.STRING);
        }

        protected void executeSync(@Nonnull CommandContext context) {
            HideHealthBarConfig.TargetSettings settings;
            String target = ((String) this.targetArg.get(context)).toLowerCase(Locale.ROOT);
            String type = ((String) this.typeArg.get(context)).toLowerCase(Locale.ROOT);
            if ("players".equals(target) || "player".equals(target)) {
                settings = this.config.getPlayerSettings();
            } else if ("npcs".equals(target) || "npc".equals(target)) {
                settings = this.config.getNpcSettings();
            } else {
                context.sendMessage(Message.raw(
                        (String) ("[HideHealthBar] Unknown target: " + target + ". Use 'players' or 'npcs'.")));
                return;
            }
            switch (type) {
                case "healthbar":
                case "health": {
                    settings.setHideHealthBar(!settings.isHideHealthBar());
                    this.config.save();
                    context.sendMessage(Message.raw((String) ("[HideHealthBar] " + target + " healthBar is now "
                            + (settings.isHideHealthBar() ? "hidden" : "visible")
                            + ". Requires server restart to take effect.")));
                    break;
                }
                case "damage":
                case "combattext": {
                    settings.setHideDamage(!settings.isHideDamage());
                    this.config.save();
                    context.sendMessage(Message.raw((String) ("[HideHealthBar] " + target + " damage text is now "
                            + (settings.isHideDamage() ? "hidden" : "visible")
                            + ". Requires server restart to take effect.")));
                    break;
                }
                case "all": {
                    boolean newState = !settings.isHideHealthBar() || !settings.isHideDamage();
                    settings.setHideHealthBar(newState);
                    settings.setHideDamage(newState);
                    this.config.save();
                    context.sendMessage(Message.raw((String) ("[HideHealthBar] " + target + " all UI is now "
                            + (newState ? "hidden" : "visible") + ". Requires server restart to take effect.")));
                    break;
                }
                default: {
                    context.sendMessage(Message.raw((String)
                            ("[HideHealthBar] Unknown type: " + type + ". Use 'healthbar', 'damage', or 'all'.")));
                }
            }
        }
    }

    private static class ReloadCommand extends CommandBase {
        private final HideHealthBarConfig config;

        public ReloadCommand(@Nonnull HideHealthBarConfig config) {
            super("reload", "Reload config from disk.");
            this.config = config;
        }

        protected void executeSync(@Nonnull CommandContext context) {
            this.config.load();
            context.sendMessage(
                    Message.raw((String) "[HideHealthBar] Config reloaded. Changes take effect on next entity spawn."));
        }
    }
}
