package xytronix.systems;

import static org.junit.jupiter.api.Assertions.*;

import com.hypixel.hytale.protocol.EntityUIComponent;
import com.hypixel.hytale.protocol.EntityUIType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UiComponentFilterTest {
    private static final int HEALTH = 7;

    static EntityUIComponent packet(EntityUIType type, int stat) {
        var result = new EntityUIComponent();
        result.type = type;
        result.entityStatIndex = stat;
        return result;
    }

    static Map<Integer, EntityUIComponent> packets() {
        var result = new HashMap<Integer, EntityUIComponent>();
        result.put(0, packet(EntityUIType.EntityStat, HEALTH));
        result.put(1, packet(EntityUIType.CombatText, 0));
        result.put(2, packet(EntityUIType.EntityStat, 8));
        return result;
    }

    @Test
    void hidesOnlyHealthBarWhenRequested() {
        assertArrayEquals(
                new int[] {1, 2}, UiComponentFilter.filter(new int[] {0, 1, 2}, true, false, HEALTH, packets()::get));
    }

    @Test
    void hidesOnlyDamageWhenRequested() {
        assertArrayEquals(
                new int[] {0, 2}, UiComponentFilter.filter(new int[] {0, 1, 2}, false, true, HEALTH, packets()::get));
    }

    @Test
    void hidingBothPreservesUnrelatedStatDisplays() {
        assertArrayEquals(
                new int[] {2}, UiComponentFilter.filter(new int[] {0, 1, 2}, true, true, HEALTH, packets()::get));
    }

    @Test
    void doesNotAddComponentsTheEntityNeverHad() {
        assertArrayEquals(new int[] {2}, UiComponentFilter.filter(new int[] {2}, true, false, HEALTH, packets()::get));
    }

    @Test
    void preservesOrderDuplicatesUnknownIdsAndInput() {
        int[] ids = {2, 1, 42, 0, 2};
        assertArrayEquals(new int[] {2, 42, 0, 2}, UiComponentFilter.filter(ids, false, true, HEALTH, packets()::get));
        assertArrayEquals(new int[] {2, 1, 42, 0, 2}, ids);
    }

    @Test
    void disabledHidingKeepsTheOriginalSelection() {
        int[] ids = {1, 0, 2};
        assertArrayEquals(ids, UiComponentFilter.filter(ids, false, false, HEALTH, packets()::get));
    }

    @Test
    void emptyOrUninitializedListsDoNotCrash() {
        assertArrayEquals(new int[0], UiComponentFilter.filter(new int[0], true, true, HEALTH, packets()::get));
        assertArrayEquals(new int[0], UiComponentFilter.filter(null, true, true, HEALTH, packets()::get));
    }

    @Test
    void unknownAssetsAreNotMistakenForHealthBars() {
        var packets = packets();
        var unknown = packet(EntityUIType.EntityStat, HEALTH);
        unknown.unknown = true;
        packets.put(3, unknown);
        assertArrayEquals(new int[] {3}, UiComponentFilter.filter(new int[] {3}, true, true, HEALTH, packets::get));
    }
}
