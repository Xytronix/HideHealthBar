package xytronix.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entityui.UIComponentList;
import com.hypixel.hytale.server.core.modules.entityui.asset.EntityUIComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.NPCComponent;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import xytronix.config.HideHealthBarConfig;

public class HideEntityUiSystem extends RefSystem<EntityStore> {
    @Nonnull
    private final ComponentType<EntityStore, UIComponentList> uiComponentListType;

    @Nonnull
    private final Query<EntityStore> query;

    @Nonnull
    private final ComponentType<EntityStore, Player> playerComponentType;

    @Nonnull
    private final ComponentType<EntityStore, NPCComponent> npcComponentType;

    @Nonnull
    private final HideHealthBarConfig config;

    public HideEntityUiSystem(@Nonnull HideHealthBarConfig config) {
        this.config = config;
        this.uiComponentListType = UIComponentList.getComponentType();
        this.playerComponentType = Player.getComponentType();
        this.npcComponentType = NPCComponent.getComponentType();
        this.query = Query.and(this.uiComponentListType, Query.or(this.playerComponentType, this.npcComponentType));
    }

    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }

    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        boolean player = store.getComponent(ref, this.playerComponentType) != null;
        HideHealthBarConfig.TargetSettings settings = null;
        if (player) {
            settings = this.config.getPlayerSettings();
        } else if (store.getComponent(ref, this.npcComponentType) != null) {
            settings = this.config.getNpcSettings();
        }
        UIComponentList list = (UIComponentList) store.getComponent(ref, this.uiComponentListType);
        if (settings == null || list == null || !player && !settings.hasAnyHide()) {
            return;
        }
        UIComponentList base = player ? new UIComponentList((String[]) null) : list;
        var assets = EntityUIComponent.getAssetMap();
        int[] ids = UiComponentFilter.filter(
                base.getComponentIds(),
                settings.isHideHealthBar(),
                settings.isHideDamage(),
                DefaultEntityStatTypes.getHealth(),
                id -> {
                    var asset = assets.getAsset(id);
                    return asset == null ? null : asset.toPacket();
                });
        if (Arrays.equals(ids, list.getComponentIds())) {
            return;
        }
        if (Arrays.equals(ids, base.getComponentIds())) {
            commandBuffer.replaceComponent(ref, this.uiComponentListType, base);
            return;
        }
        String[] components = Arrays.stream(ids)
                .mapToObj(assets::getAsset)
                .filter(Objects::nonNull)
                .map(EntityUIComponent::getId)
                .toArray(String[]::new);
        commandBuffer.replaceComponent(ref, this.uiComponentListType, new UIComponentList(components));
    }

    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {}
}
