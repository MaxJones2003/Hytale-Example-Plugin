package com.getfriedpig.golem.ai.systems;

import com.getfriedpig.golem.ai.components.*;
import com.getfriedpig.golem.bytecode.BytecodeVM;
import com.getfriedpig.golem.bytecode.ExecutionState;
import com.getfriedpig.golem.bytecode.NativeRegistrationManager;
import com.getfriedpig.golem.bytecode.VMResult;
import com.getfriedpig.golem.fileio.FileManager;
import com.getfriedpig.golem.typescript.TypeScript;
import com.google.gson.JsonObject;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class VMProcessorSystem extends EntityTickingSystem<EntityStore> {
    private final Query<EntityStore> query;
    private static boolean nativeFunctionsRegistered = false;

    public VMProcessorSystem() {
        query = ScriptInstanceComponent.getComponentType();
        
        // Register native functions once for all VMs
        if (!nativeFunctionsRegistered) {
            registerNativeFunctions();
            nativeFunctionsRegistered = true;
        }
    }

    private static void registerNativeFunctions() {
        NativeRegistrationManager.registerAll();
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
        ScriptRemovedComponent scriptRemovalRequest = chunk.getComponent(index, ScriptRemovedComponent.getComponentType());
        ScriptInstanceComponent scriptInstance = chunk.getComponent(index, ScriptInstanceComponent.getComponentType());
        if (scriptInstance == null) return;

        if (scriptRemovalRequest != null) {
            stopScript(dt, index, chunk, store, buffer, scriptRemovalRequest, scriptInstance);
        }

        ScriptAddedComponent scriptAdditionRequest = chunk.getComponent(index, ScriptAddedComponent.getComponentType());
        if (scriptAdditionRequest != null) {
            System.out.println("Script added: " + scriptAdditionRequest.fileName);
            startScript(dt, index, chunk, store, buffer, scriptAdditionRequest, scriptInstance);
        }

        if (scriptInstance != null) {
            executeScript(dt, index,chunk, store, buffer, scriptInstance);
        }

    }

    protected void stopScript(float dt, int index,
                              @NonNull ArchetypeChunk<EntityStore> chunk,
                              @NonNull Store<EntityStore> store,
                              @NonNull CommandBuffer<EntityStore> buffer,
                              @NonNull ScriptRemovedComponent scriptRemovedComponent,
                              @NonNull ScriptInstanceComponent scriptInstance) {
        // Reset the script instance component to a clean state
        scriptInstance.vm = null;
        scriptInstance.state = null;
        scriptInstance.fileName = "";
        scriptInstance.status = ScriptInstanceComponent.Status.NO_SCRIPT;
        
        // Remove the removal marker
        buffer.removeComponent(chunk.getReferenceTo(index), ScriptRemovedComponent.getComponentType());
    }

    protected void startScript(float dt, int index, @NonNull ArchetypeChunk<EntityStore> chunk,
                              @NonNull Store<EntityStore> store,
                              @NonNull CommandBuffer<EntityStore> buffer,
                              @NonNull ScriptAddedComponent scriptAddedComponent,
                              @NonNull ScriptInstanceComponent scriptInstance) {

        // Load and initialize the script
        scriptInstance.fileName = scriptAddedComponent.fileName;
        scriptInstance.playerUUID = scriptAddedComponent.playerUUID;
        scriptInstance.createdAt = System.currentTimeMillis();
        scriptInstance.status = ScriptInstanceComponent.Status.RUNNING;
        scriptInstance.vm = loadVM(scriptAddedComponent.playerUUID, scriptAddedComponent.fileName);
        scriptInstance.state = new ExecutionState();
        
        // Set the UUID of the entity running this script
        scriptInstance.state.entityUUID = scriptInstance.playerUUID;
        
        // Set stable ECS context (store and command buffer don't change)
        // currentChunk and currentIndex are refreshed before each execution in executeScript()
        scriptInstance.state.entityStore = store;
        scriptInstance.state.entityRef = chunk.getReferenceTo(index);
        scriptInstance.state.commandBuffer = buffer;
        
        // Remove the addition marker
        buffer.removeComponent(chunk.getReferenceTo(index), ScriptAddedComponent.getComponentType());
    }
    
    protected BytecodeVM loadVM(UUID playerUUID, @NonNull String fileName) {
        try {
            // Read the TypeScript file
            String fileContent = FileManager.FileReadRequest(playerUUID, fileName);
            if (fileContent == null) {
                return null;
            }
            
            // Compile TypeScript to bytecode
            JsonObject bytecode = TypeScript.compile(fileContent);
            if (bytecode == null) {
                return null;
            }
            
            // Load bytecode into VM
            // Native functions are shared globally via singleton registry
            BytecodeVM vm = BytecodeVM.load(bytecode);
            vm.setMaxInstructionsPerFrame(1000);
            
            return vm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void executeScript(float dt, int index,
                                 @NonNull ArchetypeChunk<EntityStore> chunk,
                                 @NonNull Store<EntityStore> store,
                                 @NonNull CommandBuffer<EntityStore> buffer,
                                 @NonNull ScriptInstanceComponent scriptInstanceComponent) {
        if (scriptInstanceComponent.status == ScriptInstanceComponent.Status.NO_SCRIPT
                || scriptInstanceComponent.status == ScriptInstanceComponent.Status.COMPLETED
                || scriptInstanceComponent.status == ScriptInstanceComponent.Status.ERROR) {
            return;
        }

        if (scriptInstanceComponent.status == ScriptInstanceComponent.Status.PAUSED) {
            // TODO: handle unpausing
        }

        // Reinitialize VM if null (e.g., after world reload while script was running)
        if (scriptInstanceComponent.vm == null) {
            if (scriptInstanceComponent.playerUUID != null) {
                scriptInstanceComponent.vm = loadVM(scriptInstanceComponent.playerUUID, scriptInstanceComponent.fileName);
                if (scriptInstanceComponent.vm == null) {
                    System.err.println("Failed to reload VM for script: " + scriptInstanceComponent.fileName);
                    scriptInstanceComponent.status = ScriptInstanceComponent.Status.ERROR;
                    return;
                }
            } else {
                System.err.println("Cannot reload VM - playerUUID is null");
                scriptInstanceComponent.status = ScriptInstanceComponent.Status.ERROR;
                return;
            }
        }

        if (scriptInstanceComponent.state == null) {
            scriptInstanceComponent.state = new ExecutionState();
        }

        // Sync the state with the VM before execution
        scriptInstanceComponent.vm.setState(scriptInstanceComponent.state);

        VMResult result = scriptInstanceComponent.vm.execute();
        
        // Handle the result
        switch (result.status) {
            case COMPLETED:
                scriptInstanceComponent.status = ScriptInstanceComponent.Status.COMPLETED;
                System.out.println("[VMProcessor] Script completed: " + scriptInstanceComponent.fileName);
                break;
                
            case PAUSED:
                scriptInstanceComponent.status = ScriptInstanceComponent.Status.PAUSED;
                scriptInstanceComponent.state = result.state;
                System.out.println("[VMProcessor] Script paused at checkpoint: " + result.checkpointId);
                break;
                
            case ERROR:
                scriptInstanceComponent.status = ScriptInstanceComponent.Status.ERROR;
                System.err.println("[VMProcessor] Script error: " + result.errorMessage);
                break;
                
            case RUNNING:
                // Continue running in next frame
                break;
        }
    }
}