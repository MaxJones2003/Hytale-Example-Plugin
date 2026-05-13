package com.getfriedpig.golem.typescript;

import com.google.gson.JsonObject;

/**
 * TypeScript compilation pipeline.
 * Handles compiling TypeScript source to bytecode.
 */
public class TypeScript {
    static boolean hadCompilationError = false;

    /**
     * Compile TypeScript source code to bytecode.
     * 
     * @param source The TypeScript source code
     * @return The compiled bytecode as JsonObject
     */
    public static JsonObject compile(String source) {
        hadCompilationError = false;

        try {
            return TypeScriptCompiler.compile(source);
        } catch (TypeScriptCompiler.CompilationException e) {
            error(e.getMessage());
            return null;
        }
    }

    /**
     * Compile from a file path.
     * 
     * @param filePath Path to the TypeScript file
     * @param fileContent The content of the file
     * @return The compiled bytecode as JsonObject
     */
    public static JsonObject compileFile(String filePath, String fileContent) {
        hadCompilationError = false;

        try {
            return TypeScriptCompiler.compile(fileContent);
        } catch (TypeScriptCompiler.CompilationException e) {
            error("Failed to compile " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Report a compilation error.
     */
    static void error(String message) {
        System.err.println("[TypeScript Compiler Error] " + message);
        hadCompilationError = true;
    }

    /**
     * Check if there were any compilation errors.
     */
    public static boolean hadError() {
        return hadCompilationError;
    }
}
