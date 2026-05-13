package com.getfriedpig.golem.bytecode;

import java.util.List;

/**
 * Example: How to register and use native functions
 * 
 * Usage patterns:
 * 1. Namespace only: "Math.random()"
 * 2. Namespace + Class: "Entity.PlayerUtils.teleport(player, x, y, z)"
 * 3. Namespace + Method: "Logger.debug(message)"
 */
public class NativeFunctionExample {

    /**
     * Example: Register math functions
     */
    public static void registerMathFunctions(BytecodeVM vm) {
        NativeFunctionRegistry registry = NativeFunctionRegistry.getInstance();
        
        // Math.random() - no args
        registry.register("Math", "random", args -> Math.random());
        
        // Math.abs(x)
        registry.register("Math", "abs", args -> {
            double x = toDouble(args.get(0));
            return Math.abs(x);
        });
        
        // Math.sqrt(x)
        registry.register("Math", "sqrt", args -> {
            double x = toDouble(args.get(0));
            return Math.sqrt(x);
        });
        
        // Math.floor(x)
        registry.register("Math", "floor", args -> {
            double x = toDouble(args.get(0));
            return Math.floor(x);
        });
        
        // Math.ceil(x)
        registry.register("Math", "ceil", args -> {
            double x = toDouble(args.get(0));
            return Math.ceil(x);
        });
    }

    /**
     * Example: Register entity functions
     */
    public static void registerEntityFunctions(BytecodeVM vm) {
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
            System.out.println("[Entity " + entityUUID + "] " + message);
            return null;
        });
    }

    /**
     * Example: Register logger functions
     */
    public static void registerLoggerFunctions(BytecodeVM vm) {
        NativeFunctionRegistry registry = NativeFunctionRegistry.getInstance();
        
        // Logger.debug(message)
        registry.register("Logger", "debug", args -> {
            System.out.println("[DEBUG] " + args.get(0));
            return null;
        });
        
        // Logger.warn(message)
        registry.register("Logger", "warn", args -> {
            System.err.println("[WARN] " + args.get(0));
            return null;
        });
    }

    /**
     * Example: How to use it in your code
     */
    public static void exampleUsage() throws Exception {
        // Create and configure VM
        BytecodeVM vm = new BytecodeVM();
        
        // Register native functions
        registerMathFunctions(vm);
        registerEntityFunctions(vm);
        registerLoggerFunctions(vm);
        
        // Load and execute bytecode
        // vm.load(bytecodeJson);
        // VMResult result = vm.execute();
    }

    private static double toDouble(Object value) throws Exception {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new Exception("Cannot convert " + value + " to number");
    }
}
