package xytronix.systems;

import com.hypixel.hytale.protocol.EntityUIComponent;
import com.hypixel.hytale.protocol.EntityUIType;
import java.util.Arrays;
import java.util.function.IntFunction;

final class UiComponentFilter {
    private UiComponentFilter() {}

    static int[] filter(
            int[] ids,
            boolean hideHealthBar,
            boolean hideDamage,
            int healthIndex,
            IntFunction<EntityUIComponent> components) {
        if (ids == null) return new int[0];
        return Arrays.stream(ids)
                .filter(id -> {
                    EntityUIComponent ui = id < 0 ? null : components.apply(id);
                    if (ui == null || ui.unknown) return true;
                    if (hideDamage && ui.type == EntityUIType.CombatText) return false;
                    return !(hideHealthBar
                            && healthIndex >= 0
                            && ui.type == EntityUIType.EntityStat
                            && ui.entityStatIndex == healthIndex);
                })
                .toArray();
    }
}
