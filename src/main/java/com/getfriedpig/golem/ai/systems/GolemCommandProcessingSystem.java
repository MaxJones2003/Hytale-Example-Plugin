package com.getfriedpig.golem.ai.systems;

import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.ai.components.MovementStateComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

/**
 * Processes commands from the queue and initializes movement/rotation states.
 * Responsible for state transitions and command initialization.
 */
public class GolemCommandProcessingSystem extends EntityTickingSystem<EntityStore> {
    private final Query<EntityStore> query;

    public GolemCommandProcessingSystem() {
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
        CommandQueueComponent cmdQ = chunk.getComponent(index, CommandQueueComponent.getComponentType());
        TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
        MovementStateComponent state = chunk.getComponent(index, MovementStateComponent.getComponentType());

        if (cmdQ == null || transform == null || state == null) return;

        // Only process if there's no active command or the current one has completed
        if (state.activeCommand != null) {
            return; // Let the movement system handle this
        }

        // Load next command from queue
        if (cmdQ.hasCommand()) {
            state.activeCommand = cmdQ.popCommand();
            initializeStateForCommand(state, transform);
        }
    }

    /**
     * Initialize the movement/rotation state based on the command type.
     */
    private void initializeStateForCommand(MovementStateComponent state, TransformComponent transform) {
        switch (state.activeCommand) {
            case MovingForward -> {
                Vector3d forward = state.getCurrentDirection();
                state.startPosition = transform.getPosition();
                state.targetPosition = state.startPosition.clone();
                state.targetPosition.add(forward);
                state.isMoving = true;
                state.movementTime = 0f;
            }

            case MovingBackward -> {
                Vector3d backward = state.getCurrentDirection();
                state.startPosition = transform.getPosition();
                state.targetPosition = state.startPosition.clone();
                state.targetPosition.subtract(backward);
                state.isMoving = true;
                state.movementTime = 0f;
            }

            case TurningLeft -> {
                int oldIndex = state.currentDirectionIndex;
                state.rotateLeft();
                state.isRotating = true;
                state.movementTime = 0f;
            }

            case TurningRight -> {
                int oldIndex = state.currentDirectionIndex;
                state.rotateRight();
                state.isRotating = true;
                state.movementTime = 0f;
            }

            case Idle -> {
                state.activeCommand = null;
            }
        }
    }

    /**
     * Get the cardinal direction name by index.
     */
    private String getDirectionNameByIndex(int index) {
        return switch(index) {
            case 0 -> "North";
            case 1 -> "East";
            case 2 -> "South";
            case 3 -> "West";
            default -> "Unknown";
        };
    }
}
