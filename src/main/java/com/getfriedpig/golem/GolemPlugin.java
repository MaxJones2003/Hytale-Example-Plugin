package com.getfriedpig.golem;

import com.getfriedpig.golem.ai.components.*;
import com.getfriedpig.golem.ai.systems.*;
import com.getfriedpig.golem.commands.*;
import com.getfriedpig.golem.player.components.PlayerGolemsData;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class GolemPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static ComponentType<EntityStore, CommandQueueComponent> commandQueueComponent;
    public static ComponentType<EntityStore, MovementStateComponent> movementStateComponent;
    public static ComponentType<EntityStore, PlayerGolemsData> playerGolemsData;
    public static ComponentType<EntityStore, ScriptInstanceComponent> scriptInstanceComponent;
    public static ComponentType<EntityStore, ScriptRemovedComponent> scriptRemovedComponent;
    public static ComponentType<EntityStore, ScriptAddedComponent> scriptAddedComponent;
    
    public GolemPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        NPCSetup();
        commandSetup();
    }

    @Override
    protected void start() {
        NPCStart();
    }

    private void commandSetup() {
        var commandRegistry = getCommandRegistry();
        commandRegistry.registerCommand(new LoxTestCommand("run", "Tests the parser/interpreter"));
        commandRegistry.registerCommand(new LoxTextEditorCommand());
        commandRegistry.registerCommand(new SpawnGolemCommand());
        commandRegistry.registerCommand(new InstructGolemCommand());
        commandRegistry.registerCommand(new RemoveAllGolemsCommand());
        commandRegistry.registerCommand(new TypeScriptTestCommand("tstest", "Tests the TypeScript compiler"));
        commandRegistry.registerCommand(new TypeScriptExecCommand("tsexec", "Compiles and executes a TypeScript script"));
        commandRegistry.registerCommand(new AssignGolemFileAndRun());
        commandRegistry.registerCommand(new UnAssignGolemScriptCommand());
    }

    private void NPCSetup() {
        commandQueueComponent =  this.getEntityStoreRegistry().registerComponent(CommandQueueComponent.class, "CommandQueue", CommandQueueComponent.CODEC);
        movementStateComponent = this.getEntityStoreRegistry().registerComponent(MovementStateComponent.class, "MovementState", MovementStateComponent.CODEC);
        playerGolemsData =       this.getEntityStoreRegistry().registerComponent(PlayerGolemsData.class, "PlayerGolemsData", PlayerGolemsData.CODEC);
        scriptInstanceComponent = this.getEntityStoreRegistry().registerComponent(ScriptInstanceComponent.class, "ScriptInstanceComponent", ScriptInstanceComponent.CODEC);
        scriptRemovedComponent = this.getEntityStoreRegistry().registerComponent(ScriptRemovedComponent.class, "ScriptRemovedComponent", ScriptRemovedComponent.CODEC);
        scriptAddedComponent = this.getEntityStoreRegistry().registerComponent(ScriptAddedComponent.class, "ScriptAddedComponent", ScriptAddedComponent.CODEC);
    }

    private void NPCStart() {
        LOGGER.atInfo().log("Registering Golem system");
        ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
        if (npcComponentType == null) {
            LOGGER.atSevere().log("Failed to register Golem system");
            return;
        }

        // register system
        this.getEntityStoreRegistry().registerSystem(new GolemMovementApplyingSystem());
        this.getEntityStoreRegistry().registerSystem(new GolemCommandProcessingSystem());
        this.getEntityStoreRegistry().registerSystem(new GolemNPCSystem());
        this.getEntityStoreRegistry().registerSystem(new VMProcessorSystem());
        // Register actions

        // Register sensors
    }
}
