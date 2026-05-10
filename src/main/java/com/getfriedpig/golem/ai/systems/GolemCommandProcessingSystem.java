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
            System.out.println("[GolemCommandProcessing] Command selected: " + state.activeCommand);
            initializeStateForCommand(state, transform);
            System.out.println("[GolemCommandProcessing] Command initialized at position: " + transform.getPosition());
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
                System.out.println("[GolemCommandProcessing] MovingForward (" + state.getDirectionName() + ")");
                System.out.println("  Direction Vector: " + forward + " (Index: " + state.currentDirectionIndex + ")");
                System.out.println("  Start: " + state.startPosition + " Target: " + state.targetPosition);
            }

            case MovingBackward -> {
                Vector3d backward = state.getCurrentDirection();
                state.startPosition = transform.getPosition();
                state.targetPosition = state.startPosition.clone();
                state.targetPosition.subtract(backward);
                state.isMoving = true;
                state.movementTime = 0f;
                System.out.println("[GolemCommandProcessing] MovingBackward - Start: " + state.startPosition + " Target: " + state.targetPosition);
            }

            case TurningLeft -> {
                int oldIndex = state.currentDirectionIndex;
                state.rotateLeft();
                state.isRotating = true;
                state.movementTime = 0f;
                System.out.println("[GolemCommandProcessing] TurningLeft - " + getDirectionNameByIndex(oldIndex) + " -> " + state.getDirectionName());
            }

            case TurningRight -> {
                int oldIndex = state.currentDirectionIndex;
                state.rotateRight();
                state.isRotating = true;
                state.movementTime = 0f;
                System.out.println("[GolemCommandProcessing] TurningRight - " + getDirectionNameByIndex(oldIndex) + " -> " + state.getDirectionName());
            }

            case Idle -> {
                System.out.println("[GolemCommandProcessing] Idle command processed");
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
