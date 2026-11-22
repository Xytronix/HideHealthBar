# Hide HealthBar

Server-side Hytale mod that hides entity health bars and combat text, separately for players and NPCs.

## Install

Requires Hytale Server 0.8.0-pre.6 or newer. For Hytale 0.7 use the `main` branch.

Put `HideHealthBar-1.1.2-beta.1.jar` in the server's `mods` folder. To build it yourself, run `mvn clean verify` (JDK 25, Maven 3.9+).

## Configuration

`mods/HideHealthBar/config.json`:

```json
{
  "targets": [
    { "target": "players", "hideHealthBar": true, "hideDamage": true },
    { "target": "npcs", "hideHealthBar": true, "hideDamage": true }
  ]
}
```

Only the entity UI is hidden. Health, damage and the player's own HUD are unchanged.

## Commands

`/hhb` is short for `/hidehealthbar`.

- `/hhb status`
- `/hhb toggle <players|npcs> <healthbar|damage|all>`
- `/hhb reload`

Changes apply to entities as they load. Restart to apply them to every entity.
