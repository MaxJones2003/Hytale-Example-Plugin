package com.getfriedpig.golem.bytecode;

import java.util.*;

/**
 * Registry for native functions organized by namespace and class
 * Supports calling native Java functions from bytecode
 * 
 * Uses a singleton pattern - all VMs share the same registry
 */
public class NativeFunctionRegistry {
    private static NativeFunctionRegistry instance;
    private Map<String, Map<String, Map<String, NativeFunction>>> registry = new HashMap<>();

    private NativeFunctionRegistry() {
        // Private constructor for singleton
    }

    /**
     * Get the shared instance
     */
    public static synchronized NativeFunctionRegistry getInstance() {
        if (instance == null) {
            instance = new NativeFunctionRegistry();
        }
        return instance;
    }

    /**
     * Functional interface for native functions
     */
    @FunctionalInterface
    public interface NativeFunction {
        Object invoke(List<Object> args) throws Exception;
    }

    /**
     * Register a native function
     * @param namespace Namespace (e.g., "Entity", "Player", "Math")
     * @param className Class name (e.g., "EntityUtils", "PlayerManager")
     * @param methodName Method name
     * @param function The function implementation
     */
    public void register(String namespace, String className, String methodName, NativeFunction function) {
        registry.computeIfAbsent(namespace, k -> new HashMap<>())
                .computeIfAbsent(className, k -> new HashMap<>())
                .put(methodName, function);
    }

    /**
     * Register a function with just namespace and method name
     */
    public void register(String namespace, String methodName, NativeFunction function) {
        register(namespace, "default", methodName, function);
    }

    /**
     * Call a native function
     * @return The function result
     */
    public Object call(String namespace, String className, String methodName, List<Object> args) throws Exception {
        if (className == null) {
            className = "default";
        }

        Map<String, Map<String, NativeFunction>> namespaceMap = registry.get(namespace);
        if (namespaceMap == null) {
            throw new Exception("Namespace not found: " + namespace);
        }

        Map<String, NativeFunction> classMap = namespaceMap.get(className);
        if (classMap == null) {
            throw new Exception("Class not found in namespace '" + namespace + "': " + className);
        }

        NativeFunction function = classMap.get(methodName);
        if (function == null) {
            throw new Exception("Method not found in '" + namespace + "." + className + "': " + methodName);
        }

        return function.invoke(args);
    }

    /**
     * Call a native function with simpler signature
     */
    public Object call(String namespace, String methodName, List<Object> args) throws Exception {
        return call(namespace, "default", methodName, args);
    }

    /**
     * Check if a function is registered
     */
    public boolean exists(String namespace, String className, String methodName) {
        if (className == null) {
            className = "default";
        }

        Map<String, Map<String, NativeFunction>> namespaceMap = registry.get(namespace);
        if (namespaceMap == null) {
            return false;
        }

        Map<String, NativeFunction> classMap = namespaceMap.get(className);
        if (classMap == null) {
            return false;
        }

        return classMap.containsKey(methodName);
    }

    /**
     * List all registered functions (useful for debugging)
     */
    public void listAll() {
        for (String namespace : registry.keySet()) {
            System.out.println("Namespace: " + namespace);
            for (String className : registry.get(namespace).keySet()) {
                System.out.println("  Class: " + className);
                for (String methodName : registry.get(namespace).get(className).keySet()) {
                    System.out.println("    Method: " + methodName);
                }
            }
        }
    }
}
