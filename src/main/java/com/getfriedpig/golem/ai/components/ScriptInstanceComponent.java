package com.getfriedpig.golem.ai.components;

import com.getfriedpig.golem.GolemPlugin;
import com.getfriedpig.golem.bytecode.BytecodeVM;
import com.getfriedpig.golem.bytecode.ExecutionState;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;


public class ScriptInstanceComponent implements Component<EntityStore> {
    public enum Status {
        RUNNING,
        PAUSED,
        COMPLETED,
        ERROR,
        NO_SCRIPT
    }

    @Nonnull
    public static final BuilderCodec<ScriptInstanceComponent> CODEC = BuilderCodec.builder(ScriptInstanceComponent.class, ScriptInstanceComponent::new)
            .append(
                    new KeyedCodec<>("Filename", Codec.STRING),
                    (scriptInstanceComponent, fileName) -> scriptInstanceComponent.fileName = fileName,
                    scriptInstanceComponent -> scriptInstanceComponent.fileName
            ).add()
            .append(
                    new KeyedCodec<>("Status", Codec.INTEGER),
                    (scriptInstanceComponent, status) -> scriptInstanceComponent.status = Status.values()[status],
                    scriptInstanceComponent -> scriptInstanceComponent.status.ordinal()
            ).add()
            .append(
                    new KeyedCodec<>("CreatedAt", Codec.LONG),
                    ((scriptInstanceComponent, createdAt) -> scriptInstanceComponent.createdAt = createdAt),
                    scriptInstanceComponent -> scriptInstanceComponent.createdAt
            ).add()
            .append(
                    new KeyedCodec<>("PlayerUUID", Codec.UUID_STRING),
                    (scriptInstanceComponent, playerUUID) -> scriptInstanceComponent.playerUUID = playerUUID,
                    scriptInstanceComponent -> scriptInstanceComponent.playerUUID
            ).add()
            .build();

    public BytecodeVM vm;
    public ExecutionState state;
    public String fileName = "";
    public long createdAt = 0;
    public UUID playerUUID = null;

    public Status status = Status.NO_SCRIPT;




    @Override
    public Component<EntityStore> clone() {
        ScriptInstanceComponent clone = new ScriptInstanceComponent();
        if (vm == null) vm = new BytecodeVM();
        clone.vm = vm.clone();
        if (state == null) state = new ExecutionState();
        clone.state = state.clone();
        clone.fileName = fileName;
        clone.status = status;
        clone.playerUUID = playerUUID;
        clone.createdAt = createdAt;

        return clone;
    }

    public static ComponentType<EntityStore, ScriptInstanceComponent> getComponentType() {
        return GolemPlugin.scriptInstanceComponent;
    }
}