package com.getfriedpig.golem.bytecode;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the state of a running bytecode program
 * Can be saved and resumed at checkpoints
 */
public class ExecutionState {
    // Program counter
    public int pc = 0;

    // Call stack: each frame contains (function_id, return_pc)
    public List<CallFrame> callStack = new ArrayList<>();

    // Value stack for computation
    public List<Object> stack = new ArrayList<>();

    // Local variables
    public Map<String, Object> locals = new HashMap<>();

    // Global variables
    public Map<String, Object> globals = new HashMap<>();

    // Current function being executed
    public int currentFunctionId = 0;

    // Current 'this' object reference (for instance methods)
    public ObjectInstance thisObject = null;

    // Whether execution is paused
    public boolean paused = false;

    // Checkpoint ID where execution was paused
    public String pausedAtCheckpoint = null;

    // UUID of the entity running this script
    public java.util.UUID entityUUID = null;

    // ECS Context - the store managing entities (stable, safe to store)
    public Store<EntityStore> entityStore = null;
    public Ref<EntityStore> entityRef = null;

    // ECS Context - command buffer for queuing entity changes (stable, safe to store)
    public CommandBuffer<EntityStore> commandBuffer = null;



    /**
     * Represents a call stack frame
     */
    public static class CallFrame {
        public int functionId;
        public int returnPc;
        public Map<String, Object> locals;
        public ObjectInstance thisObject;  // For method calls

        public CallFrame(int functionId, int returnPc) {
            this.functionId = functionId;
            this.returnPc = returnPc;
            this.locals = new HashMap<>();
            this.thisObject = null;
        }

        public CallFrame(int functionId, int returnPc, ObjectInstance thisObject) {
            this.functionId = functionId;
            this.returnPc = returnPc;
            this.locals = new HashMap<>();
            this.thisObject = thisObject;
        }
    }

    /**
     * Push a value onto the stack
     */
    public void pushStack(Object value) {
        stack.add(value);
    }

    /**
     * Pop a value from the stack
     */
    public Object popStack() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow");
        }
        return stack.remove(stack.size() - 1);
    }

    /**
     * Peek at the top of the stack without removing
     */
    public Object peekStack() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return stack.get(stack.size() - 1);
    }

    /**
     * Get the size of the stack
     */
    public int stackSize() {
        return stack.size();
    }

    /**
     * Set a local variable
     */
    public void setLocal(String name, Object value) {
        locals.put(name, value);
    }

    /**
     * Get a local variable
     */
    public Object getLocal(String name) {
        Object value = locals.get(name);
        if (value == null && !locals.containsKey(name)) {
            throw new RuntimeException("Undefined variable: " + name);
        }
        return value;
    }

    /**
     * Set a global variable
     */
    public void setGlobal(String name, Object value) {
        globals.put(name, value);
    }

    /**
     * Get a global variable
     */
    public Object getGlobal(String name) {
        Object value = globals.get(name);
        if (value == null && !globals.containsKey(name)) {
            throw new RuntimeException("Undefined global: " + name);
        }
        return value;
    }

    /**
     * Save current state for resumption
     */
    public ExecutionState snapshot() {
        ExecutionState copy = new ExecutionState();
        copy.pc = this.pc;
        copy.currentFunctionId = this.currentFunctionId;
        copy.thisObject = this.thisObject;
        copy.stack = new ArrayList<>(this.stack);
        copy.locals = new HashMap<>(this.locals);
        copy.globals = new HashMap<>(this.globals);
        copy.callStack = new ArrayList<>(this.callStack);
        copy.paused = this.paused;
        copy.pausedAtCheckpoint = this.pausedAtCheckpoint;
        copy.entityUUID = this.entityUUID;
        copy.entityStore = this.entityStore;
        copy.entityRef = this.entityRef;
        copy.commandBuffer = this.commandBuffer;
        return copy;
    }

    @Override
    public String toString() {
        return "ExecutionState{" +
                "pc=" + pc +
                ", stackSize=" + stack.size() +
                ", locals=" + locals.size() +
                ", paused=" + paused +
                ", checkpoint='" + pausedAtCheckpoint + '\'' +
                ", entityUUID=" + entityUUID +
                '}';
    }

    public ExecutionState clone() {
        ExecutionState clone = new ExecutionState();
        clone.pc = this.pc;
        clone.currentFunctionId = this.currentFunctionId;
        clone.thisObject = this.thisObject;
        clone.stack = new ArrayList<>(this.stack);
        clone.locals = new HashMap<>(this.locals);
        clone.globals = new HashMap<>(this.globals);
        clone.callStack = new ArrayList<>(this.callStack);
        clone.paused = this.paused;
        clone.pausedAtCheckpoint = this.pausedAtCheckpoint;
        clone.entityUUID = this.entityUUID;
        return clone;
    }
}
