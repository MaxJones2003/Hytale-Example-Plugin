package com.getfriedpig.golem.bytecode;

import java.util.UUID;

/**
 * Example: How backend code uses ExecutionContext to send commands to golems
 * 
 * During script execution, native functions can access the UUID of the golem running the script.
 * This allows them to send commands like InstructGolemCommand does.
 */
public class ExecutionContextUsageExample {

    /**
     * Example: Native function that sends a command to the golem
     * 
     * In NativeFunctionExample.java:
     */
    public static void exampleNativeCallWithCommand() {
        // This would be in a native function registration:
        /*
        registry.register("Golem", "moveForward", args -> {
            UUID golumUUID = ExecutionContext.getCurrentEntityUUID();
            if (golumUUID == null) {
                throw new Exception("Script not running in a golem context");
            }
            
            // Now you can send a command to the golem
            // Similar to how InstructGolemCommand works:
            double distance = (Double) args.get(0);
            sendGolemCommand(golumUUID, "moveForward", distance);
            return null;
        });
        */
    }

    /**
     * Example: How to extend native functions to send commands
     * 
     * These patterns allow users to write TypeScript like:
     * 
     *   Golem.moveForward(10);
     *   Golem.turnLeft(45);
     *   Golem.placeBlock("dirt");
     */
    public static class GolemCommandNativeFunctions {
        /*
        public static void registerGolemCommands() {
            NativeFunctionRegistry registry = NativeFunctionRegistry.getInstance();
            
            // Golem.moveForward(distance)
            registry.register("Golem", "moveForward", args -> {
                UUID golemUUID = ExecutionContext.getCurrentEntityUUID();
                double distance = (Double) args.get(0);
                // Send InstructGolemCommand or similar
                sendInstructionToGolem(golemUUID, new MoveInstruction(distance));
                return null;
            });
            
            // Golem.turnLeft(degrees)
            registry.register("Golem", "turnLeft", args -> {
                UUID golemUUID = ExecutionContext.getCurrentEntityUUID();
                double degrees = (Double) args.get(0);
                sendInstructionToGolem(golemUUID, new TurnInstruction(degrees));
                return null;
            });
            
            // Golem.placeBlock(blockType)
            registry.register("Golem", "placeBlock", args -> {
                UUID golemUUID = ExecutionContext.getCurrentEntityUUID();
                String blockType = args.get(0).toString();
                sendInstructionToGolem(golemUUID, new PlaceBlockInstruction(blockType));
                return null;
            });
        }
        
        private static void sendInstructionToGolem(UUID golemUUID, Object instruction) {
            // Send command similar to InstructGolemCommand
            // This is where you'd integrate with your command system
        }
        */
    }

    /**
     * Key points:
     * 
     * 1. ExecutionContext.getCurrentEntityUUID() is available during script execution
     * 2. Use it to get the UUID of the golem running the script
     * 3. Send commands to that golem (like InstructGolemCommand does)
     * 4. This keeps the user-facing API simple - they just call Golem.moveForward(10)
     * 5. The backend handles getting the UUID and routing the command
     * 
     * Example TypeScript user code:
     * 
     *   // Simple and clean - no UUID management for users
     *   Golem.moveForward(10);
     *   Golem.turnLeft(45);
     *   Golem.placeBlock("dirt");
     *   
     * Backend handles:
     * 
     *   // Gets called when user executes Golem.moveForward(10)
     *   UUID golemUUID = ExecutionContext.getCurrentEntityUUID();
     *   // Send instruction to golem with UUID
     *   sendCommandToGolem(golemUUID, moveInstruction);
     */
}
