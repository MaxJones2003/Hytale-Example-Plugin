package com.getfriedpig.golem.ai.components;

import com.getfriedpig.golem.GolemPlugin;
import com.getfriedpig.golem.ai.GolemMovementCommand;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class MovementStateComponent implements Component<EntityStore> {

    public @Nullable GolemMovementCommand activeCommand = null;

    public static final BuilderCodec<MovementStateComponent> CODEC = BuilderCodec.builder(MovementStateComponent.class, MovementStateComponent::new)
            .append(
                    new KeyedCodec<>("IsMoving", Codec.BOOLEAN),
                    (MovementStateComponent, isMoving) -> MovementStateComponent.isMoving = isMoving,
                    MovementStateComponent -> MovementStateComponent.isMoving
            ).documentation("Is the entity moving.").add()
            .append(
                    new KeyedCodec<>("IsRotating", Codec.BOOLEAN),
                    (MovementStateComponent, isRotating) -> MovementStateComponent.isRotating = isRotating,
                    MovementStateComponent -> MovementStateComponent.isRotating
            ).documentation("Is the entity rotating.").add()
            .append(
                    new KeyedCodec<>("DirectionIndex", Codec.INTEGER),
                    (MovementStateComponent, currentDirectionIndex) -> MovementStateComponent.currentDirectionIndex = currentDirectionIndex,
                    MovementStateComponent -> MovementStateComponent.currentDirectionIndex
            ).add()
            .build();

    // Movement
    public boolean isMoving = false;
    public Vector3d startPosition;
    public Vector3d targetPosition;

    // Rotation
    public boolean isRotating = false;
    public float startYaw;
    public float targetYaw;

    public double moveSpeed = 1.0;
    public float rotateSpeed = 180f; // degrees/sec

    public float movementTime = 0f;

    // Cardinal directions for grid-aligned movement
    public static final Vector3d[] CARDINAL_DIRECTIONS = new Vector3d[] {
            new Vector3d(0, 0, -1),  // North (negative Z)
            new Vector3d(-1, 0, 0),  // East (negative X)
            new Vector3d(0, 0, 1),   // South (positive Z)
            new Vector3d(1, 0, 0)    // West (positive X)
    };
    
    // Current facing direction index (0=North, 1=East, 2=South, 3=West)
    public int currentDirectionIndex = 0;

    @Override
    public Component<EntityStore> clone() {
        MovementStateComponent clone = new MovementStateComponent();
        clone.activeCommand = this.activeCommand;
        clone.isMoving = this.isMoving;
        clone.isRotating = this.isRotating;
        clone.startPosition = this.startPosition;
        clone.targetPosition = this.targetPosition;
        clone.startYaw = this.startYaw;
        clone.targetYaw = this.targetYaw;
        clone.movementTime = this.movementTime;
        clone.currentDirectionIndex = this.currentDirectionIndex;
        return clone;
    }

    /**
     * Get the current facing direction vector.
     */
    public Vector3d getCurrentDirection() {
        return CARDINAL_DIRECTIONS[currentDirectionIndex];
    }

    /**
     * Rotate left (counter-clockwise when viewed from above).
     * Updates the direction index to the left cardinal direction.
     */
    public void rotateLeft() {
        currentDirectionIndex = (currentDirectionIndex + 1) % 4;
    }

    /**
     * Rotate right (clockwise when viewed from above).
     * Updates the direction index to the right cardinal direction.
     */
    public void rotateRight() {
        currentDirectionIndex = (currentDirectionIndex + 3) % 4; // -1 mod 4 = 3
    }

    /**
     * Get the cardinal direction name for debugging.
     */
    public String getDirectionName() {
        return switch(currentDirectionIndex) {
            case 0 -> "North";
            case 1 -> "East";
            case 2 -> "South";
            case 3 -> "West";
            default -> "Unknown";
        };
    }

    public static ComponentType<EntityStore, MovementStateComponent> getComponentType() {
        return GolemPlugin.movementStateComponent;
    }
}