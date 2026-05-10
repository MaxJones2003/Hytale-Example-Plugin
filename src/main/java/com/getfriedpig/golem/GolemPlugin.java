package com.getfriedpig.golem;

import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.ai.components.GolemContextComponent;
import com.getfriedpig.golem.ai.components.MovementStateComponent;
import com.getfriedpig.golem.ai.systems.GolemMovementApplyingSystem;
import com.getfriedpig.golem.ai.systems.GolemCommandProcessingSystem;
import com.getfriedpig.golem.ai.systems.GolemNPCSystem;
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
    public static ComponentType<EntityStore, GolemContextComponent> golemContextComponent;
    public static ComponentType<EntityStore, CommandQueueComponent> commandQueueComponent;
    public static ComponentType<EntityStore, MovementStateComponent> movementStateComponent;
    public static ComponentType<EntityStore, PlayerGolemsData> playerGolemsData;
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
    }

    private void NPCSetup() {
        golemContextComponent =  this.getEntityStoreRegistry().registerComponent(GolemContextComponent.class, "GolemContext", GolemContextComponent.CODEC);
        commandQueueComponent =  this.getEntityStoreRegistry().registerComponent(CommandQueueComponent.class, "CommandQueue", CommandQueueComponent.CODEC);
        movementStateComponent = this.getEntityStoreRegistry().registerComponent(MovementStateComponent.class, "MovementState", MovementStateComponent.CODEC);
        playerGolemsData =       this.getEntityStoreRegistry().registerComponent(PlayerGolemsData.class, "PlayerGolemsData", PlayerGolemsData.CODEC);
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
        // Register actions

        // Register sensors
    }
}
