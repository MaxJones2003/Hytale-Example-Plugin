package com.getfriedpig.golem.ai.components;

import com.getfriedpig.golem.GolemPlugin;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

public class ScriptRemovedComponent  implements Component<EntityStore> {
    public static ComponentType<EntityStore, ScriptRemovedComponent> getComponentType() {
        return GolemPlugin.scriptRemovedComponent;
    }

    public static final BuilderCodec<ScriptRemovedComponent> CODEC = BuilderCodec.builder(ScriptRemovedComponent.class, ScriptRemovedComponent::new)

            .build();

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new ScriptRemovedComponent();
    }
}
