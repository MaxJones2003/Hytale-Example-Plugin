package com.getfriedpig.golem.typescript;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Compiles TypeScript source code to bytecode using the Node.js compiler.
 * Spawns a subprocess to invoke the TypeScript compiler CLI.
 */
public class TypeScriptCompiler {
    private static final String COMPILER_RELATIVE_PATH = "compiler/dist/index.js";
    private static final Gson gson = new Gson();

    /**
     * Find the compiler path, trying multiple locations.
     */
    private static String findCompilerPath() throws CompilationException {
        // Try relative path from current working directory
        Path p1 = Paths.get(COMPILER_RELATIVE_PATH);
        if (Files.exists(p1)) {
            return p1.toAbsolutePath().toString();
        }

        // Try relative to user.dir (mod runtime directory)
        String userDir = System.getProperty("user.dir");
        Path p2 = Paths.get(userDir, COMPILER_RELATIVE_PATH);
        if (Files.exists(p2)) {
            return p2.toAbsolutePath().toString();
        }

        // Try parent directory (in case mod is in a subdirectory)
        Path p3 = Paths.get(userDir, "..", COMPILER_RELATIVE_PATH).normalize();
        if (Files.exists(p3)) {
            return p3.toAbsolutePath().toString();
        }

        throw new CompilationException(
            "Could not find compiler at: " + COMPILER_RELATIVE_PATH + 
            "\nTried: " + p1 + ", " + p2 + ", " + p3 +
            "\nCurrent dir: " + userDir
        );
    }

    /**
     * Compiles TypeScript source code to bytecode.
     * 
     * @param sourceCode The TypeScript source code to compile
     * @return The compiled bytecode as a JsonObject
     * @throws CompilationException if compilation fails
     */
    public static JsonObject compile(String sourceCode) throws CompilationException {
        String compilerPath = findCompilerPath();
        
        try {
            // Start the Node.js compiler process
            ProcessBuilder pb = new ProcessBuilder("node", compilerPath);
            pb.redirectErrorStream(false);
            
            Process process = pb.start();

            // Send source code to stdin
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(sourceCode.getBytes("UTF-8"));
                stdin.flush();
            } catch (IOException e) {
                throw new CompilationException("Failed to write source code to compiler: " + e.getMessage(), e);
            }

            // Read output from stdout
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                output = sb.toString().trim();
            } catch (IOException e) {
                throw new CompilationException("Failed to read compiler output: " + e.getMessage(), e);
            }

            // Read error output if any
            String errorOutput;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                errorOutput = sb.toString().trim();
            }

            // Wait for process to complete
            int exitCode = process.waitFor();
            
            if (!errorOutput.isEmpty()) {
                System.out.println("[TypeScript] Stderr: " + errorOutput);
            }

            if (output.isEmpty()) {
                throw new CompilationException(
                    "Compiler produced no output. Exit code: " + exitCode + 
                    "\nStderr: " + (errorOutput.isEmpty() ? "(empty)" : errorOutput)
                );
            }

            // Parse the JSON response
            JsonObject response;
            try {
                response = gson.fromJson(output, JsonObject.class);
            } catch (Exception e) {
                throw new CompilationException("Failed to parse compiler response as JSON: " + e.getMessage() + 
                    "\nResponse was: " + output, e);
            }

            if (response == null) {
                throw new CompilationException("Invalid compiler response (null): " + output);
            }

            if (!response.has("success")) {
                throw new CompilationException("Compiler response missing 'success' field: " + output);
            }

            boolean success = response.get("success").getAsBoolean();

            if (!success) {
                if (response.has("error")) {
                    String error = response.get("error").getAsString();
                    throw new CompilationException("Compilation failed: " + error);
                } else {
                    throw new CompilationException("Compilation failed with no error message");
                }
            }

            return response.getAsJsonObject("bytecode");

        } catch (IOException e) {
            throw new CompilationException("Failed to spawn compiler process: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompilationException("Compiler process was interrupted", e);
        }
    }

    /**
     * Exception thrown when TypeScript compilation fails.
     */
    public static class CompilationException extends Exception {
        public CompilationException(String message) {
            super(message);
        }

        public CompilationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
