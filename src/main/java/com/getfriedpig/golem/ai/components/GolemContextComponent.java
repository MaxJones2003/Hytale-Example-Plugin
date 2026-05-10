package com.getfriedpig.golem.ai.components;

import com.getfriedpig.golem.GolemPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;


public class GolemContextComponent implements Component<EntityStore> {
    public enum GolemState {
        Idle,
        MovingForward,
        MovingBackward,
        TurningRight,
        TurningLeft,
        Interacting,
    }

    @Nonnull
    public static final BuilderCodec<GolemContextComponent> CODEC = BuilderCodec.builder(GolemContextComponent.class, GolemContextComponent::new)
            .append(
                    new KeyedCodec<>("IsState", Codec.BOOLEAN),
                    (golemContextComponent, isState) -> golemContextComponent.isState = isState,
                    golemContextComponent -> golemContextComponent.isState
            ).documentation("Contains the state and other information about a golem entity.").add()
            .append(new KeyedCodec<>("IsIdle", Codec.BOOLEAN),
                    (golemContextComponent, isIdle) -> golemContextComponent.isIdle = isIdle,
                    golemContextComponent -> golemContextComponent.isIdle)
            .add()
            .append(new KeyedCodec<>("IsMovingForward", Codec.BOOLEAN),
                    (golemContextComponent, isMovingForward) -> golemContextComponent.isMovingForward = isMovingForward,
                    golemContextComponent -> golemContextComponent.isMovingForward)
            .add()
            .append(new KeyedCodec<>("IsMovingBackward", Codec.BOOLEAN),
                    (golemContextComponent, isMovingBackward) -> golemContextComponent.isMovingBackward = isMovingBackward,
                    golemContextComponent -> golemContextComponent.isMovingBackward)
            .add()
            .append(new KeyedCodec<>("IsTurningRight", Codec.BOOLEAN),
                    (golemContextComponent, isTurningRight) -> golemContextComponent.isTurningRight = isTurningRight,
                    golemContextComponent -> golemContextComponent.isTurningRight)
            .add()
            .append(new KeyedCodec<>("IsTurningLeft", Codec.BOOLEAN),
                    (golemContextComponent, isTurningLeft) -> golemContextComponent.isTurningLeft = isTurningLeft,
                    golemContextComponent -> golemContextComponent.isTurningLeft)
            .add()
            .append(new KeyedCodec<>("IsInteracting", Codec.BOOLEAN),
                    (golemContextComponent, isInteracting) -> golemContextComponent.isInteracting = isInteracting,
                    golemContextComponent -> golemContextComponent.isInteracting)
            .add()
            .build();


    private GolemState state;
    private boolean isState;
    private boolean isIdle;
    private boolean isMovingForward;
    private boolean isMovingBackward;
    private boolean isTurningRight;
    private boolean isTurningLeft;
    private boolean isInteracting;


    public boolean isState(GolemState state) {
        return state == this.state;
    }

    public void setState(GolemState state) {
        this.state = state;
    }

    public boolean isIdle() { return this.state == GolemState.Idle; }

    public boolean isMovingForward() { return this.state == GolemState.MovingForward; }

    public boolean isMovingBackward() { return this.state == GolemState.MovingBackward; }

    public boolean isTurningRight() { return this.state == GolemState.TurningRight; }

    public boolean isTurningLeft() { return this.state == GolemState.TurningLeft; }

    public boolean isInteracting() { return this.state == GolemState.Interacting; }

    @Override
    public Component<EntityStore> clone() {
        GolemContextComponent clone = new GolemContextComponent();
        clone.state = this.state;
        return clone;
    }

    public static ComponentType<EntityStore, GolemContextComponent> getComponentType() {
        return GolemPlugin.golemContextComponent;
    }
}
