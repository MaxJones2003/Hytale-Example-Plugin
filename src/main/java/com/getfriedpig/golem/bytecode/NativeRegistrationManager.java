package com.getfriedpig.golem.bytecode;

import com.getfriedpig.golem.bytecode.NativeFunctions.Entity;
import com.getfriedpig.golem.bytecode.NativeFunctions.Logger;
import com.getfriedpig.golem.bytecode.NativeFunctions.MathFunctions;

/**
 * Central manager for registering all native function namespaces
 * Provides a single entry point for initializing all available native functions
 * 
 * Usage:
 *   NativeRegistrationManager.registerAll();
 */
public class NativeRegistrationManager {
    
    private static boolean registered = false;
    
    /**
     * Register all native function namespaces at once
     * Safe to call multiple times - will only register once
     */
    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        

        // Register all namespaces
        MathFunctions.register();
        Entity.register();
        Logger.register();
        
        registered = true;
    }
    
    /**
     * Reset registration state (mainly for testing)
     */
    protected static synchronized void reset() {
        registered = false;
    }
}
