package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.ai.GolemMovementCommand;
import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.ai.components.ScriptAddedComponent;
import com.getfriedpig.golem.ai.components.ScriptInstanceComponent;
import com.getfriedpig.golem.player.components.PlayerGolemsData;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AssignGolemFileAndRun  extends AbstractPlayerCommand {
    private final RequiredArg<Integer> golemNumber;
    private final RequiredArg<String> script;
    //private final RequiredArg<Integer> instructionCount;
    public AssignGolemFileAndRun() {

        super("assign", "Give a golem a file to run");

        this.golemNumber = this.withRequiredArg("index", "Index of golem in users golem array", ArgTypes.INTEGER);
        this.script = this.withRequiredArg("script", "script for golem to execute", ArgTypes.STRING);
        //this.instructionCount = this.withRequiredArg("count", "Number of times to run instruction", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        Integer index = golemNumber.get(commandContext);
        String scriptName = this.script.get(commandContext);

        PlayerGolemsData data = store.getComponent(ref, PlayerGolemsData.getComponentType());
        if (data == null) {
            System.out.println("No golems found");
            return;
        }

        UUID uuid = data.getGolem(index - 1);
        if (uuid == null) {
            System.out.println("No golem found at index: " + index);
            return;
        }
        System.out.println("Found golem: " + uuid);

        Ref<EntityStore> golemRef = world.getEntityRef(uuid);
        if (golemRef == null) {
            System.out.println("Golem ref not found");
            return;
        }
        Store<EntityStore> golemStore = golemRef.getStore();
        if (golemStore == null) {
            System.out.println("Golem store not found");
            return;
        }
        ScriptAddedComponent scriptAdditionRequest = new ScriptAddedComponent();
        scriptAdditionRequest.fileName = scriptName;
        scriptAdditionRequest.playerUUID = store.getComponent(ref, UUIDComponent.getComponentType()).getUuid();
        golemStore.addComponent(golemRef, ScriptAddedComponent.getComponentType(), scriptAdditionRequest);
    }
}
