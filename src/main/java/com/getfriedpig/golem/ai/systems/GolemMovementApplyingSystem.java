package com.getfriedpig.golem.ai.systems;

import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.ai.components.MovementStateComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

/**
 * Applies movement and rotation to entities based on their movement state.
 * Responsible for updating transforms and transitioning states when complete.
 */
public class GolemMovementApplyingSystem extends EntityTickingSystem<EntityStore> {
    private final Query<EntityStore> query;

    public GolemMovementApplyingSystem() {
        query = Query.and(
            CommandQueueComponent.getComponentType(),
            MovementStateComponent.getComponentType(),
            TransformComponent.getComponentType()
        );
    }

    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void tick(float dt, int index,
                     @NonNull ArchetypeChunk<EntityStore> chunk,
                     @NonNull Store<EntityStore> store,
                     @NonNull CommandBuffer<EntityStore> buffer) {
        TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
        MovementStateComponent state = chunk.getComponent(index, MovementStateComponent.getComponentType());

        if (transform == null || state == null || state.activeCommand == null) {
            return;
        }

        state.movementTime += dt;

        switch (state.activeCommand) {
            case MovingForward, MovingBackward -> {
                applyMovement(chunk, index, state, transform);
            }

            case TurningLeft, TurningRight -> {
                applyRotation(chunk, index, state, transform);
            }

            case Idle -> {
                state.activeCommand = null;
            }
        }
    }

    /**
     * Apply movement to the entity based on current state.
     * Lerps the position between start and target at a constant speed.
     */
    private void applyMovement(ArchetypeChunk<EntityStore> chunk, int index, 
                              MovementStateComponent state, TransformComponent transform) {
        // Calculate distance and duration
        double distance = state.startPosition.distanceTo(state.targetPosition);
        float duration = (float) (distance / state.moveSpeed);
        
        // Calculate progress (0 to 1)
        float progress = Math.min(state.movementTime / duration, 1.0f);
        
        // Interpolate position
        Vector3d newPosition = lerpVector(state.startPosition, state.targetPosition, progress);
        transform.setPosition(newPosition);

        // Check if movement is complete
        if (progress >= 1.0f) {
            transform.setPosition(state.targetPosition);  // Snap to exact target
            state.isMoving = false;
            state.movementTime = 0f;
            state.activeCommand = null;  // Command complete, ready for next
        }
    }

    /**
     * Linearly interpolate between two Vector3d positions.
     */
    private Vector3d lerpVector(Vector3d start, Vector3d end, float t) {
        Vector3d result = start.clone();
        Vector3d diff = end.clone();
        diff.subtract(start);
        diff.scale(t);
        result.add(diff);
        return result;
    }

    /**
     * Apply rotation to the entity based on current state.
     * With cardinal directions, rotation is instant.
     * Updates both HeadRotation and body TransformComponent to match the new cardinal direction.
     */
    private void applyRotation(ArchetypeChunk<EntityStore> chunk, int index, 
                              MovementStateComponent state, TransformComponent transform) {
        // Rotation is instant with cardinal directions
        state.isRotating = false;
        state.movementTime = 0f;

        float newYaw = getYawFromCardinalDirection(state.currentDirectionIndex);
        
        // Update HeadRotation (look direction)
        HeadRotation headRotation = chunk.getComponent(index, HeadRotation.getComponentType());
        if (headRotation != null) {
            headRotation.setRotation(new com.hypixel.hytale.math.vector.Vector3f(0, newYaw, 0));
        } else {
            //System.out.println("[GolemMovementApplying] WARNING: HeadRotation not found");
        }
        
        // Update body TransformComponent (body orientation) to match
        com.hypixel.hytale.math.vector.Vector3f bodyRotation = new com.hypixel.hytale.math.vector.Vector3f(0, newYaw, 0);
        transform.setRotation(bodyRotation);

        state.activeCommand = null;
    }

    /**
     * Convert cardinal direction index to yaw in radians.
     * North (0): 0, East (1): π/2, South (2): π, West (3): -π/2
     */
    private float getYawFromCardinalDirection(int directionIndex) {
        return switch(directionIndex) {
            case 0 -> 0;                              // North
            case 1 -> (float) Math.PI / 2;            // East
            case 2 -> (float) Math.PI;                // South
            case 3 -> -(float) Math.PI / 2;           // West
            default -> 0;
        };
    }
}
