package com.getfriedpig.golem.bytecode.NativeFunctions;

import com.getfriedpig.golem.ai.GolemMovementCommand;
import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.bytecode.ExecutionContext;
import com.getfriedpig.golem.bytecode.NativeFunctionRegistry;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Native entity function registrations
 * Provides entity-related operations accessible from bytecode scripts
 */
public class Entity {
    
    /**
     * Register all entity functions in the "Entity" namespace
     */
    public static void register() {
        NativeFunctionRegistry registry = NativeFunctionRegistry.getInstance();
        
        // Entity.getSelf() - returns the UUID of the entity running the script
        // Most users won't need this, but it's available for advanced use cases
        registry.register("Entity", "getSelf", args -> {
            java.util.UUID currentUUID = ExecutionContext.getCurrentEntityUUID();
            if (currentUUID == null) {
                throw new Exception("No entity context - script not running in an entity");
            }
            return currentUUID.toString();
        });
        
        // Entity.log(message) - logs a message from the entity
        // The backend can access the UUID via ExecutionContext.getCurrentEntityUUID()
        registry.register("Entity", "log", args -> {
            java.util.UUID entityUUID = ExecutionContext.getCurrentEntityUUID();
            String message = args.get(0).toString();
            return null;
        });

        registry.register("Entity", "forward", args -> {
            java.util.UUID entityUUID = ExecutionContext.getCurrentEntityUUID();
            Store<EntityStore> store = ExecutionContext.getCurrentEntityStore();
            Ref<EntityStore> ref = ExecutionContext.getCurrentEntityRef();
            if (store == null || ref == null) { return null; }
            CommandQueueComponent cmdQ = store.getComponent(ref, CommandQueueComponent.getComponentType());
            if (cmdQ == null) {
                cmdQ = store.addComponent(ref, CommandQueueComponent.getComponentType());
            }
            cmdQ.addCommand(GolemMovementCommand.MovingForward);

            return null;
        });

        registry.register("Entity", "backward", args -> {
            java.util.UUID entityUUID = ExecutionContext.getCurrentEntityUUID();
            Store<EntityStore> store = ExecutionContext.getCurrentEntityStore();
            Ref<EntityStore> ref = ExecutionContext.getCurrentEntityRef();
            if (store == null || ref == null) { return null; }
            CommandQueueComponent cmdQ = store.getComponent(ref, CommandQueueComponent.getComponentType());
            if (cmdQ == null) {
                cmdQ = store.addComponent(ref, CommandQueueComponent.getComponentType());
            }
            cmdQ.addCommand(GolemMovementCommand.MovingBackward);

            return null;
        });

        registry.register("Entity", "turnLeft", args -> {
            java.util.UUID entityUUID = ExecutionContext.getCurrentEntityUUID();
            Store<EntityStore> store = ExecutionContext.getCurrentEntityStore();
            Ref<EntityStore> ref = ExecutionContext.getCurrentEntityRef();
            if (store == null || ref == null) { return null; }
            CommandQueueComponent cmdQ = store.getComponent(ref, CommandQueueComponent.getComponentType());
            if (cmdQ == null) {
                cmdQ = store.addComponent(ref, CommandQueueComponent.getComponentType());
            }
            cmdQ.addCommand(GolemMovementCommand.TurningLeft);

            return null;
        });

        registry.register("Entity", "turnRight", args -> {
            java.util.UUID entityUUID = ExecutionContext.getCurrentEntityUUID();
            Store<EntityStore> store = ExecutionContext.getCurrentEntityStore();
            Ref<EntityStore> ref = ExecutionContext.getCurrentEntityRef();
            if (store == null || ref == null) { return null; }
            CommandQueueComponent cmdQ = store.getComponent(ref, CommandQueueComponent.getComponentType());
            if (cmdQ == null) {
                cmdQ = store.addComponent(ref, CommandQueueComponent.getComponentType());
            }
            cmdQ.addCommand(GolemMovementCommand.TurningRight);

            return null;
        });
    }
}
