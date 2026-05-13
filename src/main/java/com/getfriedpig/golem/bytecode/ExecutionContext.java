package com.getfriedpig.golem.bytecode;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

/**
 * Provides thread-local access to the current execution context
 * Allows native functions and commands to access the UUID of the entity running the script,
 * as well as ECS context (chunk, store, command buffer, index)
 */
public class ExecutionContext {
    private static final ThreadLocal<ExecutionState> currentState = new ThreadLocal<>();

    /**
     * Set the current execution state (called by BytecodeVM before executing)
     */
    public static void setCurrentState(ExecutionState state) {
        currentState.set(state);
    }

    /**
     * Get the current execution state
     */
    public static ExecutionState getCurrentState() {
        return currentState.get();
    }

    /**
     * Get the UUID of the entity running the current script
     * @return The entity UUID, or null if no script is running
     */
    public static UUID getCurrentEntityUUID() {
        ExecutionState state = currentState.get();
        return state != null ? state.entityUUID : null;
    }

    /**
     * Get the archetype chunk containing the entity (current fresh value)
     * @return The chunk, or null if not available
     */
    public static Ref<EntityStore> getCurrentEntityRef() {
        ExecutionState state = currentState.get();
        return state != null ? state.entityRef : null;
    }

    /**
     * Get the store managing the entity
     * @return The store, or null if no script is running
     */
    public static Store<EntityStore> getCurrentEntityStore() {
        ExecutionState state = currentState.get();
        return state != null ? state.entityStore : null;
    }

    /**
     * Get the command buffer for queuing entity changes
     * @return The command buffer, or null if no script is running
     */
    public static CommandBuffer<EntityStore> getCurrentCommandBuffer() {
        ExecutionState state = currentState.get();
        return state != null ? state.commandBuffer : null;
    }

    /**
     * Check if a script is currently running
     */
    public static boolean isExecuting() {
        return currentState.get() != null;
    }

    /**
     * Clear the current execution state (called by BytecodeVM after execution)
     */
    public static void clear() {
        currentState.remove();
    }
}
