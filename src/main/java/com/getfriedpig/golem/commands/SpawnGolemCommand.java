package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.ai.components.CommandQueueComponent;
import com.getfriedpig.golem.ai.components.MovementStateComponent;
import com.getfriedpig.golem.player.components.PlayerGolemsData;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;

import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SpawnGolemCommand extends AbstractPlayerCommand {
    public SpawnGolemCommand() {
        super("spawng", "Spawn a golem");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Kweebec_Sapling");
        if (modelAsset == null) { return; }
        Model model = Model.createScaledModel(modelAsset, 1);
        TransformComponent transform = store.getComponent(playerRef.getReference(), EntityModule.get().getTransformComponentType());
        Vector3d pos = transform.getPosition();
        Vector3f rot = transform.getRotation();
        
        // Try to get HeadRotation for more accurate look direction
        HeadRotation playerHeadRotation = store.getComponent(playerRef.getReference(), HeadRotation.getComponentType());
        float lookYaw = rot.getYaw();
        if (playerHeadRotation != null) {
            lookYaw = playerHeadRotation.getRotation().getYaw();
            System.out.println("[SpawnGolemCommand] Using player HeadRotation yaw: " + lookYaw);
        } else {
            System.out.println("[SpawnGolemCommand] Using player TransformComponent yaw: " + lookYaw);
        }

        // Round position to nearest 0.5 (center of blocks)
        Vector3d roundedPos = new Vector3d(
            roundToHalf(pos.getX()),
            Math.floor(pos.getY()),
            roundToHalf(pos.getZ())
        );

        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(roundedPos, rot));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(Interactions.getComponentType(), new Interactions());
        
        // Create movement state with direction matching player's look direction
        MovementStateComponent movementState = new MovementStateComponent();
        int directionIndex = getCardinalDirectionFromYaw(lookYaw);
        System.out.println("[SpawnGolemCommand] Player yaw: " + lookYaw + " -> Direction index: " + directionIndex);
        movementState.currentDirectionIndex = directionIndex;
        holder.addComponent(MovementStateComponent.getComponentType(), movementState);
        
        // Create HeadRotation with yaw matching the cardinal direction
        HeadRotation headRotation = new HeadRotation();
        float yaw = getYawFromCardinalDirection(directionIndex);
        headRotation.setRotation(new com.hypixel.hytale.math.vector.Vector3f(0, yaw, 0));
        holder.addComponent(HeadRotation.getComponentType(), headRotation);
        
        // Update body rotation to match the cardinal direction
        com.hypixel.hytale.math.vector.Vector3f bodyRotation = new com.hypixel.hytale.math.vector.Vector3f(0, yaw, 0);
        TransformComponent golemTransform = (TransformComponent) holder.getComponent(TransformComponent.getComponentType());
        if (golemTransform != null) {
            golemTransform.setRotation(bodyRotation);
        }
        
        System.out.println("[SpawnGolemCommand] Golem spawned with body and head rotation set to yaw: " + yaw + " radians (direction: " + getDirectionNameByIndex(directionIndex) + ")");
        
        CommandQueueComponent cmdQ = new CommandQueueComponent();
        holder.addComponent(CommandQueueComponent.getComponentType(), cmdQ);

        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.ensureComponent(Interactable.getComponentType());

        UUIDComponent uuidC = holder.getComponent(UUIDComponent.getComponentType());
        if (uuidC == null) return;


        PlayerGolemsData data = store.getComponent(ref, PlayerGolemsData.getComponentType());
        if (data == null) {
            data = store.addComponent(ref, PlayerGolemsData.getComponentType());
        }
        data.addGolem(uuidC.getUuid());
        System.out.println("New golem spawned: " + uuidC.getUuid());

        store.addEntity(holder, AddReason.SPAWN);
    }

    public static double roundToHalf(double value) {
        return Math.floor(value) + 0.5;
    }

    /**
     * Convert a yaw angle to the nearest cardinal direction index.
     * 0=North, 1=East, 2=South, 3=West
     */
    private int getCardinalDirectionFromYaw(float yaw) {
        // Normalize yaw to 0-360 range
        yaw = yaw % 360f;
        if (yaw < 0f) yaw += 360f;
        
        // Add 45 to rotate the boundaries, then divide by 90
        // This makes: 0-90 -> North, 90-180 -> East, 180-270 -> South, 270-360 -> West
        int direction = Math.round((yaw + 45f) / 90f) % 4;
        return direction;
    }

    /**
     * Convert cardinal direction index to yaw in radians.
     * North (0): 0, East (1): π/2, South (2): π, West (3): -π/2
     */
    private float getYawFromCardinalDirection(int directionIndex) {
        return switch(directionIndex) {
            case 0 -> 0;                              // North
            case 1 -> (float) Math.PI / 2;            // East
            case 2 -> (float) Math.PI;                // South
            case 3 -> -(float) Math.PI / 2;           // West
            default -> 0;
        };
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
