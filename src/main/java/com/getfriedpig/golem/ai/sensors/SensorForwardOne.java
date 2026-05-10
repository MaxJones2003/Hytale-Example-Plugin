package com.getfriedpig.golem.ai.sensors;

import com.getfriedpig.golem.ai.builders.BuilderSensorMoveForward;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


import javax.annotation.Nonnull;

public class SensorForwardOne extends SensorBase {
    protected final boolean moveForward;
    public SensorForwardOne(@NonNull BuilderSensorMoveForward builderSensorBase, @Nonnull BuilderSupport builderSupport) {
        super(builderSensorBase);
        this.moveForward = builderSensorBase.getIsMovingForward(builderSupport);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref,
                           @Nonnull Role role,
                           double dt,
                           @Nonnull Store<EntityStore> store) {

        return false;
    }

    @Override
    public @Nullable InfoProvider getSensorInfo() {
        return null;
    }
}
