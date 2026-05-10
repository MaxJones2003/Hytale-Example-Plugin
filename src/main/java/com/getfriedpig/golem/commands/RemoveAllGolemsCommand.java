package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.player.components.PlayerGolemsData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.*;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RemoveAllGolemsCommand extends AbstractPlayerCommand {
    public RemoveAllGolemsCommand() {
        super("removeg", "Remove all golems");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerGolemsData data = store.getComponent(ref, PlayerGolemsData.getComponentType());
        if (data == null) {
            data = store.addComponent(ref, PlayerGolemsData.getComponentType());
        }
        int count = data.getGolemCount();
        for (int i = count - 1; i >= 0; i--) {
            UUID uuid = data.getGolem(i);
            data.removeGolem(uuid);

            Ref<EntityStore> entityRef = world.getEntityRef(uuid);
            if (entityRef == null) continue;
            Store<EntityStore> entityStore = entityRef.getStore();
            entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
        }
    }
}
