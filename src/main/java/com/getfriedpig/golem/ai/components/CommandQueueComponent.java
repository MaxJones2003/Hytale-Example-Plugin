package com.getfriedpig.golem.ai.components;

import com.getfriedpig.golem.GolemPlugin;
import com.getfriedpig.golem.ai.GolemMovementCommand;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedList;


public class CommandQueueComponent implements Component<EntityStore> {
    LinkedList<GolemMovementCommand> commandQueue = new LinkedList<GolemMovementCommand>();

    @Nonnull
    public static final BuilderCodec<CommandQueueComponent> CODEC = BuilderCodec.builder(CommandQueueComponent.class, CommandQueueComponent::new)
            .append(
                    new KeyedCodec<>("HasCommand", Codec.BOOLEAN),
                    (commandQueueComponent, hasCommand) -> commandQueueComponent.hasCommand = hasCommand,
                    commandQueueComponent -> commandQueueComponent.hasCommand
            ).documentation("Contains command queue of a golem.").add()
            .build();

    private boolean hasCommand;

    public boolean hasCommand() {
        hasCommand = !commandQueue.isEmpty();
        return hasCommand;
    }

    public boolean addCommand(GolemMovementCommand command) {
        return commandQueue.add(command);
    }

    public GolemMovementCommand popCommand() {
        return commandQueue.pop();
    }

    public GolemMovementCommand peekCommand() {
        return commandQueue.peek();
    }

    @Override
    public Component<EntityStore> clone() {
        CommandQueueComponent clone = new CommandQueueComponent();
        clone.commandQueue = new LinkedList<>(this.commandQueue);
        return clone;
    }

    public static ComponentType<EntityStore, CommandQueueComponent> getComponentType() {
        return GolemPlugin.commandQueueComponent;
    }
}
