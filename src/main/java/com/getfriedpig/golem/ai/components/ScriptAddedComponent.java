package com.getfriedpig.golem.ai.components;

import com.getfriedpig.golem.GolemPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class ScriptAddedComponent  implements Component<EntityStore> {
    public static ComponentType<EntityStore, ScriptAddedComponent> getComponentType() {
        return GolemPlugin.scriptAddedComponent;
    }

    public static final BuilderCodec<ScriptAddedComponent> CODEC = BuilderCodec.builder(ScriptAddedComponent.class, ScriptAddedComponent::new)
            .append(
                    new KeyedCodec<>("PlayerUUID", Codec.UUID_STRING),
                    (scriptAddedComponent, playerUUID) -> scriptAddedComponent.playerUUID = playerUUID,
                    scriptAddedComponent -> scriptAddedComponent.playerUUID
            ).add()
            .append(
                    new KeyedCodec<>("Filename", Codec.STRING),
                    (scriptAddedComponent, fileName) -> scriptAddedComponent.fileName = fileName,
                    scriptAddedComponent -> scriptAddedComponent.fileName
            ).add()
            .build();

    public UUID playerUUID;
    public String fileName;

    @Override
    public @Nullable Component<EntityStore> clone() {
        ScriptAddedComponent clone = new ScriptAddedComponent();
        clone.playerUUID = this.playerUUID;
        clone.fileName = this.fileName;
        return clone;
    }
}
