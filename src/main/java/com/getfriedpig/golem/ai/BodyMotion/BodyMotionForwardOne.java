package com.getfriedpig.golem.ai.BodyMotion;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BodyMotionForwardOne extends BodyMotionBase {
    public BodyMotionForwardOne(@NonNull BuilderBodyMotionBase builderMotionBase) {
        super(builderMotionBase);
    }

    @Override
    public boolean computeSteering(@NonNull Ref<EntityStore> ref, @NonNull Role role, @Nullable InfoProvider infoProvider, double v, @NonNull Steering steering, @NonNull ComponentAccessor<EntityStore> componentAccessor) {
        return false;
    }
}
