package com.getfriedpig.golem.ai.systems;

import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.ai.components.MovementStateComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;

public class GolemNPCSystem extends HolderSystem<EntityStore> {
    private final ComponentType<EntityStore, CommandQueueComponent> commandQueueComponent = CommandQueueComponent.getComponentType();
    private final ComponentType<EntityStore, MovementStateComponent> movementStateComponent = MovementStateComponent.getComponentType();
    private final ComponentType<EntityStore, NetworkId> networkIdComponentType = NetworkId.getComponentType();

    private final Query<EntityStore> query = Query.and(this.commandQueueComponent, this.movementStateComponent, Query.not(this.networkIdComponentType));

    @Override
    public void onEntityAdd(@NotNull Holder<EntityStore> holder, @NotNull AddReason reason, @NotNull Store<EntityStore> store) {
        if (!holder.getArchetype().contains(NetworkId.getComponentType())) {
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        }

    }
    @Override
    public void onEntityRemoved(@NotNull Holder<EntityStore> holder, @NotNull RemoveReason reason, @NotNull Store<EntityStore> store) {}
    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return query;
    }
}