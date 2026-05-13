package com.getfriedpig.golem.bytecode.NativeFunctions;

import com.getfriedpig.golem.bytecode.NativeFunctionRegistry;

/**
 * Native logger function registrations
 * Provides logging operations accessible from bytecode scripts
 */
public class Logger {
    
    /**
     * Register all logger functions in the "Logger" namespace
     */
    public static void register() {
        NativeFunctionRegistry registry = NativeFunctionRegistry.getInstance();
        
        System.out.println("[Logger] Registering logger functions...");
        
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
        
        System.out.println("[Logger] Logger functions registered!");
    }
}
