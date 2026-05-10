package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.ai.GolemMovementCommand;
import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.player.components.PlayerGolemsData;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

public class InstructGolemCommand  extends AbstractPlayerCommand {
    private final RequiredArg<Integer> golemNumber;
    private final RequiredArg<GolemMovementCommand> direction;
    private final RequiredArg<Integer> instructionCount;
    public InstructGolemCommand() {

        super("instruct", "Instruct a golem");

        this.golemNumber = this.withRequiredArg("index", "Index of golem in users golem array", ArgTypes.INTEGER);
        this.direction = this.withRequiredArg("instruction", "Instruction for golem to execute", ArgTypes.forEnum("GolemMovementCommand", GolemMovementCommand.class));
        this.instructionCount = this.withRequiredArg("count", "Number of times to run instruction", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        Integer index = golemNumber.get(commandContext);
        GolemMovementCommand direction = this.direction.get(commandContext);

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

        CommandQueueComponent cmdQ = golemStore.getComponent(golemRef, CommandQueueComponent.getComponentType());
        if (cmdQ == null) {
            System.out.println("Golem command queue not found");
            return;
        }

        int count = instructionCount.get(commandContext);

        for (int i = 0; i < count; i++) {
            cmdQ.addCommand(direction);
        }
    }
}
