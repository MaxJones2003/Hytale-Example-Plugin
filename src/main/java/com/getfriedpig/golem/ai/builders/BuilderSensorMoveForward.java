package com.getfriedpig.golem.ai.builders;

import com.getfriedpig.golem.ai.sensors.SensorForwardOne;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;

import javax.annotation.Nonnull;

public class BuilderSensorMoveForward extends BuilderSensorBase {
    protected final BooleanHolder moveForward = new BooleanHolder();

    public boolean getIsMovingForward(@Nonnull BuilderSupport builderSupport) {
        return this.moveForward.get(builderSupport.getExecutionContext());
    }

    @Override
    @Nonnull
    public String getShortDescription() {
        return "Checks if move forward was requested/in progress";
    }

    @Override
    @Nonnull
    public String getLongDescription() {
        return this.getShortDescription();
    }

    @Override
    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorForwardOne(this, builderSupport);
    }

    @Override
    @Nonnull
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Override
    @Nonnull
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getBoolean(
                data,
                "IsMovingForward",
                this.moveForward,
                false,
                BuilderDescriptorState.Stable,
                "If NPC is/should move forward",
                ""
        );
        return this;
    }
}
