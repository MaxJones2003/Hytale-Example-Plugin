package com.getfriedpig.golem.bytecode.NativeFunctions;

import com.getfriedpig.golem.bytecode.NativeFunctionRegistry;

/**
 * Native math function registrations
 * Provides mathematical operations accessible from bytecode scripts
 */
public class MathFunctions {
    
    /**
     * Register all math functions in the "Math" namespace
     */
    public static void register() {
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
